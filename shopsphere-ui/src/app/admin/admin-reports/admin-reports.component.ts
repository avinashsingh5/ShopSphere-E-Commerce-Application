import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { DashboardResponse } from '../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css'
})
export class AdminReportsComponent implements OnInit {
  report: DashboardResponse | null = null;
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getReports().subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  getOrderFulfillmentRate(): number {
    if (!this.report || this.report.totalOrders === 0) return 0;
    return Math.round((this.report.deliveredOrders / this.report.totalOrders) * 100);
  }

  getCancellationRate(): number {
    if (!this.report || this.report.totalOrders === 0) return 0;
    return Math.round((this.report.cancelledOrders / this.report.totalOrders) * 100);
  }
}
