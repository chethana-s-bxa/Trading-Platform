package com.trading.tradingplatform.repository;

import com.trading.tradingplatform.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.trading.tradingplatform.entity.enums.TradeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserId(Long userId);

    List<Trade> findByUserIdOrderByTimestampDesc(Long userId);

    Page<Trade> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.totalAmount),0) FROM Trade t WHERE t.user.id = :userId AND t.tradeType = :type")
    BigDecimal getTotalAmountByUserAndType(@Param("userId") Long userId,
                                           @Param("type") TradeType type);

    long countByUserId(Long userId);

    long countByUserIdAndTradeType(Long userId, TradeType tradeType);

    List<Trade> findByUserUsername(String username);

    List<Trade> findByStockSymbolAndUserUsername(String symbol, String username);

    List<Trade> findByStockSymbol(String symbol);
}