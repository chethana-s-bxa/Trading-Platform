package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.order.OrderBookResponse;
import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping("/buy")
    public OrderResponse placeBuyOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {

        String email = authentication.getName();

        return orderService.placeBuyOrder(email, request);
    }

    @PostMapping("/sell")
    public OrderResponse placeSellOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {

        String email = authentication.getName();

        return orderService.placeSellOrder(email, request);
    }

    @GetMapping("/user")
    public List<OrderResponse> getUserOrders(Authentication authentication) {

        String email = authentication.getName();

        return orderService.getUserOrders(email);
    }

    @DeleteMapping("/{orderId}")
    public void cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        orderService.cancelOrder(orderId, username);
    }

    @GetMapping("/book/{symbol}")
    public ResponseEntity<OrderBookResponse> getOrderBook(
            @PathVariable String symbol
    ) {

        OrderBookResponse orderBook = orderService.getOrderBook(symbol);

        return ResponseEntity.ok(orderBook);

    }
}