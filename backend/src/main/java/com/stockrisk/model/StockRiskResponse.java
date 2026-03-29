package com.stockrisk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StockRiskResponse {

    private String ticker;
    private String companyName;
    private int period;
    private double volatility;
    private double averageLogReturn;
    private double latestPrice;
    private List<Double> logReturns;

    public StockRiskResponse() {}

    public StockRiskResponse(String ticker, String companyName, int period,
                              double volatility, double averageLogReturn,
                              double latestPrice, List<Double> logReturns) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.period = period;
        this.volatility = volatility;
        this.averageLogReturn = averageLogReturn;
        this.latestPrice = latestPrice;
        this.logReturns = logReturns;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public double getVolatility() { return volatility; }
    public void setVolatility(double volatility) { this.volatility = volatility; }

    @JsonProperty("averageLogReturn")
    public double getAverageLogReturn() { return averageLogReturn; }
    public void setAverageLogReturn(double averageLogReturn) { this.averageLogReturn = averageLogReturn; }

    public double getLatestPrice() { return latestPrice; }
    public void setLatestPrice(double latestPrice) { this.latestPrice = latestPrice; }

    public List<Double> getLogReturns() { return logReturns; }
    public void setLogReturns(List<Double> logReturns) { this.logReturns = logReturns; }
}
