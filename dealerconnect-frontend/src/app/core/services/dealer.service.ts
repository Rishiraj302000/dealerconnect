import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE } from '../api';
import { Contact, ContactRequest, Dealer, DealerRequest } from '../models/dealer.models';

export interface DealerSearch {
  name?: string;
  city?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class DealerService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE}/dealers`;
  private readonly contactsBase = `${API_BASE}/contacts`;

  list(): Observable<Dealer[]> {
    return this.http.get<Dealer[]>(this.base);
  }

  getById(id: number): Observable<Dealer> {
    return this.http.get<Dealer>(`${this.base}/${id}`);
  }

  search(criteria: DealerSearch): Observable<Dealer[]> {
    let params = new HttpParams();
    if (criteria.name) params = params.set('name', criteria.name);
    if (criteria.city) params = params.set('city', criteria.city);
    if (criteria.status) params = params.set('status', criteria.status);
    return this.http.get<Dealer[]>(`${this.base}/search`, { params });
  }

  create(req: DealerRequest): Observable<Dealer> {
    return this.http.post<Dealer>(this.base, req);
  }

  update(id: number, req: DealerRequest): Observable<Dealer> {
    return this.http.put<Dealer>(`${this.base}/${id}`, req);
  }

  activate(id: number): Observable<Dealer> {
    return this.http.patch<Dealer>(`${this.base}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<Dealer> {
    return this.http.patch<Dealer>(`${this.base}/${id}/deactivate`, {});
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  contacts(dealerId: number): Observable<Contact[]> {
    const params = new HttpParams().set('dealerId', dealerId);
    return this.http.get<Contact[]>(this.contactsBase, { params });
  }

  createContact(req: ContactRequest): Observable<Contact> {
    return this.http.post<Contact>(this.contactsBase, req);
  }

  deleteContact(id: number): Observable<void> {
    return this.http.delete<void>(`${this.contactsBase}/${id}`);
  }
}
