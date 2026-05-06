import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  isDark = false;

  constructor(public authService: AuthService) {
    // Restore theme preference
    const stored = localStorage.getItem('theme');
    if (stored === 'dark') {
      this.isDark = true;
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    const theme = this.isDark ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }

  logout(): void {
    this.authService.logout();
  }
}
