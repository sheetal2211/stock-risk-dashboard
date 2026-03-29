package com.stockrisk.model;

import java.time.LocalDateTime;

public class StockMetrics {
    private String symbol;
    private double logReturns;           // percentage
    private double volatility;           // annualized percentage
    private String period;               // e.g., "252 days"
    private LocalDateTime timestamp;

    public StockMetrics(String symbol, double logReturns, double volatility, String period, LocalDateTime timestamp) {
        this.symbol = symbol;
        this.logReturns = logReturns;
        this.volatility = volatility;
        this.period = period;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getLogReturns() {
        return logReturns;
    }

    public void setLogReturns(double logReturns) {
        this.logReturns = logReturns;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
