import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { Notification } from './models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);

  /** Contador de não lidas exibido no sino do topbar. */
  readonly unread = signal(0);

  list(): Observable<Notification[]> {
    return this.http.get<Notification[]>('/api/notifications');
  }

  /** Atualiza o contador do sino; falhas são silenciosas (não quebram a navegação). */
  refreshUnread(): void {
    this.http.get<number>('/api/notifications/unread-count').subscribe({
      next: (n) => this.unread.set(n),
      error: () => {}
    });
  }

  markRead(id: number): Observable<Notification> {
    return this.http
      .patch<Notification>(`/api/notifications/${id}/read`, {})
      .pipe(tap(() => this.refreshUnread()));
  }

  markAllRead(): Observable<void> {
    return this.http
      .patch<void>('/api/notifications/read-all', {})
      .pipe(tap(() => this.unread.set(0)));
  }
}
