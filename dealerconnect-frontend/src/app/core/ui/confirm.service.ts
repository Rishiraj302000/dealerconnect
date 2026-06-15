import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  danger?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly current = signal<ConfirmOptions | null>(null);
  private resolver: ((value: boolean) => void) | null = null;

  ask(options: ConfirmOptions): Promise<boolean> {
    this.current.set(options);
    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  respond(value: boolean): void {
    this.resolver?.(value);
    this.resolver = null;
    this.current.set(null);
  }
}
