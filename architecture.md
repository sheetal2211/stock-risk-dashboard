# Stock Risk Dashboard - Architecture

## Overview

A real-time stock analysis dashboard that fetches historical price data from Yahoo Finance and calculates key risk metrics: **log returns**, **annualized volatility**, and **analysis period**.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        USER BROWSER                          │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Angular 18 Frontend (localhost:4200)         │   │
│  │                                                      │   │
│  │  ┌──────────────┐    ┌──────────────────────────┐   │   │
│  │  │ AppComponent │───▶│   DashboardComponent     │   │   │
│  │  └──────────────┘    │  - Stock symbol search   │   │   │
│  │                      │  - Metrics display       │   │   │
│  │                      │  - Price sparkline       │   │   │
│  │                      │  - Log returns table     │   │   │
│  │                      └──────────┬───────────────┘   │   │
│  │                                 │                    │   │
│  │                      ┌──────────▼───────────────┐   │   │
│  │                      │      StockService         │   │   │
│  │                      │  HTTP GET /api/stock/     │   │   │
│  │                      │  {symbol}/metrics        │   │   │
│  │                      └──────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP REST (localhost:8080)
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Spring Boot Backend (Java 21, port 8080)        │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   StockController                    │   │
│  │  GET /api/stock/{symbol}/metrics                     │   │
│  │  GET /api/stock/health                               │   │
│  └────────────────────────┬─────────────────────────────┘   │
│                           │                                 │
│  ┌────────────────────────▼─────────────────────────────┐   │
│  │                   StockService                       │   │
│  │                                                      │   │
│  │  1. fetchFromYahooFinance(symbol)                    │   │
│  │  2. parseAndCalculate(symbol, response)              │   │
│  │  3. calculateLogReturns(prices)                      │   │
│  │  4. calculateAnnualizedVolatility(logReturns)        │   │
│  └────────────────────────┬─────────────────────────────┘   │
│                           │                                 │
│  ┌────────────────────────▼─────────────────────────────┐   │
│  │               StockMetrics (Response DTO)            │   │
│  │  - symbol, companyName, currentPrice                 │   │
│  │  - volatility (annualized %)                         │   │
│  │  - averageLogReturn                                  │   │
│  │  - period (trading days)                             │   │
│  │  - logReturns[], dates[], closePrices[]              │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS (Yahoo Finance v8 API)
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Yahoo Finance API (External)                    │
│  query1.finance.yahoo.com/v8/finance/chart/{symbol}         │
│  Parameters: range=1y, interval=1d                          │
│  Returns: timestamps, OHLCV data (~252 trading days)        │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Details

### Backend (`/backend`)

| Component | Class | Responsibility |
|-----------|-------|---------------|
| Controller | `StockController` | REST API entry point, request validation, error mapping |
| Service | `StockService` | Yahoo Finance fetch, log return & volatility calculation |
| Model | `StockMetrics` | Response DTO with all calculated metrics |
| Exception | `StockDataException` | Domain-specific error for bad symbol or API failure |

**Key Calculations:**
- **Log Return**: `ln(P_t / P_{t-1})` for each consecutive trading day
- **Annualized Volatility**: `std_dev(logReturns) × √252` expressed as a percentage
- **Period**: count of log return data points (typically ~251 for 1-year range)

### Frontend (`/frontend`)

| Component | File | Responsibility |
|-----------|------|---------------|
| App | `app.component.ts` | Root component with navbar |
| Dashboard | `dashboard/dashboard.component.ts` | Stock search, metrics display, chart |
| Service | `services/stock.service.ts` | HTTP calls to backend REST API |
| Model | `models/stock-metrics.model.ts` | TypeScript interface for API response |

---

## API Reference

### `GET /api/stock/{symbol}/metrics`

Returns calculated risk metrics for the given stock symbol.

**Response (200 OK):**
```json
{
  "symbol": "AAPL",
  "companyName": "Apple Inc.",
  "currentPrice": 172.45,
  "volatility": 24.87,
  "averageLogReturn": 0.0009,
  "period": 251,
  "logReturns": [0.0123, -0.0045, ...],
  "dates": ["2024-03-28", ...],
  "closePrices": [172.45, 169.12, ...]
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "No data found for symbol: INVALID"
}
```

### `GET /api/stock/health`

Health check endpoint. Returns `200 OK` with plain text message.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Runtime | Java 21 |
| Backend Framework | Spring Boot 3.3.0 |
| Build Tool | Maven |
| JSON Parsing | Gson 2.10.1 |
| Frontend Framework | Angular 18 |
| Frontend Language | TypeScript 5.4 |
| UI Library | Bootstrap 5.3 |
| External Data | Yahoo Finance v8 API |
| Containerization | Docker (eclipse-temurin:21-jdk-jammy) |
| CI/CD | GitHub Actions |

---

## Data Flow

```
User types "AAPL"
    │
    ▼
DashboardComponent.search()
    │
    ▼
StockService.getStockMetrics("AAPL")
    │  HTTP GET localhost:8080/api/stock/AAPL/metrics
    ▼
StockController.getStockMetrics("AAPL")
    │
    ▼
StockService.fetchFromYahooFinance("AAPL")
    │  HTTP GET query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1y&interval=1d
    ▼
Yahoo Finance API returns ~252 daily OHLCV records
    │
    ▼
StockService.parseAndCalculate()
    ├── Extract close prices and timestamps
    ├── calculateLogReturns() → ln(P_t/P_{t-1}) for each day
    └── calculateAnnualizedVolatility() → std_dev × √252
    │
    ▼
StockMetrics DTO serialized as JSON
    │
    ▼
Angular renders metrics cards, price sparkline, returns table
```
