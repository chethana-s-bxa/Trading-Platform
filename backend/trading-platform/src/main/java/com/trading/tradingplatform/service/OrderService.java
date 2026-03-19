package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.order.OrderBookResponse;
import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.entity.Order;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.MarketStatus;
import com.trading.tradingplatform.entity.enums.OrderStatus;
import com.trading.tradingplatform.entity.enums.TradeType;
import com.trading.tradingplatform.exception.*;
import com.trading.tradingplatform.repository.OrderRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.trading.tradingplatform.dto.order.OrderBookStreamMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final TradeHistoryService tradeHistoryService;
    private final PortfolioService portfolioService;
    private final MarketDataPublisher marketDataPublisher;
    private final MarketStatusService marketStatusService;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderResponse placeBuyOrder(String email, PlaceOrderRequest request) {

        validateMarketOpen();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + request.getSymbol()));

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .tradeType(TradeType.BUY)
                .orderCategory(request.getOrderCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .orderStatus(OrderStatus.OPEN)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Matching engine will run here (next step)
        matchOrders(stock.getSymbol());
        broadcastOrderBook(stock.getSymbol());
        return mapToResponse(savedOrder);
    }

    public OrderResponse placeSellOrder(String email, PlaceOrderRequest request) {

        validateMarketOpen();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + request.getSymbol()));

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .tradeType(TradeType.SELL)
                .orderCategory(request.getOrderCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .orderStatus(OrderStatus.OPEN)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Trigger matching engine
        matchOrders(stock.getSymbol());
        broadcastOrderBook(stock.getSymbol());

        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getId())
                .symbol(order.getStock().getSymbol())
                .tradeType(order.getTradeType())
                .orderCategory(order.getOrderCategory())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .remainingQuantity(order.getRemainingQuantity())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void matchOrders(String symbol) {

        List<Order> buyOrders = orderRepository
                .findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(
                        symbol,
                        TradeType.BUY,
                        OrderStatus.OPEN
                );

        List<Order> sellOrders = orderRepository
                .findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(
                        symbol,
                        TradeType.SELL,
                        OrderStatus.OPEN
                );

        int buyIndex = 0;
        int sellIndex = 0;

        while (buyIndex < buyOrders.size() && sellIndex < sellOrders.size()) {

            Order buyOrder = buyOrders.get(buyIndex);
            Order sellOrder = sellOrders.get(sellIndex);

            // Price compatibility check
            if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) < 0) {
                break;
            }

            executeTrade(buyOrder, sellOrder);

            if (buyOrder.getRemainingQuantity() == 0) {
                buyIndex++;
            }

            if (sellOrder.getRemainingQuantity() == 0) {
                sellIndex++;
            }
        }
    }

    private void executeTrade(Order buyOrder, Order sellOrder) {

        int tradeQuantity = Math.min(
                buyOrder.getRemainingQuantity(),
                sellOrder.getRemainingQuantity()
        );

        // trade price = sell order price (exchange rule)
        BigDecimal tradePrice = sellOrder.getPrice();

        // record trade in trade history
        tradeHistoryService.recordMatchedTrade(
                buyOrder.getUser(),
                sellOrder.getUser(),
                buyOrder.getStock(),
                tradeQuantity,
                tradePrice
        );

        // update remaining quantities
        buyOrder.setRemainingQuantity(
                buyOrder.getRemainingQuantity() - tradeQuantity
        );

        sellOrder.setRemainingQuantity(
                sellOrder.getRemainingQuantity() - tradeQuantity
        );

        portfolioService.updatePortfolioAfterBuy(
                buyOrder.getUser(),
                buyOrder.getStock(),
                tradeQuantity,
                tradePrice
        );

        portfolioService.updatePortfolioAfterSell(
                sellOrder.getUser(),
                sellOrder.getStock(),
                tradeQuantity
        );

        // update order statuses
        updateOrderStatus(buyOrder);
        updateOrderStatus(sellOrder);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);

        broadcastOrderBook(buyOrder.getStock().getSymbol());
    }

    private void updateOrderStatus(Order order) {

        if (order.getRemainingQuantity() == 0) {
            order.setOrderStatus(OrderStatus.FILLED);
        }
        else if (order.getRemainingQuantity() < order.getQuantity()) {
            order.setOrderStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }

    public List<OrderResponse> getUserOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));


        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderBookResponse getOrderBook(String symbol) {

        List<Order> buyOrders = orderRepository
                .findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(
                        symbol,
                        TradeType.BUY,
                        OrderStatus.OPEN
                );

        List<Order> sellOrders = orderRepository
                .findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(
                        symbol,
                        TradeType.SELL,
                        OrderStatus.OPEN
                );

        List<OrderResponse> buyResponses = buyOrders
                .stream()
                .map(this::mapToResponse)
                .toList();

        List<OrderResponse> sellResponses = sellOrders
                .stream()
                .map(this::mapToResponse)
                .toList();

        return OrderBookResponse.builder()
                .symbol(symbol)
                .buyOrders(buyResponses)
                .sellOrders(sellResponses)
                .build();
    }

    public void cancelOrder(Long orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!order.getUser().getEmail().equals(username)) {
            throw new UnauthorizedActionException("You are not allowed to cancel this order");
        }

        if (order.getOrderStatus() == OrderStatus.FILLED ||
                order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException("Order cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        broadcastOrderBook(order.getStock().getSymbol());
    }

    /**
     * Broadcasts the current order book snapshot for a given stock symbol
     * to WebSocket clients.
     *
     * Topic format:
     * /topic/orderbook/{symbol}
     */
    private void broadcastOrderBook(String symbol) {

        OrderBookResponse orderBook = getOrderBook(symbol);

        List<OrderBookStreamMessage.OrderLevel> bids =
                orderBook.getBuyOrders()
                        .stream()
                        .map(order -> new OrderBookStreamMessage.OrderLevel(
                                order.getPrice(),
                                order.getRemainingQuantity()
                        ))
                        .toList();

        List<OrderBookStreamMessage.OrderLevel> asks =
                orderBook.getSellOrders()
                        .stream()
                        .map(order -> new OrderBookStreamMessage.OrderLevel(
                                order.getPrice(),
                                order.getRemainingQuantity()
                        ))
                        .toList();

        OrderBookStreamMessage message =
                OrderBookStreamMessage.builder()
                        .symbol(symbol)
                        .bids(bids)
                        .asks(asks)
                        .build();

        marketDataPublisher.broadcastOrderBook(message);
    }

    private void validateMarketOpen() {
        if (marketStatusService.getMarketStatus() == MarketStatus.CLOSED) {
            throw new MarketClosedException("Order Rejected: Market is closed");
        }
    }
}