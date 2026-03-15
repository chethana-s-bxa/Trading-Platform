package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.order.OrderResponse;
import com.trading.tradingplatform.dto.order.PlaceOrderRequest;
import com.trading.tradingplatform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public OrderResponse placeBuyOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {

        String username = authentication.getName();

        return orderService.placeBuyOrder(username, request);
    }

    @PostMapping("/sell")
    public OrderResponse placeSellOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {

        String username = authentication.getName();

        return orderService.placeSellOrder(username, request);
    }

    @GetMapping("/user")
    public List<OrderResponse> getUserOrders(Authentication authentication) {

        String username = authentication.getName();

        return orderService.getUserOrders(username);
    }

    @DeleteMapping("/{orderId}")
    public void cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        orderService.cancelOrder(orderId, username);
    }
}