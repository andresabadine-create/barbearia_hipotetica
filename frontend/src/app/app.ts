import { Component, effect, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { Auth } from './core/auth';
import { NotificationService } from './core/notification';

const DEMO_BANNER_KEY = 'demo-banner-dismissed';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly user = this.auth.user;
  readonly isLoggedIn = this.auth.isLoggedIn;
  readonly isAdmin = this.auth.isAdmin;
  readonly unread = this.notifications.unread;

  // Aviso de ambiente de demonstração: os dados são reiniciados a cada 6h.
  // Dispensável e lembrado por sessão via localStorage, para não incomodar.
  readonly bannerDismissed = signal(localStorage.getItem(DEMO_BANNER_KEY) === '1');

  constructor() {
    // Atualiza o contador do sino sempre que o usuário estiver autenticado
    // (carga inicial e após login).
    effect(() => {
      if (this.isLoggedIn()) {
        this.notifications.refreshUnread();
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  dismissBanner(): void {
    localStorage.setItem(DEMO_BANNER_KEY, '1');
    this.bannerDismissed.set(true);
  }
}
