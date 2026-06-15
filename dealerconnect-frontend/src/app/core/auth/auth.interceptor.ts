import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { API_BASE } from '../api';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();

  let request = req;
  if (token && req.url.startsWith(API_BASE)) {
    request = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(request).pipe(
    catchError((err) => {
      if (err.status === 401) {
        auth.clear();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
