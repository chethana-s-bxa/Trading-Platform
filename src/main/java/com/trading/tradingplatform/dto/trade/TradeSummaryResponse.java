package com.trading.tradingplatform.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TradeSummaryResponse {

    private long totalTrades;

    private long totalBuyTrades;

    private long totalSellTrades;

    private BigDecimal totalInvested;

    private BigDecimal totalSold;

    private BigDecimal netProfitOrLoss;

}