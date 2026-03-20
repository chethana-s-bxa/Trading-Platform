package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.enums.MarketTrend;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class MarketPriceSimulatorTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MarketDataPublisher marketDataPublisher;

    @InjectMocks
    private MarketPriceSimulator simulator;

    @Test
    void updateMarketPrices_success() {

        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(new BigDecimal("100"));

        when(stockRepository.findAll()).thenReturn(List.of(stock));

        simulator.updateMarketPrices();

        verify(stockRepository).save(any(Stock.class));
        verify(marketDataPublisher).broadcastPriceUpdate(any());
    }

    @Test
    void updateMarketPrices_noStocks() {

        when(stockRepository.findAll()).thenReturn(List.of());

        simulator.updateMarketPrices();

        verify(stockRepository, never()).save(any());
        verify(marketDataPublisher, never()).broadcastPriceUpdate(any());
    }

    @Test
    void updateMarketTrend_bull() throws Exception {

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextInt(3)).thenReturn(0);

        setField(simulator, "random", mockRandom);

        simulator.updateMarketTrend();

        assertEquals(MarketTrend.BULL, getTrend(simulator));
    }

    @Test
    void updateMarketTrend_bear() throws Exception {

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextInt(3)).thenReturn(1);

        setField(simulator, "random", mockRandom);

        simulator.updateMarketTrend();

        assertEquals(MarketTrend.BEAR, getTrend(simulator));
    }

    @Test
    void updateMarketTrend_neutral() throws Exception {

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextInt(3)).thenReturn(2);

        setField(simulator, "random", mockRandom);

        simulator.updateMarketTrend();

        assertEquals(MarketTrend.NEUTRAL, getTrend(simulator));
    }

    @Test
    void generatePriceChangePercent_bull() throws Exception {

        setField(simulator, "currentTrend", MarketTrend.BULL);

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextDouble()).thenReturn(0.5);

        setField(simulator, "random", mockRandom);

        BigDecimal result = invokeGenerate(simulator);

        assertTrue(result.doubleValue() >= 0); // positive change
    }

    @Test
    void generatePriceChangePercent_bear() throws Exception {

        setField(simulator, "currentTrend", MarketTrend.BEAR);

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextDouble()).thenReturn(0.5);

        setField(simulator, "random", mockRandom);

        BigDecimal result = invokeGenerate(simulator);

        assertTrue(result.doubleValue() <= 0); // negative change
    }

    @Test
    void generatePriceChangePercent_neutral() throws Exception {

        setField(simulator, "currentTrend", MarketTrend.NEUTRAL);

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextDouble()).thenReturn(0.5);

        setField(simulator, "random", mockRandom);

        BigDecimal result = invokeGenerate(simulator);

        assertNotNull(result);
    }

    private BigDecimal invokeGenerate(Object target) throws Exception {
        var method = target.getClass().getDeclaredMethod("generatePriceChangePercent");
        method.setAccessible(true);
        return (BigDecimal) method.invoke(target);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private MarketTrend getTrend(Object target) throws Exception {
        var field = target.getClass().getDeclaredField("currentTrend");
        field.setAccessible(true);
        return (MarketTrend) field.get(target);
    }


}