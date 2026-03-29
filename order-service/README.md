# Order Service

## Overview
The Order Service manages the **shopping cart, checkout process, and order lifecycle** for the ShopSphere platform. It provides:

- Cart management (add, update, remove items)
- Multi-step checkout flow (Address → Payment → Place Order)
- Order history for customers
- Order status management for admins

## Order Status Lifecycle
```
DRAFT → CHECKOUT → PAID → PACKED → SHIPPED → DELIVERED
                                              ↘ CANCELLED
                                   ↘ FAILED
```

## Database
- **Database**: `shopsphere_orders` (MySQL)
- **Tables**: `carts`, `cart_items`, `orders`, `order_items`
- **DDL**: Auto-generated via `ddl-auto: update`

## Entities
| Entity | Fields |
|--------|--------|
| Cart | id, userId, items |
| CartItem | id, productId, productName, quantity, price, cart |
| Order | id, userId, status, totalAmount, shippingAddress, paymentMode, timestamps, items |
| OrderItem | id, productId, productName, quantity, price, order |

## API Endpoints

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders/cart` | Get user's cart |
| POST | `/orders/cart/items` | Add item to cart |
| PUT | `/orders/cart/items/{id}` | Update cart item quantity |
| DELETE | `/orders/cart/items/{id}` | Remove item from cart |

### Orders (Customer)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders/checkout/start` | Start checkout (creates order from cart) |
| POST | `/orders/payment` | Process payment |
| POST | `/orders/place` | Place the order |
| GET | `/orders/{id}` | Get order by ID |
| GET | `/orders/my` | Get order history |

### Orders (Admin)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders/all` | Get all orders |
| PUT | `/orders/{id}/status` | Update order status |

## User Identification
The service reads the `X-User-Name` header (forwarded by the API Gateway after JWT validation) to identify the current user. No direct JWT handling is needed.

## Configuration
- **Port**: `8083`
- **Eureka**: Registers as `ORDER-SERVICE`

## Running
```bash
cd order-service
mvn spring-boot:run
```

> **Note**: Ensure MySQL is running and the Discovery Service is up.
