import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AdminAppointment, AdminUser } from './models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  listUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>('/api/admin/users');
  }

  sendAviso(mensagem: string, destinatarioIds: number[]): Observable<void> {
    return this.http.post<void>('/api/admin/avisos', { mensagem, destinatarioIds });
  }

  /** Agendamentos vencidos aguardando confirmação de conclusão. */
  listPendingAppointments(): Observable<AdminAppointment[]> {
    return this.http.get<AdminAppointment[]>('/api/admin/appointments');
  }

  concludeAppointment(id: number): Observable<void> {
    return this.http.post<void>(`/api/admin/appointments/${id}/concluir`, {});
  }
}
