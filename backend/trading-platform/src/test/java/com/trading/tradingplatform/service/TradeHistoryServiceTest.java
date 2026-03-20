package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.trade.TradeHistoryResponse;
import com.trading.tradingplatform.dto.trade.TradePnLResponse;
import com.trading.tradingplatform.dto.trade.TradeSummaryResponse;
import com.trading.tradingplatform.entity.*;
import com.trading.tradingplatform.entity.enums.TradeType;
import com.trading.tradingplatform.repository.TradeRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeHistoryServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private MarketDataPublisher marketDataPublisher;

    @Mock
    private PortfolioValuationService portfolioValuationService;

    @InjectMocks
    private TradeHistoryService tradeHistoryService;

    @Test
    void recordBuyTrade_success() {

        // ---------- Arrange ----------

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        BigDecimal price = BigDecimal.valueOf(100);
        int quantity = 10;

        // ---------- Act ----------

        tradeHistoryService.recordBuyTrade(user, stock, quantity, price);

        // ---------- Assert ----------

        verify(tradeRepository, times(1)).save(any(Trade.class));
    }

    @Test
    void recordSellTrade_success() {

        // ---------- Arrange ----------

        User user = new User();
        user.setId(2L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        BigDecimal price = BigDecimal.valueOf(150);
        int quantity = 5;

        // ---------- Act ----------

        tradeHistoryService.recordSellTrade(user, stock, quantity, price);

        // ---------- Assert ----------

        verify(tradeRepository, times(1)).save(any(Trade.class));
    }

    @Test
    void getUserTradeHistory_success() {

        // ---------- Arrange ----------

        Trade trade = new Trade();
        trade.setId(1L);

        when(tradeRepository.findByUserIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(trade));

        // ---------- Act ----------

        List<Trade> result = tradeHistoryService.getUserTradeHistory(1L);

        // ---------- Assert ----------

        assertEquals(1, result.size());
    }

    @Test
    void getUserTradeHistoryResponse_success() {

        // ---------- Arrange ----------

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Trade trade = new Trade();
        trade.setStock(stock);
        trade.setTradeType(TradeType.BUY);
        trade.setQuantity(10);
        trade.setPricePerShare(BigDecimal.valueOf(100));
        trade.setTotalAmount(BigDecimal.valueOf(1000));
        trade.setTimestamp(LocalDateTime.now());

        when(tradeRepository.findByUserIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(trade));

        // ---------- Act ----------

        List<TradeHistoryResponse> result =
                tradeHistoryService.getUserTradeHistoryResponse(1L);

        // ---------- Assert ----------

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals(TradeType.BUY, result.get(0).getTradeType());
    }

    @Test
    void getTradeHistory_success() {

        // ---------- Arrange ----------

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        Trade trade = new Trade();
        trade.setStock(stock);
        trade.setTradeType(TradeType.SELL);
        trade.setQuantity(5);
        trade.setPricePerShare(BigDecimal.valueOf(200));
        trade.setTotalAmount(BigDecimal.valueOf(1000));
        trade.setTimestamp(LocalDateTime.now());

        Page<Trade> tradePage =
                new PageImpl<>(List.of(trade));

        when(tradeRepository.findByUserIdOrderByTimestampDesc(eq(1L), any(Pageable.class)))
                .thenReturn(tradePage);

        // ---------- Act ----------

        Page<TradeHistoryResponse> result =
                tradeHistoryService.getTradeHistory(1L, Pageable.unpaged());

        // ---------- Assert ----------

        assertEquals(1, result.getContent().size());
        assertEquals("AAPL", result.getContent().get(0).getSymbol());
    }

    @Test
    void getUserPnL_success() {

        // ---------- Arrange ----------

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.BUY))
                .thenReturn(BigDecimal.valueOf(1000));

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.SELL))
                .thenReturn(BigDecimal.valueOf(1500));

        // ---------- Act ----------

        TradePnLResponse response = tradeHistoryService.getUserPnL(1L);

        // ---------- Assert ----------

        assertEquals(BigDecimal.valueOf(1000), response.getTotalInvested());
        assertEquals(BigDecimal.valueOf(1500), response.getTotalSold());
        assertEquals(BigDecimal.valueOf(500), response.getNetProfitOrLoss());
    }

    @Test
    void getUserPnL_lossScenario() {

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.BUY))
                .thenReturn(BigDecimal.valueOf(2000));

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.SELL))
                .thenReturn(BigDecimal.valueOf(1500));

        TradePnLResponse response = tradeHistoryService.getUserPnL(1L);

        assertEquals(BigDecimal.valueOf(-500), response.getNetProfitOrLoss());
    }

    @Test
    void getTradeSummary_success() {

        // ---------- Arrange ----------

        when(tradeRepository.countByUserId(1L))
                .thenReturn(5L);

        when(tradeRepository.countByUserIdAndTradeType(1L, TradeType.BUY))
                .thenReturn(3L);

        when(tradeRepository.countByUserIdAndTradeType(1L, TradeType.SELL))
                .thenReturn(2L);

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.BUY))
                .thenReturn(BigDecimal.valueOf(1000));

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.SELL))
                .thenReturn(BigDecimal.valueOf(1500));

        // ---------- Act ----------

        TradeSummaryResponse response =
                tradeHistoryService.getTradeSummary(1L);

        // ---------- Assert ----------

        assertEquals(5, response.getTotalTrades());
        assertEquals(3, response.getTotalBuyTrades());
        assertEquals(2, response.getTotalSellTrades());
        assertEquals(BigDecimal.valueOf(1000), response.getTotalInvested());
        assertEquals(BigDecimal.valueOf(1500), response.getTotalSold());
        assertEquals(BigDecimal.valueOf(500), response.getNetProfitOrLoss());
    }

    @Test
    void getTradeSummary_lossScenario() {

        when(tradeRepository.countByUserId(1L)).thenReturn(2L);
        when(tradeRepository.countByUserIdAndTradeType(1L, TradeType.BUY)).thenReturn(1L);
        when(tradeRepository.countByUserIdAndTradeType(1L, TradeType.SELL)).thenReturn(1L);

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.BUY))
                .thenReturn(BigDecimal.valueOf(2000));

        when(tradeRepository.getTotalAmountByUserAndType(1L, TradeType.SELL))
                .thenReturn(BigDecimal.valueOf(1500));

        TradeSummaryResponse response =
                tradeHistoryService.getTradeSummary(1L);

        assertEquals(BigDecimal.valueOf(-500), response.getNetProfitOrLoss());
    }

    @Test
    void recordMatchedTrade_success() {

        // ---------- Arrange ----------

        User buyer = new User();
        buyer.setId(1L);

        User seller = new User();
        seller.setId(2L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        BigDecimal price = BigDecimal.valueOf(100);
        int quantity = 10;

        // ---------- Act ----------

        tradeHistoryService.recordMatchedTrade(
                buyer, seller, stock, quantity, price
        );

        // ---------- Assert ----------

        // 2 trades saved
        verify(tradeRepository, times(2)).save(any(Trade.class));

        // WebSocket broadcast
        verify(marketDataPublisher, times(1))
                .broadcastTrade(any());

        // Portfolio updates
        verify(portfolioValuationService).broadcastPortfolioValue(1L);
        verify(portfolioValuationService).broadcastPortfolioValue(2L);
    }
}