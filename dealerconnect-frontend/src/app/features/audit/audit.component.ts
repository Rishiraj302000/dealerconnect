import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { AuditService } from '../../core/services/audit.service';
import { ToastService } from '../../core/ui/toast.service';
import { AuditRecord } from '../../core/models/audit.models';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './audit.component.html',
})
export class AuditComponent implements OnInit {
  private readonly audit = inject(AuditService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  protected readonly loading = signal(true);
  protected readonly records = signal<AuditRecord[]>([]);

  protected readonly actions = [
    'DEALER_CREATED',
    'DEALER_UPDATED',
    'DEALER_ACTIVATED',
    'DEALER_DEACTIVATED',
    'CONTACT_ADDED',
    'CONTACT_REMOVED',
    'FAVORITE_ADDED',
    'FAVORITE_REMOVED',
  ];

  protected readonly filterForm = this.fb.nonNullable.group({
    action: '',
    entityType: '',
    performedBy: '',
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.audit.history(this.filterForm.getRawValue()).subscribe({
      next: (r) => {
        this.records.set(r);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load audit history.');
      },
    });
  }

  protected reset(): void {
    this.filterForm.reset({ action: '', entityType: '', performedBy: '' });
    this.load();
  }

  protected tone(action: string): string {
    if (action.includes('CREATED') || action.includes('ADDED')) return 'badge-green';
    if (action.includes('REMOVED') || action.includes('DEACTIVATED'))
      return 'badge bg-rose-500/12 text-rose-500';
    if (action.includes('UPDATED') || action.includes('ACTIVATED'))
      return 'badge bg-brand-500/12 text-brand-600 dark:text-brand-300';
    return 'badge-gray';
  }

  protected label(action: string): string {
    return action
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/^\w/, (c) => c.toUpperCase());
  }

  protected when(iso: string): string {
    return new Date(iso).toLocaleString();
  }
}
