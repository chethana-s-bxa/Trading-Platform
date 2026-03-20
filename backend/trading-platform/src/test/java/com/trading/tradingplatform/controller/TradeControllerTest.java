package com.trading.tradingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.tradingplatform.dto.trade.*;
import com.trading.tradingplatform.entity.User;
import com.trading.tradingplatform.exception.GlobalExceptionHandler;
import com.trading.tradingplatform.exception.InsufficientBalanceException;
import com.trading.tradingplatform.exception.InsufficientStockException;
import com.trading.tradingplatform.repository.UserRepository;
import com.trading.tradingplatform.security.JwtService;
import com.trading.tradingplatform.service.TradeHistoryService;
import com.trading.tradingplatform.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

@WebMvcTest(TradeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @MockBean
    private TradeHistoryService tradeHistoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupSecurityContext() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@example.com", null);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void buyStock_success() throws Exception {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        TradeResponse response = new TradeResponse(
                "Stock purchased successfully",
                "AAPL",
                10,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1000)
        );

        when(tradeService.buyStock(any(BuyStockRequest.class)))
                .thenReturn(response);

        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/trade/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock purchased successfully"))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void buyStock_insufficientBalance() throws Exception {

        // ---------- Arrange ----------

        BuyStockRequest request = new BuyStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        when(tradeService.buyStock(any(BuyStockRequest.class)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/trade/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void sellStock_success() throws Exception {

        // ---------- Arrange ----------

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(5);

        TradeResponse response = new TradeResponse(
                "Stock sold successfully",
                "AAPL",
                5,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(500)
        );

        when(tradeService.sellStock(any(SellStockRequest.class)))
                .thenReturn(response);

        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/trade/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock sold successfully"))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void sellStock_insufficientStock() throws Exception {

        // ---------- Arrange ----------

        SellStockRequest request = new SellStockRequest();
        request.setSymbol("AAPL");
        request.setQuantity(10);

        when(tradeService.sellStock(any(SellStockRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient shares to sell"));

        // ---------- Act & Assert ----------

        mockMvc.perform(post("/api/v1/trade/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient shares to sell"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getTradeHistory_success() throws Exception {

        // ---------- Arrange ----------

        // Mock SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Page response
        Page<TradeHistoryResponse> page = new PageImpl<>(List.of());

        when(tradeHistoryService.getTradeHistory(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // ---------- Act & Assert ----------

        mockMvc.perform(get("/api/v1/trade/history")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void getPnL_success() throws Exception {

        // ---------- Arrange ----------

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        TradePnLResponse response = new TradePnLResponse();
        response.setTotalInvested(BigDecimal.valueOf(1000));
        response.setTotalSold(BigDecimal.valueOf(1200));
        response.setNetProfitOrLoss(BigDecimal.valueOf(200));

        when(tradeHistoryService.getUserPnL(1L))
                .thenReturn(response);

        // ---------- Act & Assert ----------

        mockMvc.perform(get("/api/v1/trade/pnl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netProfitOrLoss").value(200));
    }

    @Test
    void getTradeSummary_success() throws Exception {

        // ---------- Arrange ----------

        // Mock SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Mock User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Mock Response
        TradeSummaryResponse response = new TradeSummaryResponse();
        response.setTotalTrades(5);
        response.setTotalBuyTrades(3);
        response.setTotalSellTrades(2);

        when(tradeHistoryService.getTradeSummary(1L))
                .thenReturn(response);

        // ---------- Act & Assert ----------

        mockMvc.perform(get("/api/v1/trade/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrades").value(5))
                .andExpect(jsonPath("$.totalBuyTrades").value(3))
                .andExpect(jsonPath("$.totalSellTrades").value(2));
    }
}