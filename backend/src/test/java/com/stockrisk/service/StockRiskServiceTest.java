package com.stockrisk.service;

import com.stockrisk.client.YahooFinanceClient;
import com.stockrisk.model.StockRiskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockRiskServiceTest {

    @Mock
    private YahooFinanceClient yahooFinanceClient;

    private StockRiskService service;

    @BeforeEach
    void setUp() {
        service = new StockRiskService(yahooFinanceClient);
    }

    @Test
    void computeLogReturns_calculatesCorrectly() {
        List<Double> prices = Arrays.asList(100.0, 110.0, 105.0, 115.5);
        List<Double> returns = service.computeLogReturns(prices);

        assertThat(returns).hasSize(3);
        assertThat(returns.get(0)).isCloseTo(Math.log(110.0 / 100.0), within(1e-10));
        assertThat(returns.get(1)).isCloseTo(Math.log(105.0 / 110.0), within(1e-10));
        assertThat(returns.get(2)).isCloseTo(Math.log(115.5 / 105.0), within(1e-10));
    }

    @Test
    void computeLogReturns_withSinglePrice_returnsEmpty() {
        List<Double> returns = service.computeLogReturns(List.of(100.0));
        assertThat(returns).isEmpty();
    }

    @Test
    void annualizedVolatility_withConstantReturns_returnsZero() {
        List<Double> constantReturns = Arrays.asList(0.01, 0.01, 0.01, 0.01);
        double vol = service.annualizedVolatility(constantReturns, 252);
        assertThat(vol).isCloseTo(0.0, within(1e-10));
    }

    @Test
    void annualizedVolatility_withVaryingReturns_isPositive() {
        List<Double> returns = Arrays.asList(0.01, -0.02, 0.03, -0.01, 0.02);
        double vol = service.annualizedVolatility(returns, 252);
        assertThat(vol).isGreaterThan(0.0);
    }

    @Test
    void calculateRisk_returnsCorrectStructure() {
        List<Double> mockPrices = Arrays.asList(100.0, 102.0, 101.0, 103.0, 105.0);
        when(yahooFinanceClient.fetchClosingPrices(eq("AAPL"), anyInt())).thenReturn(mockPrices);
        when(yahooFinanceClient.fetchCompanyName("AAPL")).thenReturn("Apple Inc.");

        StockRiskResponse response = service.calculateRisk("AAPL", 252);

        assertThat(response.getTicker()).isEqualTo("AAPL");
        assertThat(response.getCompanyName()).isEqualTo("Apple Inc.");
        assertThat(response.getPeriod()).isEqualTo(4); // 5 prices → 4 returns
        assertThat(response.getVolatility()).isGreaterThanOrEqualTo(0.0);
        assertThat(response.getLogReturns()).hasSize(4);
        assertThat(response.getLatestPrice()).isEqualTo(105.0);
    }

    @Test
    void calculateRisk_withInsufficientData_throwsException() {
        when(yahooFinanceClient.fetchClosingPrices(anyString(), anyInt())).thenReturn(List.of(100.0));
        when(yahooFinanceClient.fetchCompanyName(anyString())).thenReturn("Test Corp");

        assertThatThrownBy(() -> service.calculateRisk("TEST", 252))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough price data");
    }

    @Test
    void calculateRisk_tickerIsUppercased() {
        List<Double> mockPrices = Arrays.asList(100.0, 102.0, 104.0);
        when(yahooFinanceClient.fetchClosingPrices(eq("msft"), anyInt())).thenReturn(mockPrices);
        when(yahooFinanceClient.fetchCompanyName("msft")).thenReturn("Microsoft Corporation");

        StockRiskResponse response = service.calculateRisk("msft", 252);

        assertThat(response.getTicker()).isEqualTo("MSFT");
    }
}
