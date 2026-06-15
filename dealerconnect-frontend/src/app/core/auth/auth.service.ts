import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_BASE } from '../api';
import {
  AuthResponse,
  CurrentUser,
  LoginRequest,
  RegisterRequest,
  RegisterResponse,
  Role,
} from '../models/auth.models';

const TOKEN_KEY = 'dc-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly token = signal<string | null>(this.readToken());
  readonly user = computed<CurrentUser | null>(() => this.parse(this.token()));
  readonly isAuthenticated = computed(() => this.user() !== null);
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_BASE}/auth/login`, req)
      .pipe(tap((res) => this.setToken(res.token)));
  }

  register(req: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${API_BASE}/auth/register`, req);
  }

  logout(): void {
    this.clear();
  }

  hasRole(role: Role): boolean {
    return this.user()?.role === role;
  }

  setToken(token: string): void {
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch {
      /* ignore */
    }
    this.token.set(token);
  }

  clear(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {
      /* ignore */
    }
    this.token.set(null);
  }

  private readToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  private parse(token: string | null): CurrentUser | null {
    if (!token) {
      return null;
    }
    try {
      const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(part));
      if (payload.exp && payload.exp * 1000 < Date.now()) {
        return null;
      }
      return { email: payload.sub, role: payload.role };
    } catch {
      return null;
    }
  }
}
