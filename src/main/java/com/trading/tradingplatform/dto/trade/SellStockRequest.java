package com.trading.tradingplatform.dto.trade;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellStockRequest {

    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

}