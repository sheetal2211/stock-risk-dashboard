package com.stockrisk.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceClient {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; StockRiskBot/1.0)";

    private final RestClient restClient;

    public YahooFinanceClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    /**
     * Fetches historical closing prices for the given ticker.
     *
     * @param ticker stock symbol (e.g. "AAPL")
     * @param days   number of trading days to fetch (e.g. 252)
     * @return list of closing prices in chronological order
     */
    public List<Double> fetchClosingPrices(String ticker, int days) {
        // Yahoo Finance range: "1y" covers ~252 trading days
        String range = days <= 30 ? "1mo" : days <= 90 ? "3mo" : days <= 180 ? "6mo" : "1y";
        String url = ticker.toUpperCase() + "?interval=1d&range=" + range;

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return parseClosingPrices(response);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch data for ticker: " + ticker, e);
        }
    }

    /**
     * Fetches the company short name for the given ticker.
     */
    public String fetchCompanyName(String ticker) {
        String url = ticker.toUpperCase() + "?interval=1d&range=1d";
        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonObject chart = root.getAsJsonObject("chart");
            JsonArray results = chart.getAsJsonArray("result");
            if (results != null && results.size() > 0) {
                JsonObject meta = results.get(0).getAsJsonObject().getAsJsonObject("meta");
                if (meta.has("shortName")) {
                    return meta.get("shortName").getAsString();
                }
                if (meta.has("longName")) {
                    return meta.get("longName").getAsString();
                }
            }
        } catch (Exception e) {
            // Non-critical — fall back to ticker symbol
        }
        return ticker.toUpperCase();
    }

    private List<Double> parseClosingPrices(String json) {
        List<Double> prices = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject chart = root.getAsJsonObject("chart");

        JsonElement errorElement = chart.get("error");
        if (errorElement != null && !errorElement.isJsonNull()) {
            throw new RuntimeException("Yahoo Finance API error: " + errorElement.toString());
        }

        JsonArray results = chart.getAsJsonArray("result");
        if (results == null || results.size() == 0) {
            throw new RuntimeException("No data returned for ticker");
        }

        JsonObject result = results.get(0).getAsJsonObject();
        JsonObject indicators = result.getAsJsonObject("indicators");
        JsonArray quote = indicators.getAsJsonArray("quote");

        if (quote == null || quote.size() == 0) {
            throw new RuntimeException("No quote data found");
        }

        JsonArray closes = quote.get(0).getAsJsonObject().getAsJsonArray("close");
        for (JsonElement el : closes) {
            if (!el.isJsonNull()) {
                prices.add(el.getAsDouble());
            }
        }

        return prices;
    }
}
