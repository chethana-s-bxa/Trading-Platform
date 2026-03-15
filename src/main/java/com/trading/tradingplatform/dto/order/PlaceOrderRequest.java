package com.trading.tradingplatform.dto.order;

import com.trading.tradingplatform.entity.enums.OrderCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlaceOrderRequest {

    @NotNull
    private String symbol;

    @NotNull
    private OrderCategory orderCategory;

    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer quantity;

}