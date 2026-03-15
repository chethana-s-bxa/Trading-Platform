package com.trading.tradingplatform.websocket;

import com.trading.tradingplatform.dto.market.MarketPriceUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.trading.tradingplatform.dto.trade.TradeStreamMessage;
import com.trading.tradingplatform.dto.order.OrderBookStreamMessage;

/**
 * Service responsible for publishing real-time market updates
 * through WebSocket topics.
 *
 * This service acts as a bridge between backend services
 * (like MarketPriceSimulator, TradeEngine, OrderBook)
 * and connected WebSocket clients.
 *
 * Messages sent through this service are broadcasted
 * to all subscribed clients.
 */
@Service
@RequiredArgsConstructor
public class MarketDataPublisher {

    /**
     * Spring messaging template used to send messages
     * to WebSocket topics.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket topic for broadcasting market price updates.
     */
    private static final String MARKET_PRICE_TOPIC = "/topic/market/prices";

    private static final String TRADES_TOPIC = "/topic/trades";

    /**
     * Broadcasts a stock price update to all connected clients.
     *
     * Example topic:
     * /topic/market/prices
     *
     * @param message Market price update message
     */
    public void broadcastPriceUpdate(MarketPriceUpdateMessage message) {

        messagingTemplate.convertAndSend(MARKET_PRICE_TOPIC, message);
    }

    /**
     * Broadcasts an executed trade to all connected WebSocket clients.
     *
     * Topic:
     * /topic/trades
     *
     * @param message trade stream message
     */
    public void broadcastTrade(TradeStreamMessage message) {

        messagingTemplate.convertAndSend(TRADES_TOPIC, message);
    }

    /**
     * Broadcasts order book updates for a specific stock.
     *
     * Topic format:
     * /topic/orderbook/{symbol}
     *
     * @param message order book snapshot
     */
    public void broadcastOrderBook(OrderBookStreamMessage message) {

        String topic = "/topic/orderbook/" + message.getSymbol();

        messagingTemplate.convertAndSend(topic, message);
    }
}