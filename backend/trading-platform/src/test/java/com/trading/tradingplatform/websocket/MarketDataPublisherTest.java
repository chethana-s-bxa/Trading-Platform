package com.trading.tradingplatform.websocket;

import com.trading.tradingplatform.dto.market.MarketPriceUpdateMessage;
import com.trading.tradingplatform.dto.order.OrderBookStreamMessage;
import com.trading.tradingplatform.dto.trade.TradeStreamMessage;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MarketDataPublisherTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    private final MarketDataPublisher publisher =
            new MarketDataPublisher(messagingTemplate);

    @Test
    void broadcastPriceUpdate_success() {

        MarketPriceUpdateMessage message = MarketPriceUpdateMessage.builder()
                .symbol("AAPL")
                .build();

        publisher.broadcastPriceUpdate(message);

        verify(messagingTemplate)
                .convertAndSend("/topic/market/prices", message);
    }

    @Test
    void broadcastTrade_success() {

        TradeStreamMessage message = TradeStreamMessage.builder()
                .symbol("AAPL")
                .build();

        publisher.broadcastTrade(message);

        verify(messagingTemplate)
                .convertAndSend("/topic/trades", message);
    }

    @Test
    void broadcastOrderBook_success() {

        OrderBookStreamMessage message = OrderBookStreamMessage.builder()
                .symbol("AAPL")
                .build();

        publisher.broadcastOrderBook(message);

        verify(messagingTemplate)
                .convertAndSend("/topic/orderbook/AAPL", message);
    }


}