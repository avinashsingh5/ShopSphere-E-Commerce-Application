import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { CatalogService } from '../../core/services/catalog.service';
import { ProductResponse, Category } from '../../core/models/catalog.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.css'
})
export class AdminProductsComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: Category[] = [];
  loading = true;
  showModal = false;
  editingProduct: ProductResponse | null = null;
  productForm: FormGroup;
  saving = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private adminService: AdminService,
    private catalogService: CatalogService,
    private fb: FormBuilder
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      price: [0, [Validators.required, Validators.min(1)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      imageUrl: [''],
      featured: [false],
      categoryId: [null, [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  loadProducts(): void {
    this.loading = true;
    this.catalogService.getProducts(undefined, undefined, 0, 100, 'id', 'desc').subscribe({
      next: (page) => {
        this.products = page.content;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: () => {}
    });
  }

  openAddModal(): void {
    this.editingProduct = null;
    this.productForm.reset({ featured: false, price: 0, stockQuantity: 0 });
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(product: ProductResponse): void {
    this.editingProduct = product;
    this.productForm.patchValue({
      name: product.name,
      description: product.description,
      price: product.price,
      stockQuantity: product.stockQuantity,
      imageUrl: product.imageUrl,
      featured: product.featured,
      categoryId: product.categoryId
    });
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingProduct = null;
  }

  saveProduct(): void {
    if (this.productForm.invalid) return;
    this.saving = true;
    this.errorMessage = '';

    const data = this.productForm.value;

    if (this.editingProduct) {
      this.adminService.updateProduct(this.editingProduct.id, data).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadProducts();
          this.showSuccess('Product updated successfully');
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to update product';
        }
      });
    } else {
      this.adminService.createProduct(data).subscribe({
        next: () => {
          this.saving = false;
          this.closeModal();
          this.loadProducts();
          this.showSuccess('Product created successfully');
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to create product';
        }
      });
    }
  }

  deleteProduct(id: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;
    this.adminService.deleteProduct(id).subscribe({
      next: () => {
        this.loadProducts();
        this.showSuccess('Product deleted');
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to delete product');
      }
    });
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 3000);
  }
}
