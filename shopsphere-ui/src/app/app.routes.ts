import { Routes } from '@angular/router';
import { customerGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: 'customer/home', pathMatch: 'full' },

  // Auth routes (public)
  {
    path: 'auth/login',
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent),
    title: 'Sign In — ShopSphere'
  },
  {
    path: 'auth/signup',
    loadComponent: () => import('./auth/signup/signup.component').then(m => m.SignupComponent),
    title: 'Create Account — ShopSphere'
  },

  // Customer routes (public home/products, guarded cart/checkout/orders)
  {
    path: 'customer/home',
    loadComponent: () => import('./customer/home/home.component').then(m => m.HomeComponent),
    title: 'ShopSphere — Considered Objects, Made to Last'
  },
  {
    path: 'customer/products',
    loadComponent: () => import('./customer/product-list/product-list.component').then(m => m.ProductListComponent),
    title: 'Shop — ShopSphere'
  },
  {
    path: 'customer/product/:id',
    loadComponent: () => import('./customer/product-detail/product-detail.component').then(m => m.ProductDetailComponent),
    title: 'Product — ShopSphere'
  },
  {
    path: 'customer/cart',
    loadComponent: () => import('./customer/cart/cart.component').then(m => m.CartComponent),
    canActivate: [customerGuard],
    title: 'Your Cart — ShopSphere'
  },
  {
    path: 'customer/checkout',
    loadComponent: () => import('./customer/checkout/checkout.component').then(m => m.CheckoutComponent),
    canActivate: [customerGuard],
    title: 'Checkout — ShopSphere'
  },
  {
    path: 'customer/orders',
    loadComponent: () => import('./customer/orders/orders.component').then(m => m.OrdersComponent),
    canActivate: [customerGuard],
    title: 'My Orders — ShopSphere'
  },

  // Admin routes (all guarded)
  {
    path: 'admin/dashboard',
    loadComponent: () => import('./admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    canActivate: [adminGuard],
    title: 'Admin Dashboard — ShopSphere'
  },
  {
    path: 'admin/products',
    loadComponent: () => import('./admin/admin-products/admin-products.component').then(m => m.AdminProductsComponent),
    canActivate: [adminGuard],
    title: 'Admin Products — ShopSphere'
  },
  {
    path: 'admin/categories',
    loadComponent: () => import('./admin/admin-categories/admin-categories.component').then(m => m.AdminCategoriesComponent),
    canActivate: [adminGuard],
    title: 'Admin Categories — ShopSphere'
  },
  {
    path: 'admin/orders',
    loadComponent: () => import('./admin/admin-orders/admin-orders.component').then(m => m.AdminOrdersComponent),
    canActivate: [adminGuard],
    title: 'Admin Orders — ShopSphere'
  },
  {
    path: 'admin/reports',
    loadComponent: () => import('./admin/admin-reports/admin-reports.component').then(m => m.AdminReportsComponent),
    canActivate: [adminGuard],
    title: 'Admin Reports — ShopSphere'
  },

  // Wildcard
  { path: '**', redirectTo: 'customer/home' }
];
