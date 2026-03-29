import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StockMetrics {
  symbol: string;
  logReturns: number;
  volatility: number;
  period: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = 'http://localhost:8080/api/stocks';
  private backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /**
   * Check if backend is accessible
   */
  isBackendAvailable(): Observable<boolean> {
    return new Observable(observer => {
      this.http.get(`${this.backendUrl}/api/stocks/AAPL/metrics`, {
        responseType: 'json',
        withCredentials: false
      }).subscribe(
        () => observer.next(true),
        () => observer.next(false),
        () => observer.complete()
      );
    });
  }

  getStockMetrics(symbol: string, days: number = 252): Observable<StockMetrics> {
    const url = `${this.apiUrl}/${symbol}/metrics?days=${days}`;
    console.log('StockService: Requesting URL:', url);
    console.log('StockService: Full URL:', `http://localhost:8080/api/stocks/${symbol}/metrics?days=${days}`);

    return this.http.get<StockMetrics>(url, {
      withCredentials: false
    });
  }

  searchStocks(query: string): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.apiUrl}/search?query=${query}`,
      { withCredentials: false }
    );
  }
}
