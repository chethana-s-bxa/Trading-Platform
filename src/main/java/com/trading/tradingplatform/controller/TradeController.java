package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.trade.BuyStockRequest;
import com.trading.tradingplatform.dto.trade.SellStockRequest;
import com.trading.tradingplatform.dto.trade.TradeResponse;
import com.trading.tradingplatform.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public TradeResponse buyStock(@Valid @RequestBody BuyStockRequest request) {
        return tradeService.buyStock(request);
    }

    @PostMapping("/sell")
    public TradeResponse sellStock(@Valid @RequestBody SellStockRequest request) {
        return tradeService.sellStock(request);
    }
}