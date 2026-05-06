import { Component } from '@angular/core';

@Component({
  selector: 'app-announcement-bar',
  standalone: true,
  template: ``,
  styles: [`
    .announcement-bar {
      background: var(--text-primary);
      color: #fff;
      text-align: center;
      padding: 0.6rem 1rem;
      font-family: var(--font-body);
      font-size: 0.72rem;
      letter-spacing: 0.18em;
      font-weight: 400;
    }
  `]
})
export class AnnouncementBarComponent {}
