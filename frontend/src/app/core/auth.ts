import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { AuthResponse, CurrentUser } from './models';

const STORAGE_KEY = 'barbearia.auth';

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);

  private readonly session = signal<AuthResponse | null>(readSession());

  readonly user = computed<CurrentUser | null>(() => {
    const s = this.session();
    return s ? { userId: s.userId, nome: s.nome, email: s.email, role: s.role } : null;
  });
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');

  token(): string | null {
    return this.session()?.token ?? null;
  }

  register(
    nome: string,
    email: string,
    senha: string,
    telefone: string
  ): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/api/auth/register', { nome, email, senha, telefone })
      .pipe(tap((res) => this.persist(res)));
  }

  login(email: string, senha: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/api/auth/login', { email, senha })
      .pipe(tap((res) => this.persist(res)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }

  private persist(res: AuthResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(res));
    this.session.set(res);
  }
}

function readSession(): AuthResponse | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthResponse;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}
