package com.trading.tradingplatform.service;

import com.trading.tradingplatform.entity.enums.MarketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class MarketStatusServiceTest {

    private final MarketStatusService marketStatusService = new MarketStatusService();

    @Test
    void getMarketStatus_open() {

        LocalTime time = LocalTime.of(10, 0);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.OPEN, status);
    }

    @Test
    void getMarketStatus_atOpenBoundary() {

        LocalTime time = LocalTime.of(9, 15);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.OPEN, status);
    }

    @Test
    void getMarketStatus_atCloseBoundary() {

        LocalTime time = LocalTime.of(15, 30);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.OPEN, status);
    }

    @Test
    void getMarketStatus_beforeOpen() {

        LocalTime time = LocalTime.of(8, 0);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.CLOSED, status);
    }

    @Test
    void getMarketStatus_afterClose() {

        LocalTime time = LocalTime.of(16, 0);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.CLOSED, status);
    }

    @Test
    void getMarketStatus_runtimeCall() {

        MarketStatus status = marketStatusService.getMarketStatus();

        assertNotNull(status); // we don't control time → just ensure execution
    }
    @Test
    void getMarketStatus_justBeforeOpen() {

        LocalTime time = LocalTime.of(9, 14, 59);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.CLOSED, status);
    }

    @Test
    void getMarketStatus_justAfterClose() {

        LocalTime time = LocalTime.of(15, 30, 1);

        MarketStatus status = marketStatusService.getMarketStatus(time);

        assertEquals(MarketStatus.CLOSED, status);
    }

    @Test
    void getMarketStatus_runtime_multipleCalls() {

        marketStatusService.getMarketStatus();
        marketStatusService.getMarketStatus();
    }
}