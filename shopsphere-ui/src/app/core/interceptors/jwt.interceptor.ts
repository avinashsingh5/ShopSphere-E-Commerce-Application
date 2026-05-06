import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Functional HTTP interceptor (Angular 18 style).
 * Attaches JWT token from localStorage to every outgoing request.
 * On 401 responses, clears the token and redirects to login.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  // Clone request and add Authorization header if token exists
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userName');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userEmail');
        router.navigate(['/auth/login']);
      }
      return throwError(() => error);
    })
  );
};
