import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, SignupRequest } from '../models/auth.model';

interface JwtPayload {
  sub: string;
  role: string;
  exp: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = environment.apiUrl;

  /** Emits the current user's name (or null when logged out) */
  private currentUserSubject = new BehaviorSubject<string | null>(this.getStoredName());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  // ── Auth API calls ──────────────────────────────────────────────

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, request)
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/signup`, request)
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  // ── Token & session helpers ─────────────────────────────────────

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    return localStorage.getItem('userRole');
  }

  getUserName(): string | null {
    return localStorage.getItem('userName');
  }

  getUserEmail(): string | null {
    return localStorage.getItem('userEmail');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  isAdmin(): boolean {
    return this.isLoggedIn() && this.getRole() === 'ADMIN';
  }

  isCustomer(): boolean {
    return this.isLoggedIn() && this.getRole() === 'CUSTOMER';
  }

  // ── Private helpers ─────────────────────────────────────────────

  private handleAuthResponse(res: AuthResponse): void {
    if (res.token) {
      localStorage.setItem('token', res.token);
      localStorage.setItem('userName', res.name);
      localStorage.setItem('userEmail', res.email);
      localStorage.setItem('userRole', res.role);
      this.currentUserSubject.next(res.name);
    }
  }

  private getStoredName(): string | null {
    return localStorage.getItem('userName');
  }
}
