import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { OrderResponse } from '../../core/models/order.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent {
  currentStep = 1; // 1=Address, 2=Payment, 3=Review, 4=Confirmation
  loading = false;
  errorMessage = '';
  order: OrderResponse | null = null;

  // Forms
  addressForm: FormGroup;
  paymentForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,
    private router: Router
  ) {
    this.addressForm = this.fb.group({
      shippingAddress: ['', [Validators.required, Validators.minLength(10)]]
    });

    this.paymentForm = this.fb.group({
      paymentMode: ['', [Validators.required]]
    });
  }

  /** Step 1 → 2: Call checkout API */
  submitAddress(): void {
    if (this.addressForm.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.orderService.checkout(this.addressForm.value).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
        this.currentStep = 2;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Checkout failed. Please try again.';
      }
    });
  }

  /** Step 2 → 3: Call payment API */
  submitPayment(): void {
    if (this.paymentForm.invalid || !this.order) return;
    this.loading = true;
    this.errorMessage = '';

    this.orderService.processPayment(this.order.id, this.paymentForm.value).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
        this.currentStep = 3;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Payment failed. Please try again.';
      }
    });
  }

  /** Step 3 → 4: Call place order API */
  placeOrder(): void {
    if (!this.order) return;
    this.loading = true;
    this.errorMessage = '';

    this.orderService.placeOrder(this.order.id).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
        this.currentStep = 4;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to place order. Please try again.';
      }
    });
  }
}
