# Stock Risk Dashboard — Architecture

## Overview

A real-time stock analysis dashboard that calculates and displays risk metrics (log returns, annualized volatility) for any stock ticker using the Yahoo Finance API.

---

## Tech Stack

| Layer      | Technology                                   |
|------------|----------------------------------------------|
| Backend    | Java 21, Spring Boot 3.3.0, Maven            |
| Frontend   | Angular 18, TypeScript, Bootstrap 5.3        |
| Data       | Yahoo Finance API (query1.finance.yahoo.com) |
| Container  | Docker (openjdk:21-jdk base image)           |
| CI/CD      | GitHub Actions → Docker Hub                  |

---

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   GitHub Actions CI/CD                  │
│  Trigger: push/PR to master                             │
│  Steps: Checkout → Maven Build → Docker Build → Push    │
│  Registry: Docker Hub (sheetal2211/demo-app:latest)     │
└──────────────────────┬──────────────────────────────────┘
                       │ deploys
┌──────────────────────▼──────────────────────────────────┐
│              Backend — Spring Boot (Port 8080)          │
│                                                         │
│  StockRiskController  GET /api/stock/{ticker}/risk      │
│         │                                               │
│  StockRiskService     computeLogReturns()               │
│         │             annualizedVolatility()            │
│         │                                               │
│  YahooFinanceClient   fetchClosingPrices()              │
│                       fetchCompanyName()                │
│                       → query1.finance.yahoo.com        │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP REST  (CORS: localhost:4200)
┌──────────────────────▼──────────────────────────────────┐
│              Frontend — Angular 18 (Port 4200)          │
│                                                         │
│  AppComponent         ticker input + period selector    │
│         │             displays metrics cards            │
│         │             mini bar chart (log returns)      │
│  StockService         HTTP GET /api/stock/{t}/risk      │
└─────────────────────────────────────────────────────────┘
```

---

## API Endpoints

| Method | Path                          | Description                        |
|--------|-------------------------------|------------------------------------|
| GET    | `/api/stock/{ticker}/risk`    | Returns risk metrics for a ticker  |
| GET    | `/api/stock/health`           | Health check                       |

### Query Parameters — `/api/stock/{ticker}/risk`

| Param    | Type | Default | Range    | Description             |
|----------|------|---------|----------|-------------------------|
| `period` | int  | `252`   | 10–504   | Trading days to analyze |

### Response Schema

```json
{
  "ticker": "AAPL",
  "companyName": "Apple Inc.",
  "period": 252,
  "volatility": 0.2847,
  "averageLogReturn": 0.0008,
  "latestPrice": 189.50,
  "logReturns": [0.012, -0.005, ...]
}
```

---

## Risk Metrics

| Metric            | Formula                              | Default Period |
|-------------------|--------------------------------------|----------------|
| Log Return        | `ln(P_t / P_{t-1})` per trading day  | 252 days       |
| Annualized Vol    | `stddev(log_returns) × √252`         | 252 days       |
| Risk Level        | Vol < 15% Low, 15–30% Moderate, 30–50% High, >50% Very High | — |

---

## CI/CD Pipeline

**Workflow file:** `.github/workflows/build-app-workflow.yml`

| Step                | Action                                           |
|---------------------|--------------------------------------------------|
| Trigger             | Push to `master` or PR targeting `master`        |
| Checkout            | `actions/checkout@v4`                            |
| JDK Setup           | `actions/setup-java@v4` (Java 21, Temurin + cache) |
| Maven Build         | `mvn -B package` from `./backend` directory      |
| Docker Login        | `docker/login-action@v3`                         |
| Docker Build & Push | `docker/build-push-action@v6` → `sheetal2211/demo-app:latest` (push only on merge to master) |

---

## Frontend Structure

```
frontend/src/app/
├── app.component.ts       # Root component — search + display logic
├── app.component.html     # UI: search form, metrics cards, log return chart
├── app.component.css      # Component styles
└── stock.service.ts       # HTTP client for backend API
```

## Backend Structure

```
backend/src/main/java/com/stockrisk/
├── StockRiskApplication.java          # Spring Boot entry point + CORS config
├── controller/StockRiskController.java # REST endpoints + validation
├── service/StockRiskService.java       # Log return & volatility calculations
├── client/YahooFinanceClient.java      # Yahoo Finance API integration
└── model/StockRiskResponse.java        # Response DTO
```
