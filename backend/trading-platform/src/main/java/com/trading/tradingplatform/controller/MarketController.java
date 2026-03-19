package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.entity.enums.MarketStatus;
import com.trading.tradingplatform.service.MarketStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketStatusService marketStatusService;

    @GetMapping("/status")
    public MarketStatus getMarketStatus() {
        return marketStatusService.getMarketStatus();
    }
}