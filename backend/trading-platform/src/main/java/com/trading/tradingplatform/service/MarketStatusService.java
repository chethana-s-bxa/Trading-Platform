package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.enums.MarketStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class MarketStatusService {

    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 15);
    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 30);

    public MarketStatus getMarketStatus() {

        // Get current time in IST
        LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));

        // Check if within market hours
        if (!currentTime.isBefore(MARKET_OPEN_TIME) && !currentTime.isAfter(MARKET_CLOSE_TIME)) {
            return MarketStatus.OPEN;
        }

        return MarketStatus.CLOSED;
    }
}