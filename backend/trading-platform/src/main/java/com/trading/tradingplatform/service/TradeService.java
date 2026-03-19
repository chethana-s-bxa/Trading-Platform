package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.trade.BuyStockRequest;
import com.trading.tradingplatform.dto.trade.SellStockRequest;
import com.trading.tradingplatform.dto.trade.TradeResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.MarketStatus;
import com.trading.tradingplatform.exception.*;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final TradeHistoryService tradeHistoryService;
    private final MarketStatusService marketStatusService;
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    @Transactional
    public TradeResponse buyStock(BuyStockRequest request) {

        validateMarketOpen();

        //  Get authenticated user
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        logger.info("Initiating BUY trade for user email: {}, stock: {}, quantity: {}", email, request.getSymbol(), request.getQuantity());

//        System.out.println(username);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        //  Find stock
        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        //  Get stock price
        BigDecimal stockPrice = stock.getPrice();

        //  Calculate total cost
        BigDecimal totalCost = stockPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        //  Check user balance
        if (user.getBalance().compareTo( totalCost) < 0) {
            logger.warn("BUY failed - Insufficient balance for user email: {}", email);
            throw new InsufficientBalanceException("Insufficient balance");
        }

        //  Deduct balance
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        //  Check if user already owns this stock
        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElse(null);

        if (holding == null) {

            //  Create new portfolio holding
            holding = new PortfolioHolding();
            holding.setUser(user);
            holding.setStock(stock);
            holding.setQuantity(request.getQuantity());
            holding.setAveragePrice(stockPrice);

        } else {

            //  Update existing holding using weighted average
            int oldQuantity = holding.getQuantity();
            BigDecimal oldPrice = holding.getAveragePrice();

            int newQuantity = request.getQuantity();

            BigDecimal weightedPrice =
                    (oldPrice.multiply(BigDecimal.valueOf(oldQuantity))
                            .add(stockPrice.multiply(BigDecimal.valueOf(newQuantity))))
                            .divide(
                                    BigDecimal.valueOf(oldQuantity + newQuantity),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            holding.setQuantity(oldQuantity + newQuantity);
            holding.setAveragePrice(weightedPrice);

            logger.info("BUY successful for user email: {}, stock: {}, quantity: {}", email, request.getSymbol(), request.getQuantity());
        }

        portfolioHoldingRepository.save(holding);

        tradeHistoryService.recordBuyTrade(
                user,
                stock,
                request.getQuantity(),
                stockPrice
        );

        //  Return response
        return new TradeResponse(
                "Stock purchased successfully",
                stock.getSymbol(),
                request.getQuantity(),
                stockPrice,
                totalCost
        );
    }

    @Transactional
    public TradeResponse sellStock(SellStockRequest request) {

        validateMarketOpen();

        // 1. Get authenticated user
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        logger.info("Initiating SELL trade for user email: {}, stock: {}, quantity: {}", email, request.getSymbol(), request.getQuantity());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + request.getSymbol()));

        // 3. Find portfolio holding
        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElseThrow(() -> new PortfolioStockNotFoundException("Stock not owned by user"));

        // 4. Check if user has enough shares
        if (holding.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient shares to sell");
        }

        // 5. Get stock price
        BigDecimal stockPrice = stock.getPrice();

        // 6. Calculate total sale amount
        BigDecimal totalAmount = stockPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        // 7. Reduce portfolio quantity
        int remainingQuantity = holding.getQuantity() - request.getQuantity();

        if (remainingQuantity == 0) {

            // delete holding if user sold everything
            portfolioHoldingRepository.delete(holding);

        } else {

            holding.setQuantity(remainingQuantity);
            portfolioHoldingRepository.save(holding);
        }

        // 8. Add money to user balance
        user.setBalance(user.getBalance().add(totalAmount));
        userRepository.save(user);

        tradeHistoryService.recordSellTrade(
                user,
                stock,
                request.getQuantity(),
                stockPrice
        );

        logger.info("SELL successful for user email: {}, stock: {}, quantity: {}", email, request.getSymbol(), request.getQuantity());

        // 9. Return response
        return new TradeResponse(
                "Stock sold successfully",
                stock.getSymbol(),
                request.getQuantity(),
                stockPrice,
                totalAmount
        );
    }

    private void validateMarketOpen() {
        if (marketStatusService.getMarketStatus() == MarketStatus.CLOSED) {
            logger.warn("Failed - Market is closed");
            throw new MarketClosedException("Order Rejected: Market is closed");
        }
    }
}