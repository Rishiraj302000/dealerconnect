import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DealerService } from '../../core/services/dealer.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { Dealer } from '../../core/models/dealer.models';

@Component({
  selector: 'app-dealers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dealers.component.html',
})
export class DealersComponent implements OnInit {
  private readonly dealerService = inject(DealerService);
  private readonly favoriteService = inject(FavoriteService);
  protected readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly loading = signal(true);
  protected readonly dealers = signal<Dealer[]>([]);
  protected readonly busyId = signal<number | null>(null);
  // dealerId -> favoriteId for the current user
  protected readonly favMap = signal<Map<number, number>>(new Map());

  protected readonly searchForm = this.fb.nonNullable.group({ name: '', city: '', status: '' });

  protected readonly showForm = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    code: ['', [Validators.required]],
    email: [''],
    phone: [''],
    city: [''],
  });

  ngOnInit(): void {
    this.load();
    this.loadFavorites();
  }

  protected isFav(id: number): boolean {
    return this.favMap().has(id);
  }

  protected dInvalid(name: 'name' | 'code'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || c.dirty);
  }

  protected goDetail(d: Dealer): void {
    this.router.navigate(['/dealers', d.id]);
  }

  protected load(): void {
    this.loading.set(true);
    this.dealerService.list().subscribe({
      next: (d) => {
        this.dealers.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load dealers.');
      },
    });
  }

  private loadFavorites(): void {
    this.favoriteService.list().subscribe({
      next: (favs) => {
        const map = new Map<number, number>();
        favs.forEach((f) => map.set(f.dealerId, f.id));
        this.favMap.set(map);
      },
      error: () => {
        /* favorites are non-critical here */
      },
    });
  }

  protected runSearch(): void {
    const v = this.searchForm.getRawValue();
    if (!v.name && !v.city && !v.status) {
      this.load();
      return;
    }
    this.loading.set(true);
    this.dealerService.search(v).subscribe({
      next: (d) => {
        this.dealers.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Search failed.');
      },
    });
  }

  protected resetSearch(): void {
    this.searchForm.reset({ name: '', city: '', status: '' });
    this.load();
  }

  protected openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ name: '', code: '', email: '', phone: '', city: '' });
    this.showForm.set(true);
  }

  protected openEdit(d: Dealer): void {
    this.editingId.set(d.id);
    this.form.reset({
      name: d.name,
      code: d.code,
      email: d.email ?? '',
      phone: d.phone ?? '',
      city: d.city ?? '',
    });
    this.showForm.set(true);
  }

  protected closeForm(): void {
    this.showForm.set(false);
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const req = this.form.getRawValue();
    const id = this.editingId();
    const op = id ? this.dealerService.update(id, req) : this.dealerService.create(req);
    op.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.toast.success(id ? 'Dealer updated.' : 'Dealer created.');
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.toast.error(e.status === 409 ? 'Dealer code already exists.' : 'Save failed.');
      },
    });
  }

  protected toggleStatus(d: Dealer): void {
    this.busyId.set(d.id);
    const op =
      d.status === 'ACTIVE' ? this.dealerService.deactivate(d.id) : this.dealerService.activate(d.id);
    op.subscribe({
      next: (updated) => {
        this.busyId.set(null);
        this.dealers.update((list) => list.map((x) => (x.id === d.id ? updated : x)));
        this.toast.success(`Dealer ${updated.status === 'ACTIVE' ? 'activated' : 'deactivated'}.`);
      },
      error: () => {
        this.busyId.set(null);
        this.toast.error('Action failed.');
      },
    });
  }

  protected async remove(d: Dealer): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Delete dealer?',
      message: `"${d.name}" will be removed from the active list (soft delete).`,
      confirmText: 'Delete',
      danger: true,
    });
    if (!ok) return;
    this.busyId.set(d.id);
    this.dealerService.remove(d.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.dealers.update((l) => l.filter((x) => x.id !== d.id));
        this.toast.success('Dealer deleted.');
      },
      error: () => {
        this.busyId.set(null);
        this.toast.error('Delete failed.');
      },
    });
  }

  protected toggleFavorite(d: Dealer): void {
    const favId = this.favMap().get(d.id);
    if (favId) {
      this.favoriteService.remove(favId).subscribe({
        next: () => {
          this.favMap.update((m) => {
            const next = new Map(m);
            next.delete(d.id);
            return next;
          });
          this.toast.success(`${d.name} removed from favorites.`);
        },
        error: () => this.toast.error('Could not update favorite.'),
      });
    } else {
      this.favoriteService.add({ dealerId: d.id }).subscribe({
        next: (f) => {
          this.favMap.update((m) => new Map(m).set(d.id, f.id));
          this.toast.success(`${d.name} added to favorites.`);
        },
        error: (e) =>
          this.toast.error(e.status === 409 ? 'Already in your favorites.' : 'Could not add favorite.'),
      });
    }
  }
}
