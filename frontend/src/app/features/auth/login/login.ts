import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../../core/auth';
import { apiErrorMessage } from '../../../core/http-error';

/** Conta de demonstração (admin) — também documentada no README. */
const DEMO_EMAIL = 'admin@barbearia.com';
const DEMO_SENHA = 'Barbearia@Admin2026';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html'
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly demoLoading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]]
  });

  /** Acesso em um clique com a conta de demonstração (papel ADMIN: enxerga tudo). */
  demoLogin(): void {
    if (this.demoLoading() || this.loading()) {
      return;
    }
    this.demoLoading.set(true);
    this.error.set(null);

    this.auth.login(DEMO_EMAIL, DEMO_SENHA).subscribe({
      next: () => this.router.navigate(['/appointments']),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          apiErrorMessage(
            err,
            'Não foi possível acessar a demonstração. O servidor gratuito pode estar iniciando — aguarde alguns segundos e tente de novo.'
          )
        );
        this.demoLoading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const { email, senha } = this.form.getRawValue();
    this.auth.login(email, senha).subscribe({
      next: () => this.router.navigate(['/appointments']),
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível entrar. Tente novamente.'));
        this.loading.set(false);
      }
    });
  }
}
