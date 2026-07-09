import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../../core/auth';
import { apiErrorMessage } from '../../../core/http-error';

/** Perfil de demonstração acessível em um clique. */
type DemoProfile = 'cliente' | 'admin';

/**
 * Contas de demonstração (também documentadas no README).
 * - cliente: dados semeados (cartão fidelidade, agendamentos, notificações).
 * - admin:   painel de gestão (confirmar atendimentos, avisos, cadastros).
 */
const DEMO_CREDENTIALS: Record<DemoProfile, { email: string; senha: string }> = {
  cliente: { email: 'cliente.demo@barbearia.com', senha: 'Cliente@Demo2026' },
  admin: { email: 'admin@barbearia.com', senha: 'Barbearia@Admin2026' }
};

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
  /** Perfil de demonstração cujo login está em andamento (ou null). */
  readonly demoLoading = signal<DemoProfile | null>(null);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]]
  });

  /** Acesso em um clique com a conta de demonstração do perfil escolhido. */
  demoLogin(perfil: DemoProfile): void {
    if (this.demoLoading() !== null || this.loading()) {
      return;
    }
    this.demoLoading.set(perfil);
    this.error.set(null);

    const { email, senha } = DEMO_CREDENTIALS[perfil];
    this.auth.login(email, senha).subscribe({
      next: () => this.router.navigate(['/appointments']),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          apiErrorMessage(
            err,
            'Não foi possível acessar a demonstração. O servidor gratuito pode estar iniciando — aguarde alguns segundos e tente de novo.'
          )
        );
        this.demoLoading.set(null);
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
