export interface CartResponse {
  id: number;
  userId: string;
  items: CartItemResponse[];
  totalAmount: number;
}

export interface CartItemResponse {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}

export interface OrderResponse {
  id: number;
  userId: string;
  status: string;
  totalAmount: number;
  shippingAddress: string;
  paymentMode: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
}

export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface CheckoutRequest {
  shippingAddress: string;
}

export interface PaymentRequest {
  paymentMode: string;
}

export interface OrderStatusUpdateRequest {
  status: string;
}
