package com.trading.tradingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO used to return the portfolio valuation summary
 * for a user including investment, current value and profit/loss.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioValueResponse {

    private BigDecimal totalInvestment;
    private BigDecimal currentValue;

    //      Profit or loss = currentValue - totalInvestment
    private BigDecimal profitLoss;
}