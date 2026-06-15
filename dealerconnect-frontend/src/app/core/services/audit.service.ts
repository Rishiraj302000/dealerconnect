import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE } from '../api';
import { AuditRecord } from '../models/audit.models';

export interface AuditFilter {
  action?: string;
  entityType?: string;
  performedBy?: string;
}

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE}/audit`;

  history(filter: AuditFilter = {}): Observable<AuditRecord[]> {
    let params = new HttpParams();
    if (filter.action) params = params.set('action', filter.action);
    if (filter.entityType) params = params.set('entityType', filter.entityType);
    if (filter.performedBy) params = params.set('performedBy', filter.performedBy);
    return this.http.get<AuditRecord[]>(this.base, { params });
  }
}
