package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.PortfolioHoldingResponse;
import com.trading.tradingplatform.dto.PortfolioResponse;
import com.trading.tradingplatform.dto.PortfolioValueResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.service.PortfolioService;
import com.trading.tradingplatform.service.PortfolioValuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;
    private final PortfolioValuationService portfolioValuationService;

    /**
     * Returns the portfolio of the currently logged-in user.
     *
     * The user is identified using the Authentication object
     * populated by Spring Security after JWT validation.
     *
     * @param authentication Spring Security authentication object
     * @return PortfolioResponse containing user's holdings
     */
    @GetMapping
    public PortfolioResponse getUserPortfolio(Authentication authentication) {


        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        List<PortfolioHolding> holdings =
                portfolioService.getUserPortfolio(user.getId());

        List<PortfolioHoldingResponse> holdingResponses =
                holdings.stream()
                        .map(h -> PortfolioHoldingResponse.builder()
                                .symbol(h.getStock().getSymbol())
                                .companyName(h.getStock().getCompanyName())
                                .quantity(h.getQuantity())
                                .averagePrice(h.getAveragePrice())
                                .build())
                        .collect(Collectors.toList());


        return PortfolioResponse.builder()
                .userId(user.getId())
                .holdings(holdingResponses)
                .build();
    }

    /**
     * Returns the real-time portfolio valuation
     * including investment, current value and profit/loss.
     */
    @GetMapping("/value")
    public PortfolioValueResponse getPortfolioValue(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return portfolioValuationService.calculatePortfolioValue(user.getId());
    }
}