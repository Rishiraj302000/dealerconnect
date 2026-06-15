import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DealerService } from '../../core/services/dealer.service';
import { FavoriteService } from '../../core/services/favorite.service';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { Contact, Dealer } from '../../core/models/dealer.models';

@Component({
  selector: 'app-dealer-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './dealer-detail.component.html',
})
export class DealerDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dealerService = inject(DealerService);
  private readonly favoriteService = inject(FavoriteService);
  protected readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);
  private readonly fb = inject(FormBuilder);

  private dealerId = 0;

  protected readonly loading = signal(true);
  protected readonly dealer = signal<Dealer | null>(null);
  protected readonly contacts = signal<Contact[]>([]);
  protected readonly favoriteId = signal<number | null>(null);
  protected readonly busy = signal(false);

  protected readonly selected = signal<Contact | null>(null);
  protected readonly savingContact = signal(false);
  protected readonly contactForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: [''],
    phone: [''],
    designation: [''],
  });

  ngOnInit(): void {
    this.dealerId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  protected cInvalid(): boolean {
    const c = this.contactForm.controls.name;
    return c.invalid && (c.touched || c.dirty);
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      dealer: this.dealerService.getById(this.dealerId).pipe(catchError(() => of(null))),
      contacts: this.dealerService.contacts(this.dealerId).pipe(catchError(() => of([] as Contact[]))),
      favorites: this.favoriteService.list().pipe(catchError(() => of([]))),
    }).subscribe(({ dealer, contacts, favorites }) => {
      if (!dealer) {
        this.toast.error('Dealer not found.');
        this.router.navigate(['/dealers']);
        return;
      }
      this.dealer.set(dealer);
      this.contacts.set(contacts);
      const fav = favorites.find((f) => f.dealerId === this.dealerId);
      this.favoriteId.set(fav ? fav.id : null);
      this.loading.set(false);
    });
  }

  protected toggleFavorite(): void {
    const fid = this.favoriteId();
    if (fid) {
      this.favoriteService.remove(fid).subscribe({
        next: () => {
          this.favoriteId.set(null);
          this.toast.success('Removed from favorites.');
        },
        error: () => this.toast.error('Could not update favorite.'),
      });
    } else {
      this.favoriteService.add({ dealerId: this.dealerId }).subscribe({
        next: (f) => {
          this.favoriteId.set(f.id);
          this.toast.success('Added to favorites.');
        },
        error: (e) =>
          this.toast.error(e.status === 409 ? 'Already in favorites.' : 'Could not add favorite.'),
      });
    }
  }

  protected toggleStatus(): void {
    const d = this.dealer();
    if (!d) return;
    this.busy.set(true);
    const op =
      d.status === 'ACTIVE'
        ? this.dealerService.deactivate(d.id)
        : this.dealerService.activate(d.id);
    op.subscribe({
      next: (updated) => {
        this.busy.set(false);
        this.dealer.set(updated);
        this.toast.success(`Dealer ${updated.status === 'ACTIVE' ? 'activated' : 'deactivated'}.`);
      },
      error: () => {
        this.busy.set(false);
        this.toast.error('Action failed.');
      },
    });
  }

  protected async remove(): Promise<void> {
    const d = this.dealer();
    if (!d) return;
    const ok = await this.confirm.ask({
      title: 'Delete dealer?',
      message: `"${d.name}" will be removed (soft delete).`,
      confirmText: 'Delete',
      danger: true,
    });
    if (!ok) return;
    this.dealerService.remove(d.id).subscribe({
      next: () => {
        this.toast.success('Dealer deleted.');
        this.router.navigate(['/dealers']);
      },
      error: () => this.toast.error('Delete failed.'),
    });
  }

  protected addContact(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }
    this.savingContact.set(true);
    this.dealerService
      .createContact({ dealerId: this.dealerId, ...this.contactForm.getRawValue() })
      .subscribe({
        next: (c) => {
          this.savingContact.set(false);
          this.contacts.update((l) => [...l, c]);
          this.contactForm.reset({ name: '', email: '', phone: '', designation: '' });
          this.toast.success('Contact added.');
        },
        error: () => {
          this.savingContact.set(false);
          this.toast.error('Could not add contact.');
        },
      });
  }

  protected async removeContact(c: Contact): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Delete contact?',
      message: `Remove "${c.name}"?`,
      confirmText: 'Delete',
      danger: true,
    });
    if (!ok) return;
    this.dealerService.deleteContact(c.id).subscribe({
      next: () => {
        this.contacts.update((l) => l.filter((x) => x.id !== c.id));
        if (this.selected()?.id === c.id) this.selected.set(null);
        this.toast.success('Contact removed.');
      },
      error: () => this.toast.error('Delete failed.'),
    });
  }
}
