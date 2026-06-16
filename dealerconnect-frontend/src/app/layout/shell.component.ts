import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { ThemeToggleComponent } from '../shared/theme-toggle.component';
import { AuthService } from '../core/auth/auth.service';
import { AuditService } from '../core/services/audit.service';
import { FavoriteService } from '../core/services/favorite.service';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  adminOnly?: boolean;
}

interface Notice {
  text: string;
  time: string;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './shell.component.html',
})
export class ShellComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly audit = inject(AuditService);
  private readonly favorites = inject(FavoriteService);
  protected readonly auth = inject(AuthService);

  protected readonly mobileOpen = signal(false);
  protected readonly notifOpen = signal(false);
  protected readonly notices = signal<Notice[]>([]);

  private readonly allNav: NavItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: 'grid' },
    { label: 'Dealers', path: '/dealers', icon: 'store' },
    { label: 'Favorites', path: '/favorites', icon: 'star' },
    { label: 'Audit Log', path: '/audit', icon: 'history', adminOnly: true },
  ];

  protected readonly nav = computed(() =>
    this.allNav.filter((item) => !item.adminOnly || this.auth.isAdmin())
  );

  protected readonly initial = computed(() =>
    (this.auth.user()?.email ?? '?').charAt(0).toUpperCase()
  );

  protected readonly roleLabel = computed(() =>
    this.auth.isAdmin() ? 'Administrator' : 'Relationship Manager'
  );

  ngOnInit(): void {
    this.loadNotices();
  }

  private loadNotices(): void {
    if (this.auth.isAdmin()) {
      this.audit.history().subscribe({
        next: (rows) =>
          this.notices.set(
            rows.slice(0, 6).map((r) => ({ text: r.details, time: this.relative(r.timestamp) }))
          ),
        error: () => {
          /* non-critical */
        },
      });
    } else {
      this.favorites.list().subscribe({
        next: (favs) =>
          this.notices.set(
            favs.slice(0, 6).map((f) => ({
              text: `${f.dealerName ?? 'A dealer'} is in your favorites`,
              time: this.relative(f.createdAt),
            }))
          ),
        error: () => {
          /* non-critical */
        },
      });
    }
  }

  protected toggleNotif(): void {
    const opening = !this.notifOpen();
    this.notifOpen.set(opening);
    if (opening) {
      this.loadNotices();
    }
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
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
