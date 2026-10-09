import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { map } from 'rxjs';

export const authGuard: CanActivateFn = () =>
  localStorage.getItem('cd_token') ? true : inject(Router).parseUrl('/login');

export const profileGuard: CanActivateFn = () => {
  const router = inject(Router);
  const auth = inject(AuthService);
  const token = localStorage.getItem('cd_token');
  if (!token) return router.parseUrl('/login');

  const role = auth.role();
  if (role === 'ADMIN') return true;

  if (auth.currentUser()) {
    return auth.isProfileComplete() ? true : router.parseUrl('/profile');
  }

  return auth.loadProfile().pipe(
    map(user => {
      if (!user || !auth.isProfileComplete()) {
        return router.parseUrl('/profile');
      }
      return true;
    })
  );
};

export const adminGuard: CanActivateFn = () => {
  const token = localStorage.getItem('cd_token');
  const role = localStorage.getItem('cd_role');
  const router = inject(Router);
  if (!token) return router.parseUrl('/login');
  if (role !== 'ADMIN') return router.parseUrl('/dashboard');
  return true;
};

