package com.trading.tradingplatform.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderBookResponse {

    private String symbol;

    private List<OrderResponse> buyOrders;

    private List<OrderResponse> sellOrders;

}