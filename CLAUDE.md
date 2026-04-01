# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Stock Risk Dashboard** is a minimal single-page application that calculates and displays stock risk metrics (log returns and volatility). Built with Java 21 + Spring Boot backend and Angular 18+ frontend using Yahoo Finance API for market data.

**Core Value**: Given a stock symbol, calculate and display:
- **Log Returns**: `ln(price_t / price_t-1) * 100` - daily percentage changes
- **Volatility**: `std_dev(returns) * sqrt(252) * 100` - annualized standard deviation

## Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────┐
│  Angular Frontend (Port 4200) - Single Page App         │
│  - Stock search input                                   │
│  - Display metrics in cards                            │
│  - Bootstrap CDN styling                               │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP calls
                       │ CORS enabled
┌──────────────────────▼──────────────────────────────────┐
│  Spring Boot Backend (Port 8080) - REST API            │
│  - GET /api/stocks/{symbol}/metrics                    │
│  - Yahoo Finance integration (fetch prices)            │
│  - Calculate log returns & volatility in-memory        │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ HTTP
┌──────────────────────▼──────────────────────────────────┐
│  Yahoo Finance API (yfinance-java 3.17.0)              │
│  - Provides historical price data                       │
│  - No authentication required                          │
└─────────────────────────────────────────────────────────┘
```

### Data Flow

1. **Frontend**: User enters stock symbol (e.g., "AAPL")
2. **StockService**: Makes `GET /api/stocks/AAPL/metrics?days=252`
3. **Backend StockController**: Routes request to StockMetricsService
4. **StockMetricsService**:
   - Fetches 252 days of historical prices via YahooFinance API
   - Calculates daily log returns (List<Double>)
   - Calculates volatility from returns standard deviation
   - Returns StockMetrics JSON
5. **Frontend**: Displays metrics in component

### Key Implementation Details

**Backend Service Layers**:
- `StockController`: REST endpoints (GET /api/stocks/{symbol}/metrics)
- `StockMetricsService`: Business logic for calculations
  - `calculateLogReturns()`: Computes log returns from price history
  - `calculateAnnualizedVolatility()`: Computes std_dev(returns) * sqrt(252)
- `StockRiskApplication`: Spring Boot entry point with CORS configuration
- `StockMetrics`: Data model (symbol, logReturns, volatility, period, timestamp)

**Frontend Service Layer**:
- `StockService`: HTTP client for API communication
  - `getStockMetrics(symbol, days)`: Returns Observable<StockMetrics>
- `AppComponent`: Main component with search logic and results display

**CORS Configuration**: Allows requests from `http://localhost:4200` to backend on port 8080. Update CORS origin in `StockRiskApplication.java` if frontend runs on different port.

## Development Commands

### Backend

```bash
# Navigate to backend
cd backend

# Build project
mvn clean compile

# Run development server (auto-reload on code changes via spring-boot-devtools)
mvn spring-boot:run

# Run in production mode
mvn -Dspring.profiles.active=prod spring-boot:run

# Run tests
mvn test

# Run specific test
mvn test -Dtest=StockMetricsServiceTest

# Package for deployment
mvn clean package
```

**Backend runs on**: `http://localhost:8080/api`

### Frontend

```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Run development server
npm start
# OR
ng serve

# Run development server on custom port
ng serve --port 5000

# Build for production
npm run build:prod

# Run tests
npm test

# Run linting
npm run lint

# Build and serve production build locally
npm run build:prod && npx http-server dist
```

**Frontend runs on**: `http://localhost:4200`

## Testing

### Backend Testing

```bash
# Run all tests
mvn test

# Run with verbose output
mvn test -X

# Test specific class
mvn test -Dtest=StockMetricsServiceTest

# Test with coverage
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html
```

**Test Location**: `backend/src/test/java/com/stockrisk/`

### Frontend Testing

```bash
# Run tests with Jasmine/Karma
npm test

# Run tests with code coverage
ng test --code-coverage

# Run single test file
ng test --include='**/app.component.spec.ts'
```

**Test Location**: `frontend/src/app/**/*.spec.ts`

### Manual Integration Testing

```bash
# Test backend API directly
curl "http://localhost:8080/api/stocks/AAPL/metrics?days=252"

# Expected response:
# {"symbol":"AAPL","logReturns":2.45,"volatility":28.5,"period":"252 days","timestamp":"..."}

# Test with different periods
curl "http://localhost:8080/api/stocks/MSFT/metrics?days=60"

# Test invalid symbol (should return error)
curl "http://localhost:8080/api/stocks/INVALID123/metrics"
```

## Deployment

### Docker

```bash
# Build both Docker images
docker-compose build

# Start all services with docker-compose
docker-compose up -d

# View service status
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Stop all services
docker-compose down

# Build specific image
docker build -f backend/Dockerfile -t stock-risk-backend:latest ./backend
docker build -f frontend/Dockerfile -t stock-risk-frontend:latest ./frontend
```

**Ports**:
- Backend container: 8080 → 8080
- Frontend container: 80 → 80

**Docker Healthchecks**:
- Backend: Checks `/api/stocks/AAPL/metrics` endpoint
- Frontend: Checks root HTTP response
- Frontend depends on backend health before starting

### Local Development (Without Docker)

```bash
# Terminal 1: Backend
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080

# Terminal 2: Frontend
cd frontend
npm install
ng serve
# Runs on http://localhost:4200
```

## Important Files & Directories

| Path | Purpose |
|------|---------|
| `backend/pom.xml` | Maven dependencies and build config |
| `backend/src/main/resources/application.yml` | Spring Boot server config (port 8080, context path /api) |
| `backend/src/main/java/com/stockrisk/` | Backend Java classes (controller, service, model) |
| `frontend/package.json` | Node dependencies and npm scripts |
| `frontend/src/app/app.component.ts` | Main Angular component (search logic) |
| `frontend/src/app/services/stock.service.ts` | HTTP service for API calls |
| `docker-compose.yml` | Multi-container orchestration (backend + frontend) |
| `backend/Dockerfile` | Multi-stage build for Spring Boot JAR |
| `frontend/Dockerfile` | Multi-stage build for Angular + Nginx |
| `frontend/nginx.conf` | Nginx configuration with API proxy to backend |

## Configuration

### Backend (application.yml)

```yaml
server:
  port: 8080                          # API server port
  servlet:
    context-path: /api                # All endpoints prefixed with /api
logging:
  level:
    root: INFO                        # Root log level
    com.stockrisk: DEBUG              # Package-specific debug logs
```

### Frontend (environment)

**Development**:
- API URL: `http://localhost:8080/api/stocks`
- StockService uses HttpClient with no special config

**Docker**:
- API URL: `http://backend:8080/api/stocks` (via nginx proxy)
- Nginx proxies `/api` requests to backend service on internal network

### CORS

Backend allows requests from:
- Origin: `http://localhost:4200` (development)
- Methods: GET, POST, PUT, DELETE
- Headers: All (`*`)

**To change CORS origin**: Edit `StockRiskApplication.java` CORS Bean:
```java
.allowedOrigins("http://your-frontend-url")
```

## Critical Implementation Notes

### Log Returns Calculation
- Formula: `log(price_t / price_t-1) * 100`
- Computed for each consecutive day in price history
- Used to measure daily volatility
- Sorted prices chronologically before calculation

### Volatility Calculation
- Step 1: Calculate mean of log returns
- Step 2: Calculate variance = average of (return - mean)²
- Step 3: Calculate std_dev = sqrt(variance)
- Step 4: Annualize = std_dev * sqrt(252) * 100
- 252 = number of trading days in a year

### Yahoo Finance API Integration
- Library: `com.yahoofinance-api:YahooFinanceAPI:3.17.0`
- No authentication required
- Returns: Historical quotes with date, close price
- May be slow on first request (normal behavior)
- Errors: Return null for invalid symbols (check for null)

## Common Workflows

### Adding a New Metric (e.g., Max Drawdown)

1. **Backend**:
   - Add field to `StockMetrics.java` model
   - Add calculation method in `StockMetricsService`
   - Return from controller endpoint

2. **Frontend**:
   - Update `StockMetrics` interface in `stock.service.ts`
   - Display new field in `app.component.html`

### Debugging Backend Issues

1. Check logs: `mvn spring-boot:run` shows stack traces
2. Test API with curl to isolate frontend
3. Verify Yahoo Finance API is accessible (not blocked)
4. Check CORS config if browser shows access-control errors
5. Port conflicts: `lsof -i :8080` to find process on port

### Debugging Frontend Issues

1. Open browser DevTools Console (F12)
2. Check Network tab for API calls
3. Verify API URL in `stock.service.ts`
4. Check CORS response headers
5. Verify backend is running on correct port

## Performance Considerations

- **API Calls**: First request to Yahoo Finance may take 2-5 seconds (normal), subsequent similar requests may be faster
- **Bundle Size**: Angular production build ~500KB (via gzip)
- **Memory**: Backend uses ~256-512MB (configurable via JAVA_OPTS)
- **Caching**: Currently no caching implemented (improvement opportunity)

## Future Enhancements

Based on `README.md` post-MVP roadmap:
- Add time period selector UI (1M, 3M, 1Y, 5Y)
- Add price history chart (Chart.js or Plotly)
- Implement caching layer to reduce API calls
- Add more metrics (Sharpe ratio, max drawdown, beta)
- Authentication/user accounts
- Portfolio management (multiple stocks, weighted analysis)

## Key Dependencies

**Backend**:
- Spring Boot 3.3.0 (Web framework)
- Java 21 LTS (Language)
- YahooFinanceAPI 3.17.0 (Market data)
- Lombok (Boilerplate reduction)

**Frontend**:
- Angular 18.0.0 (Framework)
- TypeScript 5.4 (Language)
- Bootstrap 5.3 via CDN (Styling)
- RxJS 7.8.0 (Reactive programming)

## Related Documentation

- `SDLC_STEPS/` - Step-by-step implementation guides
- `AGENT_EXECUTION_PROMPT.md` - Autonomous agent execution instructions
- `README.md` - Project overview and quick start
