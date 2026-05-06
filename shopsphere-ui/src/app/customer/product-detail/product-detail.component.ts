import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../core/services/catalog.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { ProductResponse } from '../../core/models/catalog.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  product: ProductResponse | null = null;
  loading = true;
  quantity = 1;
  addingToCart = false;
  addedMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogService: CatalogService,
    private cartService: CartService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.catalogService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  addToCart(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login']);
      return;
    }
    if (!this.product) return;

    this.addingToCart = true;
    this.addedMessage = '';

    this.cartService.addItem({
      productId: this.product.id,
      quantity: this.quantity
    }).subscribe({
      next: () => {
        this.addingToCart = false;
        this.addedMessage = 'Added to cart!';
        alert("Product added to cart");
        setTimeout(() => this.addedMessage = '', 3000);

      },
      error: (err) => {
        this.addingToCart = false;
        this.addedMessage = err.error?.message || 'Failed to add to cart';
      }
    });
  }

  incrementQty(): void {
    if (this.product && this.quantity < this.product.stockQuantity) {
      this.quantity++;
    }
  }

  decrementQty(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }
}
