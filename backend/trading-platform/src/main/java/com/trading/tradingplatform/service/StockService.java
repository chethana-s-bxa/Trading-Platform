package com.trading.tradingplatform.service;

import com.trading.tradingplatform.dto.StockRequest;
import com.trading.tradingplatform.entity.Stock;
import com.trading.tradingplatform.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    /**
     * Retrieves the complete list of stocks available in the system.
     *
     * @return List of all stocks present in the database.
     */
    public List<Stock> getAllStocks(){
        return stockRepository.findAll();
    }

    /**
     * Retrieves a stock using its ticker symbol.
     *
     * @param symbol the ticker symbol of the stock (e.g., TCS, INFY, RELIANCE)
     * @return the Stock entity corresponding to the given symbol
     * @throws RuntimeException if no stock is found for the provided symbol
     */
    public Stock getStockBySymbol(String symbol){
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found for this ticker"));
    }

    /**
     * Adds a new stock to the system.
     *
     * @param request contains stock details such as symbol, company name, and price
     * @return the saved Stock entity
     * @throws RuntimeException if the symbol is null or the stock already exists
     */
    public Stock addStock(StockRequest request){

        if(request.getSymbol() == null){
            throw new RuntimeException("Ticker symbol cannot be empty");
        }

        if(stockRepository.findBySymbol(request.getSymbol()).isPresent()){
            throw new RuntimeException("Stock already exists with this symbol");
        }

        Stock stock =  Stock.builder()
                .symbol(request.getSymbol())
                .companyName(request.getCompanyName())
                .price(request.getPrice())
                .lastUpdated(LocalDateTime.now())
                .build();

        stockRepository.save(stock);
        return stock;
    }

    /**
     * Updates an existing stock's details.
     *
     * @param request contains the updated stock information
     * @return the updated Stock entity
     * @throws RuntimeException if the stock symbol does not exist
     */
    public Stock updateStock(StockRequest request){


        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found for this ticker"));

        if(request.getCompanyName() != null){
            stock.setCompanyName(request.getCompanyName());
        }

        if(request.getPrice() != null){
            stock.setPrice(request.getPrice());
        }

        stock.setLastUpdated(LocalDateTime.now());

        return stockRepository.save(stock);
    }

    /**
     * Deletes a stock from the system using its ticker symbol.
     *
     * @param symbol the ticker symbol of the stock to delete
     * @throws RuntimeException if the stock does not exist
     */
    public void deleteStockBySymbol(String symbol){
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(()->new RuntimeException("Stock not found for this ticker"));

        stockRepository.deleteById(stock.getId());

    }
}