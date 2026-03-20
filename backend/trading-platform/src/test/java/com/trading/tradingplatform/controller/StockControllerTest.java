package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController controller;

    @Test
    void getAllStocks_success() {

        when(stockService.getAllStocks()).thenReturn(List.of(new Stock()));

        ResponseEntity<List<Stock>> response = controller.getAllStocks();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());

        verify(stockService).getAllStocks();
    }

    @Test
    void getStockBySymbol_success() {

        Stock stock = new Stock();

        when(stockService.getStockBySymbol("AAPL")).thenReturn(stock);

        ResponseEntity<Stock> response = controller.getStockBySymbol("AAPL");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(stock, response.getBody());
    }

    @Test
    void addStock_success() {

        StockRequest request = new StockRequest();
        Stock stock = new Stock();

        when(stockService.addStock(request)).thenReturn(stock);

        ResponseEntity<Stock> response = controller.addStock(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(stock, response.getBody());
    }

    @Test
    void updateStock_success() {

        StockRequest request = new StockRequest();
        Stock stock = new Stock();

        when(stockService.updateStock(request)).thenReturn(stock);

        ResponseEntity<Stock> response = controller.updateStock(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(stock, response.getBody());
    }

    @Test
    void deleteStock_success() {

        ResponseEntity<String> response = controller.deleteStock("AAPL");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Stock deleted successfully", response.getBody());

        verify(stockService).deleteStockBySymbol("AAPL");
    }

    @Test
    void searchStocks_success() {

        when(stockService.searchStocks(null, null, null, 0, 10))
                .thenReturn(Page.empty());

        Page<Stock> result = controller.searchStocks(
                null, null, null, 0, 10
        );

        assertNotNull(result);

        verify(stockService)
                .searchStocks(null, null, null, 0, 10);
    }


}