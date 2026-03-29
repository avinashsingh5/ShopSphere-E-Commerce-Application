# Admin Service

## Overview
The Admin Service provides **administrative operations** for the ShopSphere platform. It acts as a **coordinator** that delegates to other microservices rather than owning domain data directly.

### Design Philosophy
- **No database**: Admin-service does NOT have its own database
- **Delegation pattern**: Product management delegates to Catalog Service, order management to Order Service
- **Single source of truth**: Product data is owned by Catalog Service, order data by Order Service
- **Role enforcement**: ALL endpoints require `ADMIN` role (enforced at Spring Security level)

## API Endpoints

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/dashboard` | Aggregated system metrics |

### Product Management (delegates to Catalog Service)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/products` | Create a product (→ catalog-service) |
| PUT | `/admin/products/{id}` | Update a product (→ catalog-service) |
| DELETE | `/admin/products/{id}` | Delete a product (→ catalog-service) |

### Order Management (delegates to Order Service)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/orders` | Get all orders (→ order-service) |
| PUT | `/admin/orders/{id}/status` | Update order status (→ order-service) |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/reports` | System analytics and reports |

## Inter-Service Communication
Uses `WebClient` with `@LoadBalanced` for service discovery via Eureka:
- `http://CATALOG-SERVICE/catalog/products` → Product CRUD
- `http://ORDER-SERVICE/orders/all` → Order listing
- Headers `X-User-Name` and `X-User-Role` are forwarded from Gateway

## Security
- JWT is validated at the API Gateway layer
- `RoleHeaderFilter` reads `X-User-Role` from Gateway-forwarded headers
- `SecurityConfig` requires `ROLE_ADMIN` for all endpoints
- Additional `@PreAuthorize("hasRole('ADMIN')")` on each controller class

## Configuration
- **Port**: `8084`
- **Eureka**: Registers as `ADMIN-SERVICE`

## Running
```bash
cd admin-service
mvn spring-boot:run
```

> **Note**: Ensure Discovery Service, Catalog Service, and Order Service are running.
