package com.stockrisk.model;

import java.util.List;

public class StockMetrics {

    private String symbol;
    private String companyName;
    private double currentPrice;
    private double volatility;          // annualized volatility (%)
    private double averageLogReturn;    // average daily log return
    private int period;                 // number of trading days analyzed
    private List<Double> logReturns;
    private List<String> dates;
    private List<Double> closePrices;
    private String error;

    public StockMetrics() {}

    public StockMetrics(String error) {
        this.error = error;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getVolatility() { return volatility; }
    public void setVolatility(double volatility) { this.volatility = volatility; }

    public double getAverageLogReturn() { return averageLogReturn; }
    public void setAverageLogReturn(double averageLogReturn) { this.averageLogReturn = averageLogReturn; }

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public List<Double> getLogReturns() { return logReturns; }
    public void setLogReturns(List<Double> logReturns) { this.logReturns = logReturns; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<Double> getClosePrices() { return closePrices; }
    public void setClosePrices(List<Double> closePrices) { this.closePrices = closePrices; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
