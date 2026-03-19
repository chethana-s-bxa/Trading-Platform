package com.trading.tradingplatform.repository;

import com.trading.tradingplatform.entity.Stock;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findBySymbol(String symbol);

    Page<Stock> findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String symbol,
            String companyName,
            Pageable pageable
    );

    Page<Stock> findByPriceBetween(
            Double minPrice,
            Double maxPrice,
            Pageable pageable
    );

    boolean existsBySymbol(@NotNull(message = "Symbol cannot be null") String symbol);
}
