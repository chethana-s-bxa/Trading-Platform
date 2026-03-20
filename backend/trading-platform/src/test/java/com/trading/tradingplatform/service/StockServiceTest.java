package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.exception.*;
import com.trading.tradingplatform.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void addStock_nullSymbol() {

        StockRequest request = new StockRequest();
        request.setSymbol(null);

        assertThrows(InvalidStockException.class,
                () -> stockService.addStock(request));
    }

    @Test
    void addStock_alreadyExists() {

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(new Stock()));

        assertThrows(StockAlreadyExistsException.class,
                () -> stockService.addStock(request));
    }

    @Test
    void updateStock_onlyCompanyName() {

        Stock existing = new Stock();
        existing.setSymbol("AAPL");
        existing.setCompanyName("Old");
        existing.setPrice(new BigDecimal("100"));

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");
        request.setCompanyName("New");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(existing));

        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Stock result = stockService.updateStock(request);

        assertEquals("New", result.getCompanyName());
        assertEquals(new BigDecimal("100"), result.getPrice());
    }

    @Test
    void updateStock_onlyPrice() {

        Stock existing = new Stock();
        existing.setSymbol("AAPL");
        existing.setCompanyName("ABC");
        existing.setPrice(new BigDecimal("100"));

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");
        request.setPrice(new BigDecimal("200"));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(existing));

        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Stock result = stockService.updateStock(request);

        assertEquals(new BigDecimal("200"), result.getPrice());
        assertEquals("ABC", result.getCompanyName());
    }

    @Test
    void deleteStock_success() {

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        stockService.deleteStockBySymbol("AAPL");

        verify(stockRepository).deleteById(1L);
    }

    @Test
    void searchStocks_queryBranch() {

        when(stockRepository
                .findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
                        any(), any(), any()))
                .thenReturn(Page.empty());

        Page<Stock> result = stockService.searchStocks(
                "AAPL", null, null, 0, 10
        );

        assertNotNull(result);

        verify(stockRepository)
                .findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
                        eq("AAPL"), eq("AAPL"), any());
    }

    @Test
    void searchStocks_priceRangeBranch() {

        when(stockRepository.findByPriceBetween(anyDouble(), anyDouble(), any()))
                .thenReturn(Page.empty());

        Page<Stock> result = stockService.searchStocks(
                null, 100.0, 200.0, 0, 10
        );

        assertNotNull(result);

        verify(stockRepository)
                .findByPriceBetween(eq(100.0), eq(200.0), any());
    }

    @Test
    void searchStocks_defaultBranch() {

        when(stockRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Stock> result = stockService.searchStocks(
                null, null, null, 0, 10
        );

        assertNotNull(result);

        verify(stockRepository).findAll(any(Pageable.class));
    }

    @Test
    void getStockBySymbol_notFound() {

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> stockService.getStockBySymbol("AAPL"));
    }

    @Test
    void getAllStocks_success() {

        when(stockRepository.findAll()).thenReturn(List.of(new Stock()));

        List<Stock> result = stockService.getAllStocks();

        assertFalse(result.isEmpty());
    }

    @Test
    void getStockBySymbol_success() {

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        Stock result = stockService.getStockBySymbol("AAPL");

        assertEquals("AAPL", result.getSymbol());
    }

    @Test
    void addStock_success() {

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");
        request.setCompanyName("Apple");
        request.setPrice(new BigDecimal("100"));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Stock result = stockService.addStock(request);

        assertEquals("AAPL", result.getSymbol());
    }

    @Test
    void updateStock_noChanges() {

        Stock existing = new Stock();
        existing.setSymbol("AAPL");
        existing.setCompanyName("Old");
        existing.setPrice(new BigDecimal("100"));

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(existing));

        when(stockRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Stock result = stockService.updateStock(request);

        assertEquals("Old", result.getCompanyName());
        assertEquals(new BigDecimal("100"), result.getPrice());
    }

    @Test
    void updateStock_notFound() {

        StockRequest request = new StockRequest();
        request.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class,
                () -> stockService.updateStock(request));
    }

    @Test
    void deleteStock_notFound() {

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class,
                () -> stockService.deleteStockBySymbol("AAPL"));
    }

    @Test
    void searchStocks_blankQuery_goesToDefault() {

        when(stockRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Stock> result = stockService.searchStocks(
                "", null, null, 0, 10
        );

        assertNotNull(result);

        verify(stockRepository).findAll(any(Pageable.class));
    }


}