package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.Trade;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.TradeRepository;
import com.trading.tradingplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private StockRepository stockRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PortfolioHoldingRepository portfolioRepository;

    @InjectMocks
    private AdminController controller;

    @Test
    void createStock_alreadyExists() {

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");

        when(stockRepository.existsBySymbol("AAPL")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> controller.createStock(request));
    }

    @Test
    void updateStock_notFound() {

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        StockRequest request = new StockRequest();

        assertThrows(RuntimeException.class,
                () -> controller.updateStock("AAPL", request));
    }

    @Test
    void deleteStock_notFound() {

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> controller.deleteStock("AAPL"));
    }

    @Test
    void getAllTrades_symbolAndUsername() {

        when(tradeRepository
                .findByStockSymbolAndUserUsername("AAPL", "user"))
                .thenReturn(List.of());

        List<Trade> result =
                controller.getAllTrades("AAPL", "user");

        assertNotNull(result);

        verify(tradeRepository)
                .findByStockSymbolAndUserUsername("AAPL", "user");
    }

    @Test
    void getAllTrades_onlySymbol() {

        when(tradeRepository.findByStockSymbol("AAPL"))
                .thenReturn(List.of());

        controller.getAllTrades("AAPL", null);

        verify(tradeRepository).findByStockSymbol("AAPL");
    }

    @Test
    void getAllTrades_onlyUsername() {

        when(tradeRepository.findByUserUsername("user"))
                .thenReturn(List.of());

        controller.getAllTrades(null, "user");

        verify(tradeRepository).findByUserUsername("user");
    }

    @Test
    void getAllTrades_default() {

        when(tradeRepository.findAll()).thenReturn(List.of());

        controller.getAllTrades(null, null);

        verify(tradeRepository).findAll();
    }

    @Test
    void searchTrades_symbol() {

        when(tradeRepository.findByStockSymbol("AAPL"))
                .thenReturn(List.of());

        controller.searchTrades("AAPL", null, null, null);

        verify(tradeRepository).findByStockSymbol("AAPL");
    }

    @Test
    void searchTrades_userId() {

        when(tradeRepository.findByUserId(1L))
                .thenReturn(List.of());

        controller.searchTrades(null, 1L, null, null);

        verify(tradeRepository).findByUserId(1L);
    }

    @Test
    void searchTrades_default() {

        when(tradeRepository.findAll()).thenReturn(List.of());

        controller.searchTrades(null, null, null, null);

        verify(tradeRepository).findAll();
    }
    @Test
    void createStock_success() {

        StockRequest request = new StockRequest();
        request.setSymbol("aapl");
        request.setCompanyName("Apple");
        request.setPrice(BigDecimal.valueOf(100.0));

        when(stockRepository.existsBySymbol("aapl")).thenReturn(false);
        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Stock result = controller.createStock(request);

        assertNotNull(result);
        verify(stockRepository).save(any());
    }

    @Test
    void updateStock_success_withCompanyName() {

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        StockRequest request = new StockRequest();
        request.setPrice(BigDecimal.valueOf(200.0));
        request.setCompanyName("Apple Inc");

        controller.updateStock("AAPL", request);

        verify(stockRepository).save(stock);
    }

    @Test
    void updateStock_success_withoutCompanyName() {

        Stock stock = new Stock();

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        StockRequest request = new StockRequest();
        request.setPrice(BigDecimal.valueOf(200.0));
        request.setCompanyName(null); // IMPORTANT

        controller.updateStock("AAPL", request);

        verify(stockRepository).save(stock);
    }

    @Test
    void deleteStock_success() {

        Stock stock = new Stock();

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        controller.deleteStock("AAPL");

        verify(stockRepository).delete(stock);
    }

    @Test
    void getAllUsers_success() {

        when(userRepository.findAll()).thenReturn(List.of());

        controller.getAllUsers();

        verify(userRepository).findAll();
    }

    @Test
    void getUserPortfolio_success() {

        when(portfolioRepository.findByUserId(1L))
                .thenReturn(List.of());

        controller.getUserPortfolio(1L);

        verify(portfolioRepository).findByUserId(1L);
    }
}