import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs';
import { CatalogService } from '../../core/services/catalog.service';
import { ProductResponse } from '../../core/models/catalog.model';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  featuredProducts: ProductResponse[] = [];
  loading = true;

  constructor(
    private catalogService: CatalogService,
    private cdr: ChangeDetectorRef,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadFeaturedProducts();
  }

  loadFeaturedProducts(): void {
    this.loading = true;

    this.catalogService.getFeaturedProducts()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (products) => {
          console.log('Featured products received:', products);
          this.featuredProducts = Array.isArray(products) ? products : [];
        },
        error: (err) => {
          console.error('Featured products API error:', err);
          this.featuredProducts = [];
        }
      });
  }
}
