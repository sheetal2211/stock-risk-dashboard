import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService } from '../../services/stock.service';
import { StockMetrics } from '../../models/stock-metrics.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  symbol = '';
  metrics: StockMetrics | null = null;
  loading = false;
  errorMessage = '';
  recentSymbols: string[] = ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'AMZN'];
  showAllReturns = false;

  constructor(private stockService: StockService) {}

  search(): void {
    const trimmed = this.symbol.trim().toUpperCase();
    if (!trimmed) return;
    this.loadMetrics(trimmed);
  }

  loadMetrics(sym: string): void {
    this.symbol = sym;
    this.loading = true;
    this.errorMessage = '';
    this.metrics = null;
    this.showAllReturns = false;

    this.stockService.getStockMetrics(sym).subscribe({
      next: (data) => {
        if (data.error) {
          this.errorMessage = data.error;
        } else {
          this.metrics = data;
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.loading = false;
      }
    });
  }

  get volatilityClass(): string {
    if (!this.metrics) return '';
    if (this.metrics.volatility > 40) return 'volatility-high';
    if (this.metrics.volatility > 20) return 'volatility-medium';
    return 'volatility-low';
  }

  get volatilityLabel(): string {
    if (!this.metrics) return '';
    if (this.metrics.volatility > 40) return 'HIGH';
    if (this.metrics.volatility > 20) return 'MEDIUM';
    return 'LOW';
  }

  get displayedReturns(): { date: string; value: number }[] {
    if (!this.metrics) return [];
    const entries = this.metrics.dates.map((d, i) => ({
      date: d,
      value: this.metrics!.logReturns[i]
    }));
    return this.showAllReturns ? entries : entries.slice(-20);
  }

  get maxPrice(): number {
    return this.metrics ? Math.max(...this.metrics.closePrices) : 1;
  }

  get minPrice(): number {
    return this.metrics ? Math.min(...this.metrics.closePrices) : 0;
  }

  priceBarHeight(price: number): number {
    const range = this.maxPrice - this.minPrice;
    if (range === 0) return 50;
    return ((price - this.minPrice) / range) * 100;
  }

  trackByIndex(index: number): number {
    return index;
  }
}
