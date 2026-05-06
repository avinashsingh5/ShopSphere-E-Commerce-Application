import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  OrderResponse,
  CheckoutRequest,
  PaymentRequest
} from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Step 1: Start checkout — creates order from cart */
  checkout(request: CheckoutRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.apiUrl}/orders/checkout`, request);
  }

  /** Step 2: Process payment for an order */
  processPayment(orderId: number, request: PaymentRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(
      `${this.apiUrl}/orders/${orderId}/payment`, request
    );
  }

  /** Step 3: Place the order — reduces stock and finalizes */
  placeOrder(orderId: number): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(
      `${this.apiUrl}/orders/${orderId}/place`, {}
    );
  }

  /** Get order by ID */
  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.apiUrl}/orders/${id}`);
  }

  /** Get current user's orders */
  getMyOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.apiUrl}/orders/my`);
  }
}
