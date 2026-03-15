package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.trade.TradeHistoryResponse;
import com.trading.tradingplatform.dto.trade.TradePnLResponse;
import com.trading.tradingplatform.dto.trade.TradeSummaryResponse;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.Trade;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.entity.enums.TradeType;
import com.trading.tradingplatform.repository.TradeRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.trading.tradingplatform.dto.trade.TradeStreamMessage;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {

    private final TradeRepository tradeRepository;
    private final MarketDataPublisher marketDataPublisher;
    private final PortfolioValuationService portfolioValuationService;

    public void recordBuyTrade(User user, Stock stock, Integer quantity, BigDecimal pricePerShare) {

        BigDecimal totalAmount = pricePerShare.multiply(BigDecimal.valueOf(quantity));

        Trade trade = Trade.builder()
                .user(user)
                .stock(stock)
                .tradeType(TradeType.BUY)
                .quantity(quantity)
                .pricePerShare(pricePerShare)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        tradeRepository.save(trade);
    }

    public void recordSellTrade(User user, Stock stock, Integer quantity, BigDecimal pricePerShare) {

        BigDecimal totalAmount = pricePerShare.multiply(BigDecimal.valueOf(quantity));

        Trade trade = Trade.builder()
                .user(user)
                .stock(stock)
                .tradeType(TradeType.SELL)
                .quantity(quantity)
                .pricePerShare(pricePerShare)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        tradeRepository.save(trade);
    }

    public List<Trade> getUserTradeHistory(Long userId) {
        return tradeRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<TradeHistoryResponse> getUserTradeHistoryResponse(Long userId) {

        return tradeRepository
                .findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(trade -> new TradeHistoryResponse(
                        trade.getStock().getSymbol(),
                        trade.getTradeType(),
                        trade.getQuantity(),
                        trade.getPricePerShare(),
                        trade.getTotalAmount(),
                        trade.getTimestamp()
                ))
                .toList();
    }

    public Page<TradeHistoryResponse> getTradeHistory(Long userId, Pageable pageable) {

        return tradeRepository
                .findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(trade -> new TradeHistoryResponse(
                        trade.getStock().getSymbol(),
                        trade.getTradeType(),
                        trade.getQuantity(),
                        trade.getPricePerShare(),
                        trade.getTotalAmount(),
                        trade.getTimestamp()
                ));
    }

    public TradePnLResponse getUserPnL(Long userId) {

        BigDecimal totalInvested =
                tradeRepository.getTotalAmountByUserAndType(userId, TradeType.BUY);

        BigDecimal totalSold =
                tradeRepository.getTotalAmountByUserAndType(userId, TradeType.SELL);

        BigDecimal netProfitOrLoss = totalSold.subtract(totalInvested);

        return new TradePnLResponse(
                totalInvested,
                totalSold,
                netProfitOrLoss
        );
    }

    public TradeSummaryResponse getTradeSummary(Long userId) {

        long totalTrades = tradeRepository.countByUserId(userId);

        long totalBuyTrades =
                tradeRepository.countByUserIdAndTradeType(userId, TradeType.BUY);

        long totalSellTrades =
                tradeRepository.countByUserIdAndTradeType(userId, TradeType.SELL);

        BigDecimal totalInvested =
                tradeRepository.getTotalAmountByUserAndType(userId, TradeType.BUY);

        BigDecimal totalSold =
                tradeRepository.getTotalAmountByUserAndType(userId, TradeType.SELL);

        BigDecimal netProfitOrLoss = totalSold.subtract(totalInvested);

        return new TradeSummaryResponse(
                totalTrades,
                totalBuyTrades,
                totalSellTrades,
                totalInvested,
                totalSold,
                netProfitOrLoss
        );
    }

    /**
     * Records a matched trade between a buyer and seller.
     * Two trade records are stored:
     * - BUY trade for buyer
     * - SELL trade for seller
     *
     * After saving both records, the trade execution
     * is broadcast to WebSocket clients.
     */
    public void recordMatchedTrade(
            User buyer,
            User seller,
            Stock stock,
            Integer quantity,
            BigDecimal pricePerShare
    ) {

        BigDecimal totalAmount =
                pricePerShare.multiply(BigDecimal.valueOf(quantity));

        // BUY trade record
        Trade buyTrade = Trade.builder()
                .user(buyer)
                .stock(stock)
                .tradeType(TradeType.BUY)
                .quantity(quantity)
                .pricePerShare(pricePerShare)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        // SELL trade record
        Trade sellTrade = Trade.builder()
                .user(seller)
                .stock(stock)
                .tradeType(TradeType.SELL)
                .quantity(quantity)
                .pricePerShare(pricePerShare)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        tradeRepository.save(buyTrade);
        tradeRepository.save(sellTrade);

        /**
         * Create trade stream message for WebSocket broadcast.
         */
        TradeStreamMessage message = TradeStreamMessage.builder()
                .symbol(stock.getSymbol())
                .price(pricePerShare)
                .quantity(quantity)
                .buyerId(buyer.getId())
                .sellerId(seller.getId())
                .timestamp(LocalDateTime.now())
                .build();

        /**
         * Broadcast executed trade to WebSocket clients.
         */
        marketDataPublisher.broadcastTrade(message);

        /**
         * Broadcast updated portfolio valuation
         * for both buyer and seller.
         */
        portfolioValuationService.broadcastPortfolioValue(buyer.getId());
        portfolioValuationService.broadcastPortfolioValue(seller.getId());
    }
}