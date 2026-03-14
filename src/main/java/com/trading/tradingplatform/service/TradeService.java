package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.trade.BuyStockRequest;
import com.trading.tradingplatform.dto.trade.SellStockRequest;
import com.trading.tradingplatform.dto.trade.TradeResponse;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;

    @Transactional
    public TradeResponse buyStock(BuyStockRequest request) {

        // 1️⃣ Get authenticated user
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Find stock
        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        // 3️⃣ Get stock price
        BigDecimal stockPrice = stock.getPrice();

        // 4️⃣ Calculate total cost
        BigDecimal totalCost = stockPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        // 5️⃣ Check user balance
        if (user.getBalance().compareTo( totalCost) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 6️⃣ Deduct balance
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        // 7️⃣ Check if user already owns this stock
        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElse(null);

        if (holding == null) {

            // 8️⃣ Create new portfolio holding
            holding = new PortfolioHolding();
            holding.setUser(user);
            holding.setStock(stock);
            holding.setQuantity(request.getQuantity());
            holding.setAveragePrice(stockPrice);

        } else {

            // 9️⃣ Update existing holding using weighted average
            int oldQuantity = holding.getQuantity();
            BigDecimal oldPrice = holding.getAveragePrice();

            int newQuantity = request.getQuantity();

            BigDecimal weightedPrice =
                    (oldPrice.multiply(BigDecimal.valueOf(oldQuantity))
                            .add(stockPrice.multiply(BigDecimal.valueOf(newQuantity))))
                            .divide(BigDecimal.valueOf(oldQuantity + newQuantity));

            holding.setQuantity(oldQuantity + newQuantity);
            holding.setAveragePrice(weightedPrice);
        }

        portfolioHoldingRepository.save(holding);

        // 🔟 Return response
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

        // 1. Get authenticated user
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find stock
        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        // 3. Find portfolio holding
        PortfolioHolding holding = portfolioHoldingRepository
                .findByUserIdAndStockSymbol(user.getId(), stock.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not owned by user"));

        // 4. Check if user has enough shares
        if (holding.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient shares to sell");
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

        // 9. Return response
        return new TradeResponse(
                "Stock sold successfully",
                stock.getSymbol(),
                request.getQuantity(),
                stockPrice,
                totalAmount
        );
    }
}