import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService } from './services/stock.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Stock Risk Dashboard';

  searchQuery: string = '';
  selectedDays: number = 252;

  metrics: any | null = null;
  isLoading: boolean = false;
  error: string | null = null;

  periodOptions = [
    { label: '1 Month (20 days)', value: 20 },
    { label: '3 Months (60 days)', value: 60 },
    { label: '1 Year (252 days)', value: 252 },
    { label: '5 Years (1260 days)', value: 1260 }
  ];

  constructor(private stockService: StockService) { }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.error = 'Please enter a stock symbol';
      return;
    }

    console.log('Starting search for:', this.searchQuery);
    this.isLoading = true;
    this.error = null;
    this.metrics = null;

    this.stockService.getStockMetrics(this.searchQuery.toUpperCase(), this.selectedDays)
      .subscribe({
        next: (result) => {
          console.log('Success! Got metrics:', result);
          this.metrics = result;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('API call failed:', err);
          this.error = this.getErrorMessage(err);
          this.isLoading = false;
        },
        complete: () => {
          console.log('API call completed');
        }
      });
  }

  onPeriodChange(days: number): void {
    this.selectedDays = days;
    if (this.metrics) {
      this.onSearch();
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.onSearch();
    }
  }

  clearResults(): void {
    this.metrics = null;
    this.error = null;
    this.searchQuery = '';
  }

  /**
   * Extract error message from various error types
   */
  private getErrorMessage(err: any): string {
    console.error('Full error object:', err);
    console.error('Error status:', err.status);
    console.error('Error statusText:', err.statusText);
    console.error('Error message:', err.message);
    console.error('Error error:', err.error);

    // CORS preflight failure or actual connection error
    if (err.status === 0 && err.error instanceof ProgressEvent) {
      console.error('CORS or preflight error detected');
      return 'Error: Backend not accessible. Ensure http://localhost:8080 is running. Check browser console for details.';
    }

    // Network error without status
    if (err.status === 0) {
      return 'Error: Cannot reach backend at http://localhost:8080. Make sure it is running.';
    }

    // Backend returned an error response (200-599 status)
    if (err.status && err.status >= 200 && err.status < 600) {
      // Try to extract error message from response
      if (typeof err.error === 'string' && err.error.trim()) {
        return 'Backend Error (' + err.status + '): ' + err.error;
      } else if (err.error?.message) {
        return 'Backend Error (' + err.status + '): ' + err.error.message;
      } else if (err.statusText) {
        return 'Backend Error (' + err.status + '): ' + err.statusText;
      }
    }

    // Parse error (invalid JSON response)
    if (err.name === 'SyntaxError' || err.message?.includes('JSON')) {
      return 'Error: Backend returned invalid data. Check that backend is running correctly.';
    }

    // Timeout error
    if (err.name === 'TimeoutError' || err.message?.includes('timeout')) {
      return 'Error: Request timed out. Backend may be slow. Try again.';
    }

    // Fallback with detailed info
    let fallbackMsg = 'Error fetching stock data';
    if (typeof err.error === 'string' && err.error.trim()) {
      fallbackMsg += ': ' + err.error;
    } else if (err.message) {
      fallbackMsg += ': ' + err.message;
    } else if (err.statusText) {
      fallbackMsg += ': ' + err.statusText;
    }

    return fallbackMsg;
  }
}
