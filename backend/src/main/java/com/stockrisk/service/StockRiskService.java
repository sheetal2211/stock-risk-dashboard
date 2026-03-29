package com.stockrisk.service;

import com.stockrisk.client.YahooFinanceClient;
import com.stockrisk.model.StockRiskResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockRiskService {

    private static final int DEFAULT_PERIOD = 252;
    private static final int TRADING_DAYS_PER_YEAR = 252;

    private final YahooFinanceClient yahooFinanceClient;

    public StockRiskService(YahooFinanceClient yahooFinanceClient) {
        this.yahooFinanceClient = yahooFinanceClient;
    }

    /**
     * Fetches stock data and computes risk metrics using the default period (252 trading days).
     */
    public StockRiskResponse calculateRisk(String ticker) {
        return calculateRisk(ticker, DEFAULT_PERIOD);
    }

    /**
     * Fetches stock data and computes log returns and annualized volatility for the given period.
     */
    public StockRiskResponse calculateRisk(String ticker, int period) {
        List<Double> prices = yahooFinanceClient.fetchClosingPrices(ticker, period);
        String companyName = yahooFinanceClient.fetchCompanyName(ticker);

        if (prices.size() < 2) {
            throw new IllegalStateException("Not enough price data to calculate risk for: " + ticker);
        }

        // Trim to requested period + 1 (need N+1 prices for N returns)
        int maxPrices = Math.min(prices.size(), period + 1);
        List<Double> usedPrices = prices.subList(prices.size() - maxPrices, prices.size());

        List<Double> logReturns = computeLogReturns(usedPrices);
        double averageLogReturn = average(logReturns);
        double volatility = annualizedVolatility(logReturns, TRADING_DAYS_PER_YEAR);
        double latestPrice = usedPrices.get(usedPrices.size() - 1);

        return new StockRiskResponse(
                ticker.toUpperCase(),
                companyName,
                logReturns.size(),
                volatility,
                averageLogReturn,
                latestPrice,
                logReturns
        );
    }

    /**
     * Computes daily log returns: ln(P_t / P_{t-1})
     */
    List<Double> computeLogReturns(List<Double> prices) {
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            double curr = prices.get(i);
            if (prev > 0 && curr > 0) {
                returns.add(Math.log(curr / prev));
            }
        }
        return returns;
    }

    /**
     * Annualized volatility = stddev(log_returns) × sqrt(tradingDaysPerYear)
     */
    double annualizedVolatility(List<Double> logReturns, int tradingDaysPerYear) {
        if (logReturns.isEmpty()) return 0.0;
        double mean = average(logReturns);
        double variance = logReturns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance) * Math.sqrt(tradingDaysPerYear);
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
