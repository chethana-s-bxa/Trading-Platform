package com.trading.tradingplatform.controller;


import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.PortfolioHolding;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.entity.Trade;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.repository.PortfolioHoldingRepository;
import com.trading.tradingplatform.repository.StockRepository;
import com.trading.tradingplatform.repository.TradeRepository;
import com.trading.tradingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final PortfolioHoldingRepository portfolioRepository;

    // CREATE STOCK
    @PostMapping("/stocks")
    public Stock createStock(@RequestBody StockRequest request){

        if(stockRepository.existsBySymbol(request.getSymbol())){
            throw new RuntimeException("Stock already exists");
        }

        Stock stock = new Stock();
        stock.setSymbol(request.getSymbol().toUpperCase());
        stock.setCompanyName(request.getCompanyName());
        stock.setPrice(request.getPrice());

        return stockRepository.save(stock);
    }

    // UPDATE STOCK
    @PutMapping("/stocks/{symbol}")
    public Stock updateStock(@PathVariable String symbol,
                             @RequestBody StockRequest request){

        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setPrice(request.getPrice());

        if(request.getCompanyName() != null){
            stock.setCompanyName(request.getCompanyName());
        }

        return stockRepository.save(stock);
    }

    // DELETE STOCK
    @DeleteMapping("/stocks/{symbol}")
    public String deleteStock(@PathVariable String symbol){

        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stockRepository.delete(stock);

        return "Stock deleted successfully";
    }

    @GetMapping("/trades")
    public List<Trade> getAllTrades(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String username
    ) {

        if (symbol != null && username != null) {
            return tradeRepository.findByStockSymbolAndUserUsername(symbol, username);
        }

        if (symbol != null) {
            return tradeRepository.findByStockSymbol(symbol);
        }

        if (username != null) {
            return tradeRepository.findByUserUsername(username);
        }

        return tradeRepository.findAll();
    }

    @GetMapping("/users")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping("/users/{userId}/portfolio")
    public List<PortfolioHolding> getUserPortfolio(@PathVariable Long userId){
        return portfolioRepository.findByUserId(userId);
    }

    @GetMapping("/trades/search")
    public List<Trade> searchTrades(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {

        if (symbol != null) {
            return tradeRepository.findByStockSymbol(symbol);
        }

        if (userId != null) {
            return tradeRepository.findByUserId(userId);
        }

        return tradeRepository.findAll();
    }

}
