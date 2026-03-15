package com.trading.tradingplatform.dto.trade;

import com.trading.tradingplatform.entity.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TradeHistoryResponse {

    private String symbol;

    private TradeType tradeType;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private LocalDateTime timestamp;

}