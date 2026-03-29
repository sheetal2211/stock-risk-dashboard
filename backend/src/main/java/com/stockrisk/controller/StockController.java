package com.stockrisk.controller;

import com.stockrisk.exception.StockDataException;
import com.stockrisk.model.StockMetrics;
import com.stockrisk.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}/metrics")
    public ResponseEntity<StockMetrics> getStockMetrics(
            @PathVariable String symbol) {
        try {
            StockMetrics metrics = stockService.getStockMetrics(symbol);
            return ResponseEntity.ok(metrics);
        } catch (StockDataException e) {
            StockMetrics error = new StockMetrics(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            StockMetrics error = new StockMetrics("Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stock Risk Dashboard API is running");
    }
}
