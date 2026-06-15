import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pointer-events-none fixed right-4 top-4 z-[60] flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2.5">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="pointer-events-auto flex items-start gap-3 rounded-xl border border-line bg-surface p-3.5 shadow-soft animate-slide-in-right">
          <span class="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full"
            [ngClass]="{
              'bg-emerald-500/15 text-emerald-500': toast.type === 'success',
              'bg-rose-500/15 text-rose-500': toast.type === 'error',
              'bg-brand-500/15 text-brand-500': toast.type === 'info'
            }">
            @switch (toast.type) {
              @case ('success') {
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5" /></svg>
              }
              @case ('error') {
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
              }
              @default {
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M12 16v-4M12 8h.01" /><circle cx="12" cy="12" r="9" /></svg>
              }
            }
          </span>
          <p class="flex-1 text-sm font-medium text-ink">{{ toast.text }}</p>
          <button (click)="toasts.dismiss(toast.id)" class="text-muted transition hover:text-ink" aria-label="Dismiss">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastHostComponent {
  protected readonly toasts = inject(ToastService);
}
