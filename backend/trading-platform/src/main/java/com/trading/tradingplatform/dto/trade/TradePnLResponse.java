package com.trading.tradingplatform.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TradePnLResponse {

    private BigDecimal totalInvested;

    private BigDecimal totalSold;

    private BigDecimal netProfitOrLoss;

}