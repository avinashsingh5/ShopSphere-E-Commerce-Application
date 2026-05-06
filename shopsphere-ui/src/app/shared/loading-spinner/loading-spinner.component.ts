import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="spinner-wrapper" *ngIf="show">
      <div class="spinner"></div>
      <p class="spinner-text" *ngIf="message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .spinner-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1rem;
    }

    .spinner {
      width: 36px;
      height: 36px;
      border: 3px solid var(--border-color);
      border-top-color: var(--accent);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .spinner-text {
      margin-top: 1rem;
      font-family: var(--font-body);
      font-size: 0.85rem;
      color: var(--text-secondary);
      letter-spacing: 0.08em;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() show = true;
  @Input() message = 'Loading...';
}
