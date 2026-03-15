package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.order.OrderBookResponse;
import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.entity.Order;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.OrderStatus;
import com.trading.tradingplatform.entity.enums.TradeType;
import com.trading.tradingplatform.repository.OrderRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public OrderResponse placeBuyOrder(String userName, PlaceOrderRequest request) {

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

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

        return mapToResponse(savedOrder);
    }

    public OrderResponse placeSellOrder(String userName, PlaceOrderRequest request) {

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

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
    }

    private void updateOrderStatus(Order order) {

        if (order.getRemainingQuantity() == 0) {
            order.setOrderStatus(OrderStatus.FILLED);
        }
        else if (order.getRemainingQuantity() < order.getQuantity()) {
            order.setOrderStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }

    public List<OrderResponse> getUserOrders(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to cancel this order");
        }

        if (order.getOrderStatus() == OrderStatus.FILLED ||
                order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
}