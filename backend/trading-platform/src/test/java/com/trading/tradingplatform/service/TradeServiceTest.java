package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.trade.BuyStockRequest;
import com.trading.tradingplatform.dto.trade.SellStockRequest;
import com.trading.tradingplatform.dto.trade.TradeResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.MarketStatus;
import com.trading.tradingplatform.exception.*;
import com.trading.tradingplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Mock
    private TradeHistoryService tradeHistoryService;

    @Mock
    private MarketStatusService marketStatusService;

    @InjectMocks
    private TradeService tradeService;


    @BeforeEach
    void setupSecurityContext() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@example.com", null);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void buyStock_success_newHolding() {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(10000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(BigDecimal.valueOf(100));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // No existing holding
        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.empty());

        // ---------- Act ----------

        TradeResponse response = tradeService.buyStock(request);

        // ---------- Assert ----------

        assertNotNull(response);
        assertEquals("Stock purchased successfully", response.getMessage());
        assertEquals("AAPL", response.getSymbol());
        assertEquals(10, response.getQuantity());

        // Verify balance deducted: 10000 - (100 * 10) = 9000
        assertEquals(BigDecimal.valueOf(9000), user.getBalance());

        // Verify interactions
        verify(userRepository).save(user);
        verify(portfolioHoldingRepository).save(any(PortfolioHolding.class));
        verify(tradeHistoryService).recordBuyTrade(any(), any(), eq(10), any());
    }

    @Test
    void buyStock_insufficientBalance() {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User with LOW balance
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(500)); // NOT enough

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(BigDecimal.valueOf(100)); // total = 1000

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // ---------- Act & Assert ----------

        assertThrows(InsufficientBalanceException.class, () -> {
            tradeService.buyStock(request);
        });

        // Verify NO save operations happened
        verify(userRepository, never()).save(any());
        verify(portfolioHoldingRepository, never()).save(any());
    }

    @Test
    void buyStock_marketClosed() {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        // Market CLOSED
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.CLOSED);

        // ---------- Act & Assert ----------

        assertThrows(MarketClosedException.class, () -> {
            tradeService.buyStock(request);
        });

        // Verify NOTHING else is called
        verify(userRepository, never()).findByEmail(any());
        verify(stockRepository, never()).findBySymbol(any());
        verify(portfolioHoldingRepository, never()).save(any());
    }

    @Test
    void buyStock_existingHolding_weightedAverage() {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(20000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock (new price)
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(BigDecimal.valueOf(100));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // Existing holding
        PortfolioHolding holding = new PortfolioHolding();
        holding.setUser(user);
        holding.setStock(stock);
        holding.setQuantity(10); // old qty
        holding.setAveragePrice(BigDecimal.valueOf(80)); // old price

        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        // ---------- Act ----------

        TradeResponse response = tradeService.buyStock(request);

        // ---------- Assert ----------

        assertNotNull(response);

        // New quantity = 10 + 10 = 20
        assertEquals(20, holding.getQuantity());

        // Weighted avg = (80*10 + 100*10) / 20 = 90
        assertEquals(0, holding.getAveragePrice().compareTo(BigDecimal.valueOf(90)));

        // Verify save called
        verify(portfolioHoldingRepository).save(holding);
    }

    @Test
    void sellStock_success_partialSell() {

        // ---------- Arrange ----------

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(1000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(BigDecimal.valueOf(100));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // Existing holding (user owns 10 shares)
        PortfolioHolding holding = new PortfolioHolding();
        holding.setUser(user);
        holding.setStock(stock);
        holding.setQuantity(10);

        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        // ---------- Act ----------

        TradeResponse response = tradeService.sellStock(request);

        // ---------- Assert ----------

        assertNotNull(response);
        assertEquals("Stock sold successfully", response.getMessage());

        // Remaining quantity = 10 - 5 = 5
        assertEquals(5, holding.getQuantity());

        // Balance = 1000 + (100 * 5) = 1500
        assertEquals(BigDecimal.valueOf(1500), user.getBalance());

        // Verify save operations
        verify(portfolioHoldingRepository).save(holding);
        verify(userRepository).save(user);
        verify(tradeHistoryService).recordSellTrade(any(), any(), eq(5), any());
    }

    @Test
    void sellStock_fullSell_deleteHolding() {

        // ---------- Arrange ----------

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(1000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setPrice(BigDecimal.valueOf(100));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // Holding = exactly 10 shares
        PortfolioHolding holding = new PortfolioHolding();
        holding.setUser(user);
        holding.setStock(stock);
        holding.setQuantity(10);

        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        // ---------- Act ----------

        TradeResponse response = tradeService.sellStock(request);

        // ---------- Assert ----------

        assertNotNull(response);
        assertEquals("Stock sold successfully", response.getMessage());

        // Balance = 1000 + (100 * 10) = 2000
        assertEquals(BigDecimal.valueOf(2000), user.getBalance());

        // Verify DELETE called instead of save
        verify(portfolioHoldingRepository).delete(holding);

        // Ensure save NOT called for holding
        verify(portfolioHoldingRepository, never()).save(any());

        verify(userRepository).save(user);
    }

    @Test
    void sellStock_insufficientShares() {

        // ---------- Arrange ----------

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10); // trying to sell more

        // Market OPEN
        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setBalance(BigDecimal.valueOf(1000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        // Holding = only 5 shares
        PortfolioHolding holding = new PortfolioHolding();
        holding.setUser(user);
        holding.setStock(stock);
        holding.setQuantity(5);

        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.of(holding));

        // ---------- Act & Assert ----------

        assertThrows(InsufficientStockException.class, () -> {
            tradeService.sellStock(request);
        });

        // Ensure nothing is saved/deleted
        verify(portfolioHoldingRepository, never()).save(any());
        verify(portfolioHoldingRepository, never()).delete(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void sellStock_marketClosed() {

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.CLOSED);

        assertThrows(MarketClosedException.class, () -> {
            tradeService.sellStock(request);
        });

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void sellStock_stockNotFound() {

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> {
            tradeService.sellStock(request);
        });
    }

    @Test
    void sellStock_holdingNotFound() {

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);

        when(marketStatusService.getMarketStatus()).thenReturn(MarketStatus.OPEN);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        Stock stock = new Stock();
        stock.setSymbol("AAPL");

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(stock));

        when(portfolioHoldingRepository.findByUserIdAndStockSymbol(1L, "AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(PortfolioStockNotFoundException.class, () -> {
            tradeService.sellStock(request);
        });
    }
}