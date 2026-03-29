package com.stockrisk.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockServiceTest {

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService();
    }

    @Test
    void calculateLogReturns_returnsCorrectValues() {
        List<Double> prices = Arrays.asList(100.0, 110.0, 105.0, 115.0);
        List<Double> logReturns = stockService.calculateLogReturns(prices);

        assertEquals(3, logReturns.size());
        assertEquals(Math.log(110.0 / 100.0), logReturns.get(0), 1e-9);
        assertEquals(Math.log(105.0 / 110.0), logReturns.get(1), 1e-9);
        assertEquals(Math.log(115.0 / 105.0), logReturns.get(2), 1e-9);
    }

    @Test
    void calculateLogReturns_singlePairReturnsOneValue() {
        List<Double> prices = Arrays.asList(50.0, 55.0);
        List<Double> logReturns = stockService.calculateLogReturns(prices);

        assertEquals(1, logReturns.size());
        assertEquals(Math.log(55.0 / 50.0), logReturns.get(0), 1e-9);
    }

    @Test
    void calculateAnnualizedVolatility_constantReturnsZeroVolatility() {
        // If all returns are equal, std dev = 0, volatility = 0
        List<Double> constantReturns = Arrays.asList(0.01, 0.01, 0.01, 0.01, 0.01);
        double volatility = stockService.calculateAnnualizedVolatility(constantReturns);

        assertEquals(0.0, volatility, 1e-9);
    }

    @Test
    void calculateAnnualizedVolatility_knownValues() {
        // Simple case: two values with known std dev
        List<Double> returns = Arrays.asList(0.1, -0.1);
        double volatility = stockService.calculateAnnualizedVolatility(returns);

        // std dev of [0.1, -0.1] = 0.1 * sqrt(2) / sqrt(1) = 0.1414...
        // annualized = std_dev * sqrt(252)
        double expectedStdDev = Math.sqrt(Math.pow(0.1 - 0.0, 2) + Math.pow(-0.1 - 0.0, 2));
        // sample std dev with n-1 denominator
        double sampleStdDev = Math.sqrt((Math.pow(0.1, 2) + Math.pow(0.1, 2)) / 1.0);
        double expectedVolatility = sampleStdDev * Math.sqrt(252);

        assertEquals(expectedVolatility, volatility, 1e-9);
    }

    @Test
    void calculateLogReturns_emptyWhenSinglePrice() {
        List<Double> prices = Arrays.asList(100.0);
        List<Double> logReturns = stockService.calculateLogReturns(prices);
        assertTrue(logReturns.isEmpty());
    }
}
