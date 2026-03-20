package com.trading.tradingplatform.dto.order;

import com.trading.tradingplatform.entity.enums.OrderCategory;
import com.trading.tradingplatform.entity.enums.OrderStatus;
import com.trading.tradingplatform.entity.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;

    private String symbol;

    private TradeType tradeType;

    private OrderCategory orderCategory;

    private BigDecimal price;

    private Integer quantity;

    private Integer remainingQuantity;

    private OrderStatus orderStatus;

    private LocalDateTime createdAt;

}