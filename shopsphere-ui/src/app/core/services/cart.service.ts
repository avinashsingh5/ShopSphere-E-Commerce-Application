import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CartResponse, CartItemRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class CartService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Get current user's cart */
  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(`${this.apiUrl}/orders/cart`);
  }

  /** Add item to cart */
  addItem(request: CartItemRequest): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.apiUrl}/orders/cart/items`, request);
  }

  /** Update cart item quantity */
  updateItem(itemId: number, quantity: number): Observable<CartResponse> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<CartResponse>(
      `${this.apiUrl}/orders/cart/items/${itemId}`, null, { params }
    );
  }

  /** Remove item from cart */
  removeItem(itemId: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.apiUrl}/orders/cart/items/${itemId}`);
  }
}
