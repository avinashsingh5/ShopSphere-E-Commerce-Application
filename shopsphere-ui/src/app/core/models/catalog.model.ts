export interface ProductResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  featured: boolean;
  categoryId: number;
  categoryName: string;
}

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  featured: boolean;
  categoryId: number;
}

export interface Category {
  id: number;
  name: string;
  description: string;
}

/** Spring Page wrapper */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}


export interface CategoryRequest {
  name: string;
  description: string;
}
