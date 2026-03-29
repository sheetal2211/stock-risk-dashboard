export interface StockMetrics {
  symbol: string;
  companyName: string;
  currentPrice: number;
  volatility: number;          // annualized volatility as percentage
  averageLogReturn: number;    // average daily log return
  period: number;              // number of trading days analyzed
  logReturns: number[];
  dates: string[];
  closePrices: number[];
  error?: string;
}
