import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE } from '../api';
import { Favorite, FavoriteRequest } from '../models/favorite.models';

@Injectable({ providedIn: 'root' })
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE}/favorites`;

  list(category?: string): Observable<Favorite[]> {
    let params = new HttpParams();
    if (category) params = params.set('category', category);
    return this.http.get<Favorite[]>(this.base, { params });
  }

  add(req: FavoriteRequest): Observable<Favorite> {
    return this.http.post<Favorite>(this.base, req);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
