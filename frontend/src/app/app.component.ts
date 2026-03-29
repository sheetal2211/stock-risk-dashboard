import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService, StockRiskResponse } from './stock.service';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  readonly Math = Math;
  ticker = '';
  period = 252;
  loading = false;
  error = '';
  result: StockRiskResponse | null = null;

  constructor(private stockService: StockService) {}

  search(): void {
    const t = this.ticker.trim().toUpperCase();
    if (!t) return;

    this.loading = true;
    this.error = '';
    this.result = null;

    this.stockService.getStockRisk(t, this.period).subscribe({
      next: (data) => {
        this.result = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.error ?? 'Failed to fetch stock data. Please try again.';
        this.loading = false;
      }
    });
  }

  get volatilityPercent(): string {
    return this.result ? (this.result.volatility * 100).toFixed(2) + '%' : '';
  }

  get avgReturnPercent(): string {
    return this.result ? (this.result.averageLogReturn * 100).toFixed(4) + '%' : '';
  }

  get riskLabel(): string {
    if (!this.result) return '';
    const v = this.result.volatility;
    if (v < 0.15) return 'Low';
    if (v < 0.30) return 'Moderate';
    if (v < 0.50) return 'High';
    return 'Very High';
  }

  get riskBadgeClass(): string {
    const label = this.riskLabel;
    if (label === 'Low') return 'bg-success';
    if (label === 'Moderate') return 'bg-warning text-dark';
    if (label === 'High') return 'bg-danger';
    return 'bg-dark';
  }
}
