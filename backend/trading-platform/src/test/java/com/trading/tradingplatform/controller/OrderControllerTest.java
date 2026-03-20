package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.order.OrderBookResponse;
import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    @Test
    void placeBuyOrder_success() {

        // Arrange
        String email = "test@gmail.com";

        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderResponse response = new OrderResponse();

        when(authentication.getName()).thenReturn(email);
        when(orderService.placeBuyOrder(email, request)).thenReturn(response);

        // Act
        OrderResponse result = orderController.placeBuyOrder(authentication, request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        verify(authentication).getName();
        verify(orderService).placeBuyOrder(email, request);
    }

    @Test
    void cancelOrder_success() {

        // Arrange
        Long orderId = 1L;
        String username = "test@gmail.com";

        when(authentication.getName()).thenReturn(username);

        // Act
        orderController.cancelOrder(orderId, authentication);

        // Assert
        verify(authentication).getName();
        verify(orderService).cancelOrder(orderId, username);
    }

    @Test
    void getOrderBook_success() {

        // Arrange
        String symbol = "AAPL";

        OrderBookResponse mockResponse = new OrderBookResponse();

        when(orderService.getOrderBook(symbol)).thenReturn(mockResponse);

        // Act
        ResponseEntity<OrderBookResponse> response =
                orderController.getOrderBook(symbol);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(orderService).getOrderBook(symbol);
    }

    @Test
    void getUserOrders_success() {

        // Arrange
        String email = "test@gmail.com";
        List<OrderResponse> mockList = List.of(new OrderResponse());

        when(authentication.getName()).thenReturn(email);
        when(orderService.getUserOrders(email)).thenReturn(mockList);

        // Act
        List<OrderResponse> result =
                orderController.getUserOrders(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(authentication).getName();
        verify(orderService).getUserOrders(email);
    }

    @Test
    void placeSellOrder_success() {

        // Arrange
        String email = "test@gmail.com";

        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderResponse response = new OrderResponse();

        when(authentication.getName()).thenReturn(email);
        when(orderService.placeSellOrder(email, request)).thenReturn(response);

        // Act
        OrderResponse result =
                orderController.placeSellOrder(authentication, request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        verify(authentication).getName();
        verify(orderService).placeSellOrder(email, request);
    }
}