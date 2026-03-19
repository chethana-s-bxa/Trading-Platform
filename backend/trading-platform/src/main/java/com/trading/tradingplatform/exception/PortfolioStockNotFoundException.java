package com.trading.tradingplatform.exception;

public class PortfolioStockNotFoundException extends RuntimeException {

    public PortfolioStockNotFoundException(String message) {
        super(message);
    }
}