package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.exception.StockNotFoundException;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioHoldingRepository portfolioHoldingRepository;

    /**
     * Retrieves all portfolio holdings belonging to a specific user.
     *
     * @param userId the ID of the user whose portfolio is requested
     * @return list of PortfolioHolding objects representing the user's holdings
     */
    public List<PortfolioHolding> getUserPortfolio(Long userId) {
        return portfolioHoldingRepository.findByUserId(userId);
    }

    /**
     * Adds stock to the user's portfolio after a BUY transaction.
     * if quantity is increased and if the user does not own the stock
     * a new PortfolioHolding record is created
     *
     * @param user the user buying the stock
     * @param stock the stock being purchased
     * @param quantity number of shares purchased
     * @param buyPrice price at which the shares were purchased
     * @return updated or newly created PortfolioHolding
     */
    public PortfolioHolding addStockToPortfolio(User user, Stock stock, Integer quantity, BigDecimal buyPrice) {

        Optional<PortfolioHolding> existingHolding =
                portfolioHoldingRepository.findByUserIdAndStockSymbol(user.getId(), stock.getSymbol());

        if (existingHolding.isPresent()) {

            PortfolioHolding holding = existingHolding.get();

            Integer oldQuantity = holding.getQuantity();
            BigDecimal oldAvgPrice = holding.getAveragePrice();

            Integer newQuantity = oldQuantity + quantity;

            BigDecimal newAveragePrice = calculateAveragePrice(
                    oldQuantity,
                    oldAvgPrice,
                    quantity,
                    buyPrice
            );

            holding.setQuantity(newQuantity);
            holding.setAveragePrice(newAveragePrice);

            return portfolioHoldingRepository.save(holding);
        }

        PortfolioHolding newHolding = PortfolioHolding.builder()
                .user(user)
                .stock(stock)
                .quantity(quantity)
                .averagePrice(buyPrice)
                .build();

        return portfolioHoldingRepository.save(newHolding);
    }

    /**
     * Calculates the weighted average price of a stock holding.
     *
     * @param oldQuantity number of shares already owned
     * @param oldPrice average price of existing shares
     * @param newQuantity number of newly purchased shares
     * @param newPrice purchase price of new shares
     * @return new weighted average price
     */
    private BigDecimal calculateAveragePrice(
            Integer oldQuantity,
            BigDecimal oldPrice,
            Integer newQuantity,
            BigDecimal newPrice) {

        BigDecimal oldTotal = oldPrice.multiply(BigDecimal.valueOf(oldQuantity));
        BigDecimal newTotal = newPrice.multiply(BigDecimal.valueOf(newQuantity));

        BigDecimal totalCost = oldTotal.add(newTotal);

        Integer totalShares = oldQuantity + newQuantity;

        return totalCost.divide(BigDecimal.valueOf(totalShares), 2, BigDecimal.ROUND_HALF_UP);
    }

    public void updatePortfolioAfterBuy(
            User user,
            Stock stock,
            Integer quantity,
            BigDecimal price
    ) {

        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElse(null);

        if (holding == null) {

            holding = PortfolioHolding.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(quantity)
                    .averagePrice(price)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

        } else {

            int newQuantity = holding.getQuantity() + quantity;

            BigDecimal totalCost =
                    holding.getAveragePrice()
                            .multiply(BigDecimal.valueOf(holding.getQuantity()))
                            .add(price.multiply(BigDecimal.valueOf(quantity)));

            BigDecimal newAveragePrice =
                    totalCost.divide(BigDecimal.valueOf(newQuantity), RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setAveragePrice(newAveragePrice);
            holding.setUpdatedAt(LocalDateTime.now());
        }

        portfolioHoldingRepository.save(holding);
    }

    public void updatePortfolioAfterSell(
            User user,
            Stock stock,
            Integer quantity
    ) {

        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not owned"));

        int newQuantity = holding.getQuantity() - quantity;

        if (newQuantity == 0) {
            portfolioHoldingRepository.delete(holding);
        } else {
            holding.setQuantity(newQuantity);
            holding.setUpdatedAt(LocalDateTime.now());
            portfolioHoldingRepository.save(holding);
        }
    }
}