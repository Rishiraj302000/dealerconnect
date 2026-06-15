import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ThemeToggleComponent } from '../../shared/theme-toggle.component';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { Role } from '../../core/models/auth.models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ThemeToggleComponent],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  protected readonly submitting = signal(false);
  protected readonly showPassword = signal(false);

  protected readonly roles: { value: Role; label: string; hint: string }[] = [
    { value: 'ADMIN', label: 'Administrator', hint: 'Full dealer & audit control' },
    { value: 'RELATIONSHIP_MANAGER', label: 'Relationship Manager', hint: 'Search & manage favorites' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['ADMIN' as Role, [Validators.required]],
  });

  protected invalid(control: 'name' | 'email' | 'password'): boolean {
    const c = this.form.controls[control];
    return c.invalid && (c.touched || c.dirty);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success('Account created. Please sign in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.submitting.set(false);
        this.toast.error(
          err.status === 409 ? 'That email is already registered.' : 'Registration failed. Try again.'
        );
      },
    });
  }
}
