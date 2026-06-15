import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { FavoriteService } from '../../core/services/favorite.service';
import { ToastService } from '../../core/ui/toast.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { Favorite } from '../../core/models/favorite.models';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './favorites.component.html',
})
export class FavoritesComponent implements OnInit {
  private readonly favorites = inject(FavoriteService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);
  private readonly router = inject(Router);

  protected open(dealerId: number): void {
    this.router.navigate(['/dealers', dealerId]);
  }

  protected readonly loading = signal(true);
  protected readonly all = signal<Favorite[]>([]);
  protected readonly category = signal<string>('');

  protected readonly displayed = computed(() => {
    const c = this.category();
    return c ? this.all().filter((f) => f.category === c) : this.all();
  });

  protected readonly categories = computed(
    () => Array.from(new Set(this.all().map((f) => f.category).filter(Boolean))) as string[]
  );

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.favorites.list().subscribe({
      next: (f) => {
        this.all.set(f);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load favorites.');
      },
    });
  }

  protected filter(cat: string): void {
    this.category.set(cat);
  }

  protected async remove(f: Favorite): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Remove favorite?',
      message: `Remove "${f.dealerName ?? 'this dealer'}" from your favorites?`,
      confirmText: 'Remove',
      danger: true,
    });
    if (!ok) return;
    this.favorites.remove(f.id).subscribe({
      next: () => {
        this.all.update((l) => l.filter((x) => x.id !== f.id));
        this.toast.success('Removed from favorites.');
      },
      error: () => this.toast.error('Could not remove favorite.'),
    });
  }
}
