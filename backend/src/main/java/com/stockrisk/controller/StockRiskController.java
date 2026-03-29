package com.stockrisk.controller;

import com.stockrisk.model.StockRiskResponse;
import com.stockrisk.service.StockRiskService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@Validated
public class StockRiskController {

    private final StockRiskService stockRiskService;

    public StockRiskController(StockRiskService stockRiskService) {
        this.stockRiskService = stockRiskService;
    }

    /**
     * GET /api/stock/{ticker}/risk
     * GET /api/stock/{ticker}/risk?period=252
     *
     * Returns log returns, annualized volatility, and period for the given ticker.
     */
    @GetMapping("/{ticker}/risk")
    public ResponseEntity<StockRiskResponse> getRisk(
            @PathVariable
            @NotBlank
            @Pattern(regexp = "^[A-Za-z0-9.\\-^=]{1,10}$", message = "Invalid ticker format")
            String ticker,

            @RequestParam(defaultValue = "252")
            @Min(value = 10, message = "Period must be at least 10 trading days")
            @Max(value = 504, message = "Period cannot exceed 504 trading days (2 years)")
            int period) {

        StockRiskResponse response = stockRiskService.calculateRisk(ticker, period);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/health
     * Simple health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "stock-risk-dashboard"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
    }
}
