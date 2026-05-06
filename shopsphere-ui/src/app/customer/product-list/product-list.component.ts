import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CatalogService } from '../../core/services/catalog.service';
import { ProductResponse, Category, PageResponse } from '../../core/models/catalog.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: Category[] = [];
  loading = true;

  // Pagination & filters
  keyword = '';
  selectedCategoryId: number | undefined;
  page = 0;
  size = 12;
  sortBy = 'id';
  sortDir = 'desc';
  totalPages = 0;
  totalElements = 0;

  constructor(
    private catalogService: CatalogService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Read query params if any
    this.route.queryParams.subscribe(params => {
      if (params['sortBy']) this.sortBy = params['sortBy'];
      if (params['sortDir']) this.sortDir = params['sortDir'];
    });

    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: () => {}
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.catalogService.getProducts(
      this.keyword || undefined,
      this.selectedCategoryId,
      this.page,
      this.size,
      this.sortBy,
      this.sortDir
    ).subscribe({
      next: (pageData: PageResponse<ProductResponse>) => {
        this.products = pageData.content;
        this.totalPages = pageData.totalPages;
        this.totalElements = pageData.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.page = 0;
    this.loadProducts();
  }

  onCategorySelect(categoryId?: number): void {
    this.selectedCategoryId = categoryId;
    this.page = 0;
    this.loadProducts();
  }

  onSortChange(): void {
    this.page = 0;
    this.loadProducts();
  }

  toggleSortDir(): void {
    this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    this.page = 0;
    this.loadProducts();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages) {
      this.page = p;
      this.loadProducts();
    }
  }

  get pageNumbers(): number[] {
    const pages: number[] = [];
    const start = Math.max(0, this.page - 2);
    const end = Math.min(this.totalPages, start + 5);
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }
}
