package com.trading.tradingplatform.exception;

import com.trading.tradingplatform.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundExceptions() {

        ResponseEntity<ErrorResponse> user =
                handler.handleUserNotFound(new UserNotFoundException("User not found"));

        ResponseEntity<ErrorResponse> stock =
                handler.handleStockNotFound(new StockNotFoundException("Stock not found"));

        ResponseEntity<ErrorResponse> order =
                handler.handleOrderNotFound(new OrderNotFoundException("Order not found"));

        assertEquals(404, user.getStatusCodeValue());
        assertEquals(404, stock.getStatusCodeValue());
        assertEquals(404, order.getStatusCodeValue());
    }

    @Test
    void handleBadRequestExceptions() {

        ResponseEntity<ErrorResponse> balance =
                handler.handleInsufficientBalance(new InsufficientBalanceException("Low balance"));

        ResponseEntity<ErrorResponse> stock =
                handler.handleInsufficientStock(new InsufficientStockException("Low stock"));

        ResponseEntity<ErrorResponse> cancel =
                handler.handleOrderCancellation(new OrderCancellationException("Cancel error"));

        ResponseEntity<ErrorResponse> market =
                handler.handleMarketClosedException(new MarketClosedException("Closed"));

        assertEquals(400, balance.getStatusCodeValue());
        assertEquals(400, stock.getStatusCodeValue());
        assertEquals(400, cancel.getStatusCodeValue());
        assertEquals(400, market.getStatusCodeValue());
    }

    @Test
    void handleUnauthorized() {

        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorized(new UnauthorizedActionException("Forbidden"));

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void handleUserAlreadyExists() {

        ResponseEntity<ErrorResponse> response =
                handler.handleUserAlreadyExists(new UserAlreadyExistsException("Exists"));

        assertEquals(409, response.getStatusCodeValue());
    }

    @Test
    void handleInvalidCredentials() {

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid"));

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void errorResponse_contentValidation() {

        String message = "Test error";

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(new UserNotFoundException(message));

        ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(message, body.getMessage());
        assertEquals(404, body.getStatus());
        assertNotNull(body.getTimestamp());
    }
}