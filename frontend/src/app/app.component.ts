import { Component } from '@angular/core';
import { DashboardComponent } from './components/dashboard/dashboard.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [DashboardComponent],
  template: `
    <nav class="navbar navbar-dark" style="background-color: #161b22; border-bottom: 1px solid #30363d;">
      <div class="container-fluid px-4">
        <span class="navbar-brand">
          📊 Stock Risk Dashboard
        </span>
        <span class="text-muted small">Powered by Yahoo Finance</span>
      </div>
    </nav>
    <app-dashboard></app-dashboard>
  `
})
export class AppComponent {}
