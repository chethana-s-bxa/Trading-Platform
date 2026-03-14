package com.trading.tradingplatform.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TradeResponse {

    private String message;
    private String symbol;
    private int quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;

}