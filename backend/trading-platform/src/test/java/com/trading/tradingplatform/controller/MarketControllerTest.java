package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.entity.enums.MarketStatus;
import com.trading.tradingplatform.service.MarketStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MarketControllerTest {

    @Mock
    private MarketStatusService marketStatusService;

    @InjectMocks
    private MarketController marketController;

    @Test
    void getMarketStatus_success() {

        when(marketStatusService.getMarketStatus())
                .thenReturn(MarketStatus.OPEN);

        MarketStatus result = marketController.getMarketStatus();

        assertEquals(MarketStatus.OPEN, result);

        verify(marketStatusService).getMarketStatus();
    }


}