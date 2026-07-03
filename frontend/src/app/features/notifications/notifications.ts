import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { NotificationService } from '../../core/notification';
import { Notification } from '../../core/models';
import { apiErrorMessage } from '../../core/http-error';

@Component({
  selector: 'app-notifications',
  imports: [RouterLink],
  templateUrl: './notifications.html'
})
export class Notifications {
  private readonly service = inject(NotificationService);

  readonly items = signal<Notification[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly hasUnread = computed(() => this.items().some((n) => !n.lida));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
        this.service.refreshUnread();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível carregar suas notificações.'));
        this.loading.set(false);
      }
    });
  }

  markRead(n: Notification): void {
    if (n.lida) {
      return;
    }
    this.service.markRead(n.id).subscribe({
      next: (updated) =>
        this.items.update((list) => list.map((x) => (x.id === updated.id ? updated : x))),
      error: (err: HttpErrorResponse) =>
        this.error.set(apiErrorMessage(err, 'Não foi possível atualizar a notificação.'))
    });
  }

  markAllRead(): void {
    this.service.markAllRead().subscribe({
      next: () => this.items.update((list) => list.map((n) => ({ ...n, lida: true }))),
      error: (err: HttpErrorResponse) =>
        this.error.set(apiErrorMessage(err, 'Não foi possível marcar todas como lidas.'))
    });
  }

  /** "2026-07-01T15:30:00" -> "01/07/2026 às 15:30" */
  formatDateTime(iso: string): string {
    const [date, time] = iso.split('T');
    const [ano, mes, dia] = date.split('-');
    return `${dia}/${mes}/${ano} às ${(time ?? '').slice(0, 5)}`;
  }
}
