import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmService } from './confirm.service';

@Component({
  selector: 'app-confirm-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (confirm.current(); as opts) {
      <div class="fixed inset-0 z-[70] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm animate-fade-in" (click)="confirm.respond(false)"></div>
        <div class="relative w-full max-w-sm card p-6 animate-scale-in">
          <div class="flex h-11 w-11 items-center justify-center rounded-full"
            [ngClass]="opts.danger ? 'bg-rose-500/12 text-rose-500' : 'bg-brand-500/12 text-brand-500'">
            <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><path d="M12 9v4M12 17h.01" />
            </svg>
          </div>
          <h3 class="mt-4 text-lg font-bold text-ink">{{ opts.title }}</h3>
          <p class="mt-1.5 text-sm text-muted">{{ opts.message }}</p>
          <div class="mt-6 flex justify-end gap-2.5">
            <button class="btn-ghost" (click)="confirm.respond(false)">Cancel</button>
            <button [class]="opts.danger ? 'btn-danger' : 'btn-brand'" (click)="confirm.respond(true)">
              {{ opts.confirmText || 'Confirm' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmHostComponent {
  protected readonly confirm = inject(ConfirmService);
}
