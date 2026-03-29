package com.stockrisk.service;

import com.stockrisk.model.StockMetrics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
public class StockMetricsService {

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 3000;
    private static final Gson gson = new GsonBuilder().setLenient().create();

    /**
     * Fetch stock data directly from Yahoo Finance API with proper headers
     */
    public StockMetrics calculateMetrics(String symbol, int days) throws IOException {
        List<Double> prices = fetchHistoricalPrices(symbol, days);

        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Could not fetch data for stock symbol: " + symbol);
        }

        // Calculate daily log returns
        List<Double> logReturns = calculateLogReturns(prices);

        // Calculate metrics
        double avgLogReturn = logReturns.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        double volatility = calculateAnnualizedVolatility(logReturns);

        return new StockMetrics(
            symbol.toUpperCase(),
            Math.round(avgLogReturn * 100.0) / 100.0,
            Math.round(volatility * 100.0) / 100.0,
            prices.size() + " days",
            LocalDateTime.now()
        );
    }

    /**
     * Fetch historical prices from Yahoo Finance API
     */
    private List<Double> fetchHistoricalPrices(String symbol, int days) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    long delayMs = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 2);
                    System.out.println("Attempt " + attempt + " - Waiting " + delayMs + "ms before retry...");
                    Thread.sleep(delayMs);
                }

                System.out.println("Fetching stock data for " + symbol + " (attempt " + attempt + "/" + MAX_RETRIES + ")");
                List<Double> prices = fetchPricesDirectAPI(symbol, days);

                if (prices != null && !prices.isEmpty()) {
                    System.out.println("Successfully fetched " + prices.size() + " price points for " + symbol);
                    return prices;
                }

            } catch (Exception e) {
                lastException = e instanceof IOException ? (IOException) e : new IOException(e);
                System.out.println("Attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < MAX_RETRIES) {
                    System.out.println("Retrying...");
                }
            }
        }

        String errorMsg = lastException != null ? lastException.getMessage() : "Unknown error";
        throw new IOException("Failed to fetch stock data after " + MAX_RETRIES + " attempts. Error: " + errorMsg);
    }

    /**
     * Direct API call to Yahoo Finance using URLConnection
     */
    private List<Double> fetchPricesDirectAPI(String symbol, int days) throws IOException {
        try {
            // Build URL for Yahoo Finance API
            long endTime = System.currentTimeMillis() / 1000;
            long startTime = endTime - (days * 86400L); // days in seconds

            String urlString = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d",
                symbol.toUpperCase(), startTime, endTime
            );

            System.out.println("Calling API: " + urlString);

            // Use URLConnection for better control
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            // Set headers to mimic browser request
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Referer", "https://finance.yahoo.com/");
            connection.setRequestProperty("Sec-Fetch-Dest", "empty");
            connection.setRequestProperty("Sec-Fetch-Mode", "cors");
            connection.setRequestProperty("Sec-Fetch-Site", "same-site");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Get response code
            int responseCode = ((java.net.HttpURLConnection) connection).getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode + ": " + ((java.net.HttpURLConnection) connection).getResponseMessage());
            }

            // Read response body
            String response = readResponse(connection);

            if (response == null || response.isEmpty()) {
                throw new IOException("Empty response from API");
            }

            // Log first 500 chars of response for debugging
            String preview = response.length() > 500 ? response.substring(0, 500) : response;
            System.out.println("Response preview: " + preview);

            return parseYahooFinanceResponse(response);

        } catch (Exception e) {
            System.out.println("Error fetching prices: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to fetch stock prices: " + e.getMessage(), e);
        }
    }

    /**
     * Read response from URLConnection with gzip support
     */
    private String readResponse(URLConnection connection) throws IOException {
        java.io.InputStream inputStream = connection.getInputStream();

        // Handle gzip compression
        String encoding = connection.getContentEncoding();
        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }

        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * Parse Yahoo Finance API JSON response
     */
    private List<Double> parseYahooFinanceResponse(String jsonResponse) throws IOException {
        try {
            // Use lenient parsing for malformed JSON
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);

            if (root == null) {
                throw new IOException("Failed to parse JSON root object");
            }

            // Check for error in response
            if (root.has("chart")) {
                JsonObject chart = root.getAsJsonObject("chart");

                if (chart.has("error") && !chart.get("error").isJsonNull()) {
                    JsonObject error = chart.getAsJsonObject("error");
                    String errorMsg = error.has("description") ? error.get("description").getAsString() : "Unknown error";
                    throw new IOException("API Error: " + errorMsg);
                }

                if (chart.has("result") && !chart.get("result").isJsonNull()) {
                    JsonArray results = chart.getAsJsonArray("result");

                    if (results != null && results.size() > 0) {
                        JsonObject quote = results.get(0).getAsJsonObject();

                        if (quote.has("indicators") && !quote.get("indicators").isJsonNull()) {
                            JsonObject indicators = quote.getAsJsonObject("indicators");

                            if (indicators.has("quote") && !indicators.get("quote").isJsonNull()) {
                                JsonArray quotes = indicators.getAsJsonArray("quote");

                                if (quotes != null && quotes.size() > 0) {
                                    JsonObject quoteData = quotes.get(0).getAsJsonObject();

                                    if (quoteData.has("close") && !quoteData.get("close").isJsonNull()) {
                                        JsonArray closes = quoteData.getAsJsonArray("close");

                                        // Extract valid prices (filter out nulls)
                                        List<Double> prices = new ArrayList<>();
                                        for (JsonElement elem : closes) {
                                            if (elem != null && !elem.isJsonNull()) {
                                                try {
                                                    prices.add(elem.getAsDouble());
                                                } catch (Exception e) {
                                                    // Skip invalid prices
                                                }
                                            }
                                        }

                                        if (!prices.isEmpty()) {
                                            System.out.println("Parsed " + prices.size() + " prices from response");
                                            return prices;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            throw new IOException("Could not extract price data from API response");

        } catch (com.google.gson.JsonSyntaxException e) {
            System.out.println("JSON Parse error: " + e.getMessage());
            throw new IOException("Invalid JSON in API response: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("Parse error: " + e.getMessage());
            throw new IOException("Failed to parse API response: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate daily log returns from prices
     */
    private List<Double> calculateLogReturns(List<Double> prices) {
        List<Double> returns = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double price_t = prices.get(i);
            double price_t_minus_1 = prices.get(i - 1);

            if (price_t_minus_1 > 0 && price_t > 0) {
                double logReturn = Math.log(price_t / price_t_minus_1) * 100;
                returns.add(logReturn);
            }
        }

        return returns;
    }

    /**
     * Calculate annualized volatility from log returns
     */
    private double calculateAnnualizedVolatility(List<Double> returns) {
        if (returns.isEmpty()) return 0.0;

        double mean = returns.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average()
            .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // Annualize: daily volatility * sqrt(252 trading days)
        return stdDev * Math.sqrt(252);
    }
}
