package com.stockrisk.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stockrisk.exception.StockDataException;
import com.stockrisk.model.StockMetrics;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private static final String YAHOO_FINANCE_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?range=1y&interval=1d";

    private static final int TRADING_DAYS_PER_YEAR = 252;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestClient restClient;

    public StockService() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public StockMetrics getStockMetrics(String symbol) {
        String upperSymbol = symbol.toUpperCase().trim();
        String responseBody = fetchFromYahooFinance(upperSymbol);
        return parseAndCalculate(upperSymbol, responseBody);
    }

    private String fetchFromYahooFinance(String symbol) {
        try {
            return restClient.get()
                    .uri(YAHOO_FINANCE_URL, symbol)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new StockDataException("Failed to fetch data for symbol: " + symbol, e);
        }
    }

    private StockMetrics parseAndCalculate(String symbol, String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject chart = root.getAsJsonObject("chart");

        JsonElement errorElement = chart.get("error");
        if (errorElement != null && !errorElement.isJsonNull()) {
            throw new StockDataException("Yahoo Finance error for symbol: " + symbol);
        }

        JsonArray results = chart.getAsJsonArray("result");
        if (results == null || results.isEmpty()) {
            throw new StockDataException("No data found for symbol: " + symbol);
        }

        JsonObject result = results.get(0).getAsJsonObject();
        JsonObject meta = result.getAsJsonObject("meta");
        JsonArray timestamps = result.getAsJsonArray("timestamp");

        JsonObject indicators = result.getAsJsonObject("indicators");
        JsonArray quoteArray = indicators.getAsJsonArray("quote");
        JsonObject quote = quoteArray.get(0).getAsJsonObject();
        JsonArray closeArray = quote.getAsJsonArray("close");

        // Extract close prices and dates, filtering out nulls
        List<Double> closePrices = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < closeArray.size(); i++) {
            JsonElement closeEl = closeArray.get(i);
            if (!closeEl.isJsonNull()) {
                closePrices.add(closeEl.getAsDouble());
                long epochSeconds = timestamps.get(i).getAsLong();
                LocalDate date = Instant.ofEpochSecond(epochSeconds)
                        .atZone(ZoneOffset.UTC).toLocalDate();
                dates.add(date.format(DATE_FORMATTER));
            }
        }

        if (closePrices.size() < 2) {
            throw new StockDataException("Insufficient price data for symbol: " + symbol);
        }

        // Calculate log returns: ln(P_t / P_{t-1})
        List<Double> logReturns = calculateLogReturns(closePrices);

        // Calculate annualized volatility: std_dev(logReturns) * sqrt(252)
        double volatility = calculateAnnualizedVolatility(logReturns);

        // Average daily log return
        double avgLogReturn = logReturns.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        StockMetrics metrics = new StockMetrics();
        metrics.setSymbol(symbol);
        metrics.setCompanyName(meta.has("longName") && !meta.get("longName").isJsonNull()
                ? meta.get("longName").getAsString()
                : meta.has("shortName") && !meta.get("shortName").isJsonNull()
                        ? meta.get("shortName").getAsString() : symbol);
        metrics.setCurrentPrice(closePrices.get(closePrices.size() - 1));
        metrics.setVolatility(round4(volatility * 100)); // as percentage
        metrics.setAverageLogReturn(round4(avgLogReturn));
        metrics.setPeriod(logReturns.size());
        metrics.setLogReturns(logReturns.stream().map(this::round4).toList());
        metrics.setDates(dates.subList(1, dates.size())); // align with logReturns (skip first)
        metrics.setClosePrices(closePrices);

        return metrics;
    }

    List<Double> calculateLogReturns(List<Double> prices) {
        List<Double> logReturns = new ArrayList<>(prices.size() - 1);
        for (int i = 1; i < prices.size(); i++) {
            double logReturn = Math.log(prices.get(i) / prices.get(i - 1));
            logReturns.add(logReturn);
        }
        return logReturns;
    }

    double calculateAnnualizedVolatility(List<Double> logReturns) {
        double mean = logReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = logReturns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .sum() / (logReturns.size() - 1);
        return Math.sqrt(variance) * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
