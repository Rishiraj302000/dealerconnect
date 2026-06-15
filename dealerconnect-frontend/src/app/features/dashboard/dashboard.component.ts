import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DealerService } from '../../core/services/dealer.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/auth/auth.service';

interface StatCard {
  label: string;
  value: number;
  icon: string;
}

interface Segment {
  label: string;
  value: number;
  class: string;
}

interface ActivityRow {
  action: string;
  detail: string;
  time: string;
  tone: 'brand' | 'green' | 'amber' | 'rose';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly dealers = inject(DealerService);
  private readonly favorites = inject(FavoriteService);
  private readonly audit = inject(AuditService);
  protected readonly auth = inject(AuthService);

  protected readonly loading = signal(true);
  protected readonly stats = signal<StatCard[]>([]);
  protected readonly counts = signal<number[]>([0, 0, 0, 0]);
  protected readonly breakdown = signal<Segment[]>([]);
  protected readonly recent = signal<ActivityRow[]>([]);
  protected readonly recentTitle = signal('Recent activity');

  protected readonly greeting = computed(
    () => (this.auth.user()?.email ?? 'there').split('@')[0]
  );

  get breakdownTotal(): number {
    return this.breakdown().reduce((sum, s) => sum + s.value, 0) || 1;
  }

  ngOnInit(): void {
    const isAdmin = this.auth.isAdmin();
    forkJoin({
      dealers: this.dealers.list().pipe(catchError(() => of([]))),
      favorites: this.favorites.list().pipe(catchError(() => of([]))),
      audit: isAdmin ? this.audit.history().pipe(catchError(() => of([]))) : of([]),
    }).subscribe(({ dealers, favorites, audit }) => {
      const active = dealers.filter((d) => d.status === 'ACTIVE').length;
      const inactive = dealers.length - active;

      this.stats.set([
        { label: 'Total Dealers', value: dealers.length, icon: 'store' },
        { label: 'Active Dealers', value: active, icon: 'check' },
        { label: 'Inactive Dealers', value: inactive, icon: 'pause' },
        { label: 'My Favorites', value: favorites.length, icon: 'star' },
      ]);

      this.breakdown.set([
        { label: 'Active', value: active, class: 'bg-emerald-500' },
        { label: 'Inactive', value: inactive, class: 'bg-amber-500' },
      ]);

      if (isAdmin) {
        this.recentTitle.set('Recent activity');
        this.recent.set(
          audit.slice(0, 6).map((a) => ({
            action: this.humanise(a.action),
            detail: a.details,
            time: this.relative(a.timestamp),
            tone: this.tone(a.action),
          }))
        );
      } else {
        this.recentTitle.set('Your favorites');
        this.recent.set(
          favorites.slice(0, 6).map((f) => ({
            action: f.dealerName ?? 'Dealer #' + f.dealerId,
            detail: (f.dealerCode ?? '') + (f.category ? ' · ' + f.category : ''),
            time: this.relative(f.createdAt),
            tone: 'green' as const,
          }))
        );
      }

      this.loading.set(false);
      this.animateCounts();
    });
  }

  private animateCounts(): void {
    const targets = this.stats().map((s) => s.value);
    const duration = 900;
    const start = performance.now();
    const tick = (now: number) => {
      const progress = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - progress, 3);
      this.counts.set(targets.map((t) => Math.round(t * eased)));
      if (progress < 1) {
        requestAnimationFrame(tick);
      }
    };
    requestAnimationFrame(tick);
  }

  private tone(action: string): ActivityRow['tone'] {
    if (action.includes('ACTIVATED') && !action.includes('DEACTIVATED')) return 'green';
    if (action.includes('DEACTIVATED')) return 'amber';
    if (action.includes('ADDED')) return 'green';
    if (action.includes('REMOVED')) return 'rose';
    return 'brand';
  }

  private humanise(action: string): string {
    return action
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/^\w/, (c) => c.toUpperCase());
  }

  private relative(iso: string): string {
    const diff = Date.now() - new Date(iso).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1) return 'just now';
    if (m < 60) return `${m}m ago`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}h ago`;
    return `${Math.floor(h / 24)}d ago`;
  }
}
