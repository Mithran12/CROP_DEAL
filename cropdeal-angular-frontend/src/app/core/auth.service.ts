import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { AuthResponse, Role, User } from './models';
import { catchError, map, of, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);

  currentUser = signal<User | null>(null);

  login(email: string, password: string) {
    return this.api.post<AuthResponse>('/auth/login', { email, password }).pipe(
      tap(res => {
        if (res.token) {
          this.save(res);
          this.fetchAndSetProfile(res.userId);
        }
      })
    );
  }

  register(email: string, password: string, role: Role, otp?: string) {
    return this.api.post<AuthResponse>('/auth/register', { email, password, role, otp }).pipe(
      tap(res => {
        if (res.token) {
          this.save(res);
        }
      })
    );
  }

  requestRegistrationOtp(email: string) {
    return this.api.post<{ message: string; email?: string }>('/auth/register/request-otp', { email });
  }

  verifyRegistrationOtp(email: string, otp: string) {
    return this.api.post<{ message: string; verified: boolean }>('/auth/register/verify-otp', { email, otp });
  }

  mockFacebook(accountId: string) {
    return this.api.post<AuthResponse>('/auth/facebook/mock', { accountId }).pipe(
      tap(res => {
        if (res.token) {
          this.save(res);
          this.fetchAndSetProfile(res.userId);
        }
      })
    );
  }

  forgotPassword(email: string) {
    return this.api.post<{ message: string; resetToken?: string; resetUrl?: string }>('/auth/forgot-password', { email });
  }

  requestOtp(email: string) {
    return this.api.post<{ message: string }>('/auth/forgot-password/request-otp', { email });
  }

  verifyOtp(email: string, otp: string) {
    return this.api.post<{ message: string }>('/auth/forgot-password/verify-otp', { email, otp });
  }

  resetPasswordWithOtp(email: string, otp: string, newPassword: string, confirmPassword: string) {
    return this.api.post<{ message: string }>('/auth/forgot-password/reset', {
      email,
      otp,
      newPassword,
      confirmPassword
    });
  }

  resetPassword(token: string, newPassword: string) {
    return this.api.post<{ message: string }>('/auth/reset-password', { token, newPassword });
  }

  save(a: AuthResponse) {
    localStorage.setItem('cd_token', a.token || '');
    localStorage.setItem('cd_role', a.role);
    localStorage.setItem('cd_auth_user_id', String(a.userId));
    localStorage.setItem('cd_email', a.email);
  }

  token(): string {
    return localStorage.getItem('cd_token') || '';
  }

  role(): Role | null {
    return localStorage.getItem('cd_role') as Role | null;
  }

  email(): string {
    return localStorage.getItem('cd_email') || '';
  }

  authUserId(): number {
    return Number(localStorage.getItem('cd_auth_user_id') || 0);
  }

  userId(): number {
    return Number(localStorage.getItem('cd_user_id') || 0);
  }

  setUserId(id: number) {
    localStorage.setItem('cd_user_id', String(id));
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  isProfileComplete(): boolean {
    const role = this.role();
    if (role === 'ADMIN') return true;
    const user = this.currentUser();
    if (!user) {
      return localStorage.getItem('cd_profile_complete') === 'true';
    }
    const hasBasic = !!(user.name && user.name.trim().length >= 3 && user.phone && user.address && user.city);
    const hasPayment = !!(user.bankAccountNumber || user.upiId);
    const complete = hasBasic && hasPayment;
    if (complete) {
      localStorage.setItem('cd_profile_complete', 'true');
    } else {
      localStorage.removeItem('cd_profile_complete');
    }
    return complete;
  }

  fetchAndSetProfile(authUserId: number) {
    if (!authUserId) return;
    this.api.get<User>('/users/auth/' + authUserId).pipe(
      catchError(() => of(null))
    ).subscribe(user => {
      if (user && user.id) {
        this.setUserId(user.id);
        this.currentUser.set(user);
        this.isProfileComplete();
      }
    });
  }

  loadProfile() {
    const aid = this.authUserId();
    if (!aid) return of(null);
    return this.api.get<User>('/users/auth/' + aid).pipe(
      tap(u => {
        if (u && u.id) {
          this.setUserId(u.id);
          this.currentUser.set(u);
          this.isProfileComplete();
        }
      }),
      catchError(() => of(null))
    );
  }

  logout() {
    localStorage.clear();
    this.currentUser.set(null);
    this.router.navigateByUrl('/login');
  }
}
