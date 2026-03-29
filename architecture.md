# Stock Risk Dashboard — Architecture

## Overview

A real-time stock analysis dashboard that calculates and displays risk metrics (log returns, volatility) for any stock ticker using the Yahoo Finance API.

---

## Tech Stack

| Layer      | Technology                                   |
|------------|----------------------------------------------|
| Backend    | Java 21, Spring Boot 3.3.0, Maven            |
| Frontend   | Angular 18, TypeScript, Bootstrap 5.3        |
| Data       | Yahoo Finance API                            |
| Container  | Docker (openjdk:21-jdk base image)           |
| CI/CD      | GitHub Actions → Docker Hub                  |

---

## System Components

```
┌─────────────────────────────────────────────────────────┐
│                   GitHub Actions CI/CD                  │
│  Trigger: push/PR to master                             │
│  Steps: Checkout → Maven Build → Docker Build → Push    │
│  Registry: Docker Hub (sheetal2211/demo-app:latest)     │
└──────────────────────┬──────────────────────────────────┘
                       │ deploys
┌──────────────────────▼──────────────────────────────────┐
│                     Backend (Port 8080)                 │
│  Spring Boot REST API                                   │
│  GET /api/stock/{ticker}/risk                           │
│  ├── StockRiskController                                │
│  ├── StockRiskService  (log returns, volatility calc)   │
│  └── YahooFinanceClient (data fetching)                 │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/REST (CORS: localhost:4200)
┌──────────────────────▼──────────────────────────────────┐
│                    Frontend (Port 4200)                 │
│  Angular 18 SPA                                         │
│  ├── StockSearchComponent  (ticker input)               │
│  ├── RiskMetricsComponent  (log return, volatility)     │
│  └── StockService          (HTTP client)                │
└─────────────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline (Issue #4)

**Workflow file:** `.github/workflows/build-app-workflow.yml`

| Step                  | Action                                           |
|-----------------------|--------------------------------------------------|
| Trigger               | Push to `master` or PR targeting `master`        |
| Checkout              | `actions/checkout@v4`                            |
| JDK Setup             | `actions/setup-java@v4` (Java 21, Temurin)       |
| Maven Build           | `mvn -B package` from `./backend` directory      |
| Docker Login          | `docker/login-action@v3` (DOCKERHUB_USERNAME/PASSWORD) |
| Docker Build & Push   | `docker/build-push-action@v6` → `sheetal2211/demo-app:latest` |

> Note: Docker push only runs on direct pushes to `master` (not on PR builds).

---

## Risk Metrics

| Metric      | Formula                                              | Default Period |
|-------------|------------------------------------------------------|----------------|
| Log Return  | `ln(P_t / P_{t-1})` per trading day                 | 252 days       |
| Volatility  | `stddev(log_returns) × √252` (annualized)            | 252 days       |
| Period      | Number of trading days analyzed                      | 252            |
