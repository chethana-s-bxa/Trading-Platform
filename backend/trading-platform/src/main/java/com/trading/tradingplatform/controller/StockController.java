package com.trading.tradingplatform.controller;

import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * This endpoint gives the list of all the stocks
     * @return list of all stocks
     */
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks(){
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    /**
     * this endpoint gives the stock details based on the ticker symbol given
     * @param symbol Ticker symbol for the stock to find
     * @return details of that stock
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol){
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }

    /**
     * This endpoint allows creation of a new stock entry in the database.
     *
     * @param request the stock details provided in the request body
     * @return the newly created stock entity
     */
    @PostMapping
    public ResponseEntity<Stock> addStock(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.addStock(request));
    }

    /**
     * This endpoint updates stock attributes such as company name
     * or price based on the provided request body.
     *
     * @param request contains the updated stock details
     * @return the updated stock entity
     */
    @PutMapping
    public ResponseEntity<Stock> updateStock(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.updateStock(request));
    }

    /**
     * Deletes a stock from the system using its ticker symbol.
     *
     * @param symbol the ticker symbol of the stock to be deleted
     * @return confirmation message
     */
    @DeleteMapping("/{symbol}")
    public ResponseEntity<String> deleteStock(@PathVariable String symbol) {

        stockService.deleteStockBySymbol(symbol);

        return ResponseEntity.ok("Stock deleted successfully");
    }

    @GetMapping("/search")
    public Page<Stock> searchStocks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return stockService.searchStocks(query, minPrice, maxPrice, page, size);
    }
}
