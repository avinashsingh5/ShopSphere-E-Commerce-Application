import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProductResponse, Category, PageResponse } from '../models/catalog.model';

@Injectable({ providedIn: 'root' })
export class CatalogService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Search / list products with pagination */
  getProducts(
    keyword?: string,
    categoryId?: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDir: string = 'asc'
  ): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (categoryId) {
      params = params.set('categoryId', categoryId.toString());
    }

    return this.http.get<PageResponse<ProductResponse>>(
      `${this.apiUrl}/catalog/products`, { params }
    );
  }

  /** Get featured products */
  getFeaturedProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(`${this.apiUrl}/catalog/featured`);
  }

  /** Get a single product by ID */
  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.apiUrl}/catalog/products/${id}`);
  }

  /** Get all categories */
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/catalog/categories`);
  }
}
