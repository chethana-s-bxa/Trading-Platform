package com.trading.tradingplatform.dto.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO used for broadcasting real-time stock price updates
 * through WebSocket topics.
 *
 * This message is sent whenever a stock price changes
 * due to market simulation.
 *
 * Example message:
 *
 * {
 *   "symbol": "TCS",
 *   "price": 3512.45,
 *   "timestamp": "2026-03-20T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceUpdateMessage {

    private String symbol;
    private BigDecimal price;
    private LocalDateTime timestamp;
}