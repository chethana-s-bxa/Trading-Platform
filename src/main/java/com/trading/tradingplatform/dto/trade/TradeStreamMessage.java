package com.trading.tradingplatform.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO used for broadcasting executed trades through WebSocket.
 *
 * This message is sent whenever the matching engine executes
 * a trade between a buyer and a seller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStreamMessage {


    private String symbol;
    private BigDecimal price;
    private Integer quantity;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime timestamp;
}