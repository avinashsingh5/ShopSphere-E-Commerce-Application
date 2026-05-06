import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/** Allows access only if user is authenticated (any role) */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/auth/login']);
  return false;
};

/** Allows access only if user has ADMIN role */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAdmin()) {
    return true;
  }

  router.navigate(['/auth/login']);
  return false;
};

/** Allows access only if user has CUSTOMER role */
export const customerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isCustomer()) {
    return true;
  }

  router.navigate(['/auth/login']);
  return false;
};
