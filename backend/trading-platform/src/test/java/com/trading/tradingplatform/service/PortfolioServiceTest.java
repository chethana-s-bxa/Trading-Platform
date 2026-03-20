package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.exception.StockNotFoundException;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioHoldingRepository repository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void addStockToPortfolio_existingHolding_updatesAverage() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        PortfolioHolding existing = new PortfolioHolding();
        existing.setQuantity(10);
        existing.setAveragePrice(new BigDecimal("100"));

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(existing));

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PortfolioHolding result = portfolioService.addStockToPortfolio(
                user, stock, 10, new BigDecimal("200")
        );

        assertEquals(20, result.getQuantity());
        assertEquals(new BigDecimal("150.00"), result.getAveragePrice());

        verify(repository).save(existing);
    }

    @Test
    void addStockToPortfolio_newHolding_created() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.empty());

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PortfolioHolding result = portfolioService.addStockToPortfolio(
                user, stock, 5, new BigDecimal("100")
        );

        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("100"), result.getAveragePrice());

        verify(repository).save(any(PortfolioHolding.class));
    }

    @Test
    void updatePortfolioAfterBuy_newHolding() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.empty());

        portfolioService.updatePortfolioAfterBuy(
                user, stock, 5, new BigDecimal("100")
        );

        verify(repository).save(any(PortfolioHolding.class));
    }

    @Test
    void updatePortfolioAfterBuy_existingHolding() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        PortfolioHolding holding = new PortfolioHolding();
        holding.setQuantity(10);
        holding.setAveragePrice(new BigDecimal("100"));

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        portfolioService.updatePortfolioAfterBuy(
                user, stock, 10, new BigDecimal("200")
        );

        assertEquals(20, holding.getQuantity());
        assertEquals(new BigDecimal("150"), holding.getAveragePrice());

        verify(repository).save(holding);
    }

    @Test
    void updatePortfolioAfterSell_deleteHolding() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        PortfolioHolding holding = new PortfolioHolding();
        holding.setQuantity(10);

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        portfolioService.updatePortfolioAfterSell(user, stock, 10);

        verify(repository).delete(holding);
    }

    @Test
    void updatePortfolioAfterSell_reduceQuantity() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        PortfolioHolding holding = new PortfolioHolding();
        holding.setQuantity(10);

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        portfolioService.updatePortfolioAfterSell(user, stock, 5);

        assertEquals(5, holding.getQuantity());

        verify(repository).save(holding);
    }


    @Test
    void updatePortfolioAfterSell_stockNotOwned() {

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(repository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () ->
                portfolioService.updatePortfolioAfterSell(user, stock, 5)
        );
    }


}