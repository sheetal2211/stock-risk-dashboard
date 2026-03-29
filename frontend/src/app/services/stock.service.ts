import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { StockMetrics } from '../models/stock-metrics.model';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private readonly apiUrl = 'http://localhost:8080/api/stock';

  constructor(private http: HttpClient) {}

  getStockMetrics(symbol: string): Observable<StockMetrics> {
    return this.http.get<StockMetrics>(`${this.apiUrl}/${symbol}/metrics`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let message = 'An unexpected error occurred';
    if (error.status === 0) {
      message = 'Cannot connect to backend. Make sure the server is running on port 8080.';
    } else if (error.status === 400 && error.error?.error) {
      message = error.error.error;
    } else if (error.status === 500) {
      message = 'Server error. Please try again later.';
    }
    return throwError(() => new Error(message));
  }
}
