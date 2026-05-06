import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AnnouncementBarComponent } from './shared/announcement-bar/announcement-bar.component';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { FooterComponent } from './shared/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AnnouncementBarComponent, NavbarComponent, FooterComponent],
  template: `
    <app-announcement-bar />
    <app-navbar />
    <main>
      <router-outlet />
    </main>
    <app-footer />
  `,
  styles: [`
    :host {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }
    main {
      flex: 1;
    }
  `]
})
export class App {}
