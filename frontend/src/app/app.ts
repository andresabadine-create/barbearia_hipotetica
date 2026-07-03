import { Component, effect, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { Auth } from './core/auth';
import { NotificationService } from './core/notification';

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
}
