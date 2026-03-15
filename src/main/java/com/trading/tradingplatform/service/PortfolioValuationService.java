package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.PortfolioValueResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of PortfolioValuationService responsible
 * for computing real-time portfolio value using latest market prices.
 */
@Service
@RequiredArgsConstructor
public class PortfolioValuationService {

    private final PortfolioService portfolioService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Calculates portfolio value using current stock prices.
     */
    public PortfolioValueResponse calculatePortfolioValue(Long userId) {

        List<PortfolioHolding> holdings =
                portfolioService.getUserPortfolio(userId);

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (PortfolioHolding holding : holdings) {

            BigDecimal quantity =
                    BigDecimal.valueOf(holding.getQuantity());

            BigDecimal avgPrice =
                    holding.getAveragePrice();

            BigDecimal currentPrice =
                    holding.getStock().getPrice();

            /**
             * Investment = quantity × average buy price
             */
            BigDecimal investment =
                    avgPrice.multiply(quantity);

            /**
             * Current value = quantity × current market price
             */
            BigDecimal value =
                    currentPrice.multiply(quantity);

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(value);
        }

        /**
         * Profit or loss = current value − total investment
         */
        BigDecimal profitLoss =
                currentValue.subtract(totalInvestment);

        return PortfolioValueResponse.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .build();
    }

    /**
     * Sends real-time portfolio valuation updates
     * to the user via WebSocket.
     *
     * @param userId user id
     */
    public void broadcastPortfolioValue(Long userId) {

        PortfolioValueResponse value =
                calculatePortfolioValue(userId);

        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + userId,
                value
        );
    }
}
