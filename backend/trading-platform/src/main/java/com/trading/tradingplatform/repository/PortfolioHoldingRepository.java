package com.trading.tradingplatform.repository;

import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

    List<PortfolioHolding> findByUserId(Long userId);

    Optional<PortfolioHolding> findByUserIdAndStockSymbol(Long userId, String symbol);

//    List<PortfolioHolding> findByUser(User user);
}