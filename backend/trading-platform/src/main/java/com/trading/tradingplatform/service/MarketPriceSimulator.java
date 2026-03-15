package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.enums.MarketTrend;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.trading.tradingplatform.dto.market.MarketPriceUpdateMessage;
import com.trading.tradingplatform.websocket.MarketDataPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MarketPriceSimulator {

    private final StockRepository stockRepository;
    private final Random random = new Random();
    private MarketTrend currentTrend = MarketTrend.NEUTRAL;
    private final MarketDataPublisher marketDataPublisher;

    @Scheduled(fixedRate = 5000)
    public void updateMarketPrices() {

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            BigDecimal currentPrice = stock.getPrice();

            BigDecimal percentageChange = generatePriceChangePercent();

            BigDecimal changeAmount = currentPrice.multiply(percentageChange);

            BigDecimal newPrice = currentPrice.add(changeAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            stock.setPrice(newPrice);
            stock.setLastUpdated(LocalDateTime.now());

            stockRepository.save(stock);

            /**
            * Create WebSocket market price update message.
            */
            MarketPriceUpdateMessage message = MarketPriceUpdateMessage.builder()
                    .symbol(stock.getSymbol())
                    .price(stock.getPrice())
                    .timestamp(LocalDateTime.now())
                    .build();

            /**
             * Broadcast the updated price to all connected WebSocket clients.
             */
            marketDataPublisher.broadcastPriceUpdate(message);        }
    }

    @Scheduled(fixedRate = 60000)
    public void updateMarketTrend() {

        int trend = random.nextInt(3);

        switch (trend) {

            case 0:
                currentTrend = MarketTrend.BULL;
                break;

            case 1:
                currentTrend = MarketTrend.BEAR;
                break;

            default:
                currentTrend = MarketTrend.NEUTRAL;
        }

        System.out.println("Market Trend Changed To: " + currentTrend);
    }

    private BigDecimal generatePriceChangePercent() {

        double percent;

        switch (currentTrend) {

            case BULL:
                percent = random.nextDouble() * 0.02; // 0% to +2%
                break;

            case BEAR:
                percent = -random.nextDouble() * 0.02; // 0% to -2%
                break;

            default:
                percent = (random.nextDouble() * 4 - 2) / 100; // -2% to +2%
        }

        return BigDecimal.valueOf(percent);
    }
}