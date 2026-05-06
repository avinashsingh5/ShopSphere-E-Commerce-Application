import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { OrderResponse } from '../../core/models/order.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './admin-orders.component.html',
  styleUrl: './admin-orders.component.css'
})
export class AdminOrdersComponent implements OnInit {
  orders: OrderResponse[] = [];
  loading = true;
  updatingOrderId: number | null = null;
  successMessage = '';

  statusOptions = ['PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.adminService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  updateStatus(orderId: number, status: string): void {
    this.updatingOrderId = orderId;
    this.adminService.updateOrderStatus(orderId, status).subscribe({
      next: () => {
        this.updatingOrderId = null;
        this.loadOrders();
        this.showSuccess(`Order #${orderId} updated to ${status}`);
      },
      error: () => {
        this.updatingOrderId = null;
        alert('Failed to update status');
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PLACED': return 'status-placed';
      case 'CHECKOUT': return 'status-default';
      case 'PACKED': return 'status-packed';
      case 'SHIPPED': return 'status-shipped';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-default';
    }
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 3000);
  }
}
