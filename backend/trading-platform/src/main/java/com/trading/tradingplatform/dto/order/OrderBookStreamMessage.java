package com.trading.tradingplatform.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO used for broadcasting order book updates
 * through WebSocket.
 *
 * This message contains the current bid and ask
 * levels for a particular stock symbol.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookStreamMessage {

    private String symbol;
    private List<OrderLevel> bids;
    private List<OrderLevel> asks;

    @Data
    @AllArgsConstructor
    public static class OrderLevel {

        private BigDecimal price;

        private Integer quantity;
    }
}