import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StockRiskResponse {
  ticker: string;
  companyName: string;
  period: number;
  volatility: number;
  averageLogReturn: number;
  latestPrice: number;
  logReturns: number[];
}

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private readonly apiBase = '/api/stock';

  constructor(private http: HttpClient) {}

  getStockRisk(ticker: string, period: number = 252): Observable<StockRiskResponse> {
    const params = new HttpParams().set('period', period.toString());
    return this.http.get<StockRiskResponse>(`${this.apiBase}/${ticker}/risk`, { params });
  }
}
