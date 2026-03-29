package com.stockrisk.controller;

import com.stockrisk.model.StockMetrics;
import com.stockrisk.service.StockMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import java.io.IOException;

@RestController
@RequestMapping("/stocks")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000", "http://127.0.0.1:4200"}, methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class StockController {

    @Autowired
    private StockMetricsService metricsService;

    @RequestMapping(method = RequestMethod.OPTIONS)
    public void handleOptions() {
        // Preflight request handler
    }

    @GetMapping("/{symbol}/metrics")
    public ResponseEntity<?> getStockMetrics(
        @PathVariable String symbol,
        @RequestParam(defaultValue = "252") int days) {

        try {
            if (days <= 0 || days > 5000) {
                return ResponseEntity.badRequest()
                    .body("Days must be between 1 and 5000");
            }

            StockMetrics metrics = metricsService.calculateMetrics(symbol, days);
            return ResponseEntity.ok(metrics);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body("Error fetching data from Yahoo Finance: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchStocks(@RequestParam String query) {
        // TODO: Implement stock search functionality
        // For MVP, return mock data or empty list
        return ResponseEntity.ok(new String[]{query.toUpperCase()});
    }
}
