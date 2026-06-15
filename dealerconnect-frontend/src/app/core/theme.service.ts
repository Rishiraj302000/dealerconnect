import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'dc-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.readInitial());

  constructor() {
    this.apply(this.theme());
    // Let the first paint settle, then enable smooth colour transitions.
    setTimeout(() => document.documentElement.classList.add('theme-anim'), 60);
  }

  toggle(): void {
    this.set(this.theme() === 'dark' ? 'light' : 'dark');
  }

  set(theme: Theme): void {
    this.theme.set(theme);
    this.apply(theme);
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      /* ignore storage failures */
    }
  }

  private apply(theme: Theme): void {
    document.documentElement.classList.toggle('dark', theme === 'dark');
  }

  private readInitial(): Theme {
    try {
      const saved = localStorage.getItem(STORAGE_KEY) as Theme | null;
      if (saved === 'light' || saved === 'dark') {
        return saved;
      }
    } catch {
      /* ignore */
    }
    const prefersDark =
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches;
    return prefersDark ? 'dark' : 'light';
  }
}
