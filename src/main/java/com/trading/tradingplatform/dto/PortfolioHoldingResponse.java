package com.trading.tradingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioHoldingResponse {

    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averagePrice;

}