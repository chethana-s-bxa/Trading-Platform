package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.PortfolioResponse;
import com.trading.tradingplatform.dto.PortfolioValueResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.service.PortfolioService;
import com.trading.tradingplatform.service.PortfolioValuationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    @Mock private PortfolioService portfolioService;
    @Mock private UserRepository userRepository;
    @Mock private PortfolioValuationService valuationService;
    @Mock private Authentication authentication;

    @InjectMocks
    private PortfolioController controller;

    @Test
    void getUserPortfolio_success() {

        String email = "test@gmail.com";

        User user = new User();
        user.setId(1L);

        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setCompanyName("Apple");

        PortfolioHolding holding = new PortfolioHolding();
        holding.setStock(stock);
        holding.setQuantity(10);
        holding.setAveragePrice(new BigDecimal("100"));

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(portfolioService.getUserPortfolio(1L))
                .thenReturn(List.of(holding));

        PortfolioResponse result =
                controller.getUserPortfolio(authentication);

        assertEquals(1L, result.getUserId());
        assertEquals(1, result.getHoldings().size());
        assertEquals("AAPL", result.getHoldings().get(0).getSymbol());

        verify(portfolioService).getUserPortfolio(1L);
    }

    @Test
    void getUserPortfolio_userNotFound() {

        when(authentication.getName()).thenReturn("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> controller.getUserPortfolio(authentication));
    }

    @Test
    void getPortfolioValue_success() {

        String email = "test@gmail.com";

        User user = new User();
        user.setId(1L);

        PortfolioValueResponse response = PortfolioValueResponse.builder()
                .build();

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(valuationService.calculatePortfolioValue(1L))
                .thenReturn(response);

        PortfolioValueResponse result =
                controller.getPortfolioValue(authentication);

        assertNotNull(result);

        verify(valuationService).calculatePortfolioValue(1L);
    }

    @Test
    void getPortfolioValue_userNotFound() {

        when(authentication.getName()).thenReturn("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> controller.getPortfolioValue(authentication));
    }


}