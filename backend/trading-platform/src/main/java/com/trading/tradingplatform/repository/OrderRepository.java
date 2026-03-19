package com.trading.tradingplatform.repository;

import com.trading.tradingplatform.entity.Order;
import com.trading.tradingplatform.entity.enums.OrderStatus;
import com.trading.tradingplatform.entity.enums.TradeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceDesc(
            String symbol,
            TradeType tradeType,
            OrderStatus orderStatus
    );

    List<Order> findByStockSymbolAndTradeTypeAndOrderStatusOrderByPriceAsc(
            String symbol,
            TradeType tradeType,
            OrderStatus orderStatus
    );

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStockSymbolAndOrderStatusOrderByPriceDesc(
            String symbol,
            OrderStatus status
    );

    List<Order> findByStockSymbolAndTradeTypeAndOrderStatus(
            String symbol,
            TradeType tradeType,
            OrderStatus orderStatus
    );
}