import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResponse } from '../models/admin.model';
import { ProductRequest, ProductResponse, Category, CategoryRequest } from '../models/catalog.model';
import { OrderResponse } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // ── Dashboard & Reports ─────────────────────────────────────────

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.apiUrl}/admin/dashboard`);
  }

  getReports(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.apiUrl}/admin/reports`);
  }

  // ── Product Management ──────────────────────────────────────────

  createProduct(product: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(`${this.apiUrl}/admin/products`, product);
  }

  updateProduct(id: number, product: ProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.apiUrl}/admin/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/products/${id}`);
  }

  // ── Order Management ────────────────────────────────────────────

  getAllOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.apiUrl}/admin/orders`);
  }

  updateOrderStatus(id: number, status: string): Observable<OrderResponse> {
    return this.http.put<OrderResponse>(
      `${this.apiUrl}/admin/orders/${id}/status`, { status }
    );
  }

  // ── Category Management ─────────────────────────────────────────

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/catalog/categories`);
  }

  createCategory(category: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/catalog/categories`, category);
  }

  updateCategory(id: number, category: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/catalog/categories/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/catalog/categories/${id}`);
  }
}
