package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.order.OrderBookResponse;
import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.entity.*;
import com.trading.tradingplatform.entity.enums.*;
import com.trading.tradingplatform.exception.*;
import com.trading.tradingplatform.repository.OrderRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock private StockRepository stockRepository;
    @Mock private UserRepository userRepository;
    @Mock private TradeHistoryService tradeHistoryService;
    @Mock private PortfolioService portfolioService;
    @Mock private MarketDataPublisher marketDataPublisher;
    @Mock private MarketStatusService marketStatusService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeBuyOrder_success() {

        // ---------- Arrange ----------

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);
        request.setPrice(BigDecimal.valueOf(100));

        User user = new User();
        user.setEmail("test@example.com");

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order savedOrder = Order.builder()
                .id(1L)
                .user(user)
                .stock(stock)
                .tradeType(TradeType.BUY)
                .price(BigDecimal.valueOf(100))
                .quantity(10)
                .remainingQuantity(10)
                .orderStatus(OrderStatus.OPEN)
                .build();


        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of());

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of());

        // ---------- Act ----------

        OrderResponse response =
                orderService.placeBuyOrder("test@example.com", request);

        // ---------- Assert ----------

        assertEquals("AAPL", response.getSymbol());
        assertEquals(10, response.getQuantity());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeBuyOrder_marketClosed() {

        // ---------- Arrange ----------

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);
        request.setPrice(BigDecimal.valueOf(100));

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.CLOSED);

        // ---------- Act & Assert ----------

        assertThrows(MarketClosedException.class, () ->
                orderService.placeBuyOrder("test@example.com", request)
        );
    }

    @Test
    void placeBuyOrder_userNotFound() {

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                orderService.placeBuyOrder("test@example.com", request)
        );
    }

    @Test
    void placeBuyOrder_stockNotFound() {

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");

        User user = new User();
        user.setEmail("test@example.com");

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () ->
                orderService.placeBuyOrder("test@example.com", request)
        );
    }

    @Test
    void placeSellOrder_success() {

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);
        request.setPrice(BigDecimal.valueOf(150));

        User user = new User();
        user.setEmail("test@example.com");

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order savedOrder = Order.builder()
                .id(1L)
                .user(user)
                .stock(stock)
                .tradeType(TradeType.SELL)
                .price(BigDecimal.valueOf(150))
                .quantity(5)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of());

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of());

        OrderResponse response =
                orderService.placeSellOrder("test@example.com", request);

        assertEquals("AAPL", response.getSymbol());
        assertEquals(5, response.getQuantity());
    }

    @Test
    void placeSellOrder_marketClosed() {

        PlaceOrderRequest request = new PlaceOrderRequest();

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.CLOSED);

        assertThrows(MarketClosedException.class, () ->
                orderService.placeSellOrder("test@example.com", request)
        );
    }

    @Test
    void placeSellOrder_userNotFound() {

        PlaceOrderRequest request = new PlaceOrderRequest();

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                orderService.placeSellOrder("test@example.com", request)
        );
    }

    @Test
    void placeSellOrder_stockNotFound() {

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("AAPL");   // ✅ ADD THIS

        User user = new User();

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () ->
                orderService.placeSellOrder("test@example.com", request)
        );
    }

    @Test
    void cancelOrder_success() {

        Order order = new Order();
        User user = new User();
        user.setEmail("test@example.com");

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        order.setId(1L);
        order.setUser(user);
        order.setStock(stock);
        order.setOrderStatus(OrderStatus.OPEN);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of());

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of());

        orderService.cancelOrder(1L, "test@example.com");

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_notFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderService.cancelOrder(1L, "test@example.com")
        );
    }

    @Test
    void cancelOrder_unauthorized() {

        Order order = new Order();
        User user = new User();
        user.setEmail("owner@example.com");

        order.setUser(user);
        order.setOrderStatus(OrderStatus.OPEN);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(UnauthorizedActionException.class, () ->
                orderService.cancelOrder(1L, "other@example.com")
        );
    }

    @Test
    void cancelOrder_alreadyFilled() {

        Order order = new Order();
        User user = new User();
        user.setEmail("test@example.com");

        order.setUser(user);
        order.setOrderStatus(OrderStatus.FILLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(OrderCancellationException.class, () ->
                orderService.cancelOrder(1L, "test@example.com")
        );
    }

    @Test
    void cancelOrder_alreadyCancelled() {

        Order order = new Order();
        User user = new User();
        user.setEmail("test@example.com");

        order.setUser(user);
        order.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(OrderCancellationException.class, () ->
                orderService.cancelOrder(1L, "test@example.com")
        );
    }

    @Test
    void getUserOrders_success() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(10)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(order));

        List<OrderResponse> response =
                orderService.getUserOrders("test@example.com");

        assertEquals(1, response.size());
        assertEquals("AAPL", response.get(0).getSymbol());
    }

    @Test
    void getUserOrders_userNotFound() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                orderService.getUserOrders("test@example.com")
        );
    }

    @Test
    void getOrderBook_success() {

        String symbol = "AAPL";

        Stock stock = new Stock();
        stock.setSymbol(symbol);

        Order buyOrder = Order.builder()
                .stock(stock)
                .tradeType(TradeType.BUY)
                .price(BigDecimal.valueOf(150))
                .remainingQuantity(10)
                .build();

        Order sellOrder = Order.builder()
                .stock(stock)
                .tradeType(TradeType.SELL)
                .price(BigDecimal.valueOf(155))
                .remainingQuantity(5)
                .build();

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(
                symbol, TradeType.BUY, OrderStatus.OPEN))
                .thenReturn(List.of(buyOrder));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(
                symbol, TradeType.SELL, OrderStatus.OPEN))
                .thenReturn(List.of(sellOrder));

        OrderBookResponse response = orderService.getOrderBook(symbol);

        assertEquals("AAPL", response.getSymbol());
        assertEquals(1, response.getBuyOrders().size());
        assertEquals(1, response.getSellOrders().size());
    }

    @Test
    void executeTrade_partiallyFilled() {

        // ---------- Arrange ----------

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        User seller = new User();
        seller.setId(2L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        // BUY order (bigger quantity)
        Order buyOrder = Order.builder()
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(10)
                .remainingQuantity(10)
                .orderStatus(OrderStatus.OPEN)
                .build();

        // SELL order (smaller → partial fill)
        Order sellOrder = Order.builder()
                .user(seller)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(5)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        // ---------- Mock REQUIRED dependencies ----------

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // Save should return same object
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Matching engine inputs
        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(
                any(), any(), any()))
                .thenReturn(List.of(buyOrder));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(
                any(), any(), any()))
                .thenReturn(List.of(sellOrder));

        // Void methods → doNothing
        doNothing().when(tradeHistoryService)
                .recordMatchedTrade(any(), any(), any(), anyInt(), any());

        doNothing().when(portfolioService)
                .updatePortfolioAfterBuy(any(), any(), anyInt(), any());

        doNothing().when(portfolioService)
                .updatePortfolioAfterSell(any(), any(), anyInt());

        doNothing().when(marketDataPublisher)
                .broadcastOrderBook(any());

        // ---------- Act ----------
        orderService.placeBuyOrder(
                "test@example.com",
                new PlaceOrderRequest(
                        "AAPL",
                        OrderCategory.LIMIT,
                        BigDecimal.valueOf(150),
                        10
                )
        );

        // ---------- Assert ----------
        assertEquals(OrderStatus.PARTIALLY_FILLED, buyOrder.getOrderStatus());
        assertEquals(5, buyOrder.getRemainingQuantity());
    }

    @Test
    void getOrderBook_empty() {

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of());

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of());

        OrderBookResponse response = orderService.getOrderBook("AAPL");

        assertTrue(response.getBuyOrders().isEmpty());
        assertTrue(response.getSellOrders().isEmpty());
    }

    @Test
    void matchOrders_priceMismatch_noTrade() {

        User user = new User();
        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order buyOrder = Order.builder()
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(100)) // lower
                .quantity(10)
                .remainingQuantity(10)
                .orderStatus(OrderStatus.OPEN)
                .build();

        Order sellOrder = Order.builder()
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(150)) // higher
                .quantity(10)
                .remainingQuantity(10)
                .orderStatus(OrderStatus.OPEN)
                .build();

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbol(any())).thenReturn(Optional.of(stock));

        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of(buyOrder));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of(sellOrder));

        orderService.placeBuyOrder("test@example.com",
                new PlaceOrderRequest("AAPL", OrderCategory.LIMIT, BigDecimal.valueOf(100), 10));

        // No execution → still OPEN
        assertEquals(OrderStatus.OPEN, buyOrder.getOrderStatus());
    }

    @Test
    void executeTrade_fullyFilled() {

        User user = new User();
        user.setEmail("test@example.com");

        User seller = new User();

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order buyOrder = Order.builder()
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(5)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        Order sellOrder = Order.builder()
                .user(seller)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(5)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        setupCommonMocks(user, stock, buyOrder, sellOrder);

        orderService.placeBuyOrder("test@example.com",
                new PlaceOrderRequest("AAPL", OrderCategory.LIMIT, BigDecimal.valueOf(150), 5));

        assertEquals(OrderStatus.FILLED, buyOrder.getOrderStatus());
        assertEquals(OrderStatus.FILLED, sellOrder.getOrderStatus());
    }

    @Test
    void executeTrade_buySmallerThanSell() {

        User user = new User();
        user.setEmail("test@example.com");

        User seller = new User();

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Order buyOrder = Order.builder()
                .user(user)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(5)
                .remainingQuantity(5)
                .orderStatus(OrderStatus.OPEN)
                .build();

        Order sellOrder = Order.builder()
                .user(seller)
                .stock(stock)
                .price(BigDecimal.valueOf(150))
                .quantity(10)
                .remainingQuantity(10)
                .orderStatus(OrderStatus.OPEN)
                .build();

        setupCommonMocks(user, stock, buyOrder, sellOrder);

        orderService.placeBuyOrder("test@example.com",
                new PlaceOrderRequest("AAPL", OrderCategory.LIMIT, BigDecimal.valueOf(150), 5));

        assertEquals(OrderStatus.FILLED, buyOrder.getOrderStatus());
        assertEquals(OrderStatus.PARTIALLY_FILLED, sellOrder.getOrderStatus());
    }

    private void setupCommonMocks(User user, Stock stock, Order buyOrder, Order sellOrder) {

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(stockRepository.findBySymbol(any())).thenReturn(Optional.of(stock));

        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(any(), any(), any()))
                .thenReturn(List.of(buyOrder));

        when(orderRepository.findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(any(), any(), any()))
                .thenReturn(List.of(sellOrder));

        doNothing().when(tradeHistoryService).recordMatchedTrade(any(), any(), any(), anyInt(), any());
        doNothing().when(portfolioService).updatePortfolioAfterBuy(any(), any(), anyInt(), any());
        doNothing().when(portfolioService).updatePortfolioAfterSell(any(), any(), anyInt());
        doNothing().when(marketDataPublisher).broadcastOrderBook(any());
    }
}
