import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { DashboardResponse } from '../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  dashboard: DashboardResponse | null = null;
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
