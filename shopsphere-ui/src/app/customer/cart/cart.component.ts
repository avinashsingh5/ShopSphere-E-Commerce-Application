import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { CartResponse } from '../../core/models/order.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
  cart: CartResponse | null = null;
  loading = true;
  updatingItemId: number | null = null;

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  updateQuantity(itemId: number, newQuantity: number): void {
    if (newQuantity < 1) return;
    this.updatingItemId = itemId;
    this.cartService.updateItem(itemId, newQuantity).subscribe({
      next: (cart) => {
        this.cart = cart;
        this.updatingItemId = null;
      },
      error: () => {
        this.updatingItemId = null;
      }
    });
  }

  removeItem(itemId: number): void {
    this.updatingItemId = itemId;
    this.cartService.removeItem(itemId).subscribe({
      next: (cart) => {
        this.cart = cart;
        this.updatingItemId = null;
      },
      error: () => {
        this.updatingItemId = null;
      }
    });
  }
}
