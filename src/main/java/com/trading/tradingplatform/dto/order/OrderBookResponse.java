package com.trading.tradingplatform.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderBookResponse {

    private String symbol;

    private List<OrderResponse> buyOrders;

    private List<OrderResponse> sellOrders;

}