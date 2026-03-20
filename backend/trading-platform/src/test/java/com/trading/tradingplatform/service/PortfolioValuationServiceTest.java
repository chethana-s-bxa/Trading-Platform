package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.PortfolioValueResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioValuationServiceTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PortfolioValuationService service;

    @Test
    void calculatePortfolioValue_multipleHoldings() {

        Long userId = 1L;

        Stock stock1 = new Stock();
        stock1.setPrice(new BigDecimal("150"));

        PortfolioHolding h1 = new PortfolioHolding();
        h1.setQuantity(10);
        h1.setAveragePrice(new BigDecimal("100"));
        h1.setStock(stock1);

        Stock stock2 = new Stock();
        stock2.setPrice(new BigDecimal("50"));

        PortfolioHolding h2 = new PortfolioHolding();
        h2.setQuantity(20);
        h2.setAveragePrice(new BigDecimal("40"));
        h2.setStock(stock2);

        when(portfolioService.getUserPortfolio(userId))
                .thenReturn(List.of(h1, h2));

        PortfolioValueResponse result =
                service.calculatePortfolioValue(userId);

        // Investment = (10*100) + (20*40) = 1000 + 800 = 1800
        assertEquals(new BigDecimal("1800"), result.getTotalInvestment());

        // Current = (10*150) + (20*50) = 1500 + 1000 = 2500
        assertEquals(new BigDecimal("2500"), result.getCurrentValue());

        // Profit = 700
        assertEquals(new BigDecimal("700"), result.getProfitLoss());
    }

    @Test
    void calculatePortfolioValue_emptyPortfolio() {

        Long userId = 1L;

        when(portfolioService.getUserPortfolio(userId))
                .thenReturn(List.of());

        PortfolioValueResponse result =
                service.calculatePortfolioValue(userId);

        assertEquals(BigDecimal.ZERO, result.getTotalInvestment());
        assertEquals(BigDecimal.ZERO, result.getCurrentValue());
        assertEquals(BigDecimal.ZERO, result.getProfitLoss());
    }

    @Test
    void calculatePortfolioValue_lossScenario() {

        Long userId = 1L;

        Stock stock = new Stock();
        stock.setPrice(new BigDecimal("50"));

        PortfolioHolding holding = new PortfolioHolding();
        holding.setQuantity(10);
        holding.setAveragePrice(new BigDecimal("100"));
        holding.setStock(stock);

        when(portfolioService.getUserPortfolio(userId))
                .thenReturn(List.of(holding));

        PortfolioValueResponse result =
                service.calculatePortfolioValue(userId);

        assertEquals(new BigDecimal("1000"), result.getTotalInvestment());
        assertEquals(new BigDecimal("500"), result.getCurrentValue());
        assertEquals(new BigDecimal("-500"), result.getProfitLoss());
    }

    @Test
    void broadcastPortfolioValue_success() {

        Long userId = 1L;

        PortfolioValueResponse mockResponse = PortfolioValueResponse.builder()
                .totalInvestment(BigDecimal.TEN)
                .currentValue(BigDecimal.TEN)
                .profitLoss(BigDecimal.ZERO)
                .build();

        PortfolioValuationService spyService = Mockito.spy(service);

        doReturn(mockResponse)
                .when(spyService)
                .calculatePortfolioValue(userId);

        spyService.broadcastPortfolioValue(userId);

        verify(messagingTemplate).convertAndSend(
                "/topic/portfolio/" + userId,
                mockResponse
        );
    }
}