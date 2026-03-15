package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.trade.*;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.service.TradeHistoryService;
import com.trading.tradingplatform.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeHistoryService tradeHistoryService;
    private final UserRepository userRepository;

    @PostMapping("/buy")
    public TradeResponse buyStock(@Valid @RequestBody BuyStockRequest request) {
        return tradeService.buyStock(request);
    }

    @PostMapping("/sell")
    public TradeResponse sellStock(@Valid @RequestBody SellStockRequest request) {
        return tradeService.sellStock(request);
    }

//    GET /api/v1/trades/history?page=0&size=5  -> request api example
    @GetMapping("/history")
    public Page<TradeHistoryResponse> getTradeHistory(Pageable pageable) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tradeHistoryService.getTradeHistory(user.getId(), pageable);
    }

    @GetMapping("/pnl")
    public TradePnLResponse getProfitAndLoss() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tradeHistoryService.getUserPnL(user.getId());
    }

    @GetMapping("/summary")
    public TradeSummaryResponse getTradeSummary() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tradeHistoryService.getTradeSummary(user.getId());
    }
}