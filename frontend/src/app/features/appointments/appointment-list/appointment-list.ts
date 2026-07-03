import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { AppointmentService } from '../../../core/appointment';
import { NotificationService } from '../../../core/notification';
import { Appointment, AppointmentStatus } from '../../../core/models';
import { apiErrorMessage } from '../../../core/http-error';

@Component({
  selector: 'app-appointment-list',
  imports: [RouterLink],
  templateUrl: './appointment-list.html'
})
export class AppointmentList {
  private readonly service = inject(AppointmentService);
  private readonly notifications = inject(NotificationService);

  readonly appointments = signal<Appointment[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly cancelingId = signal<number | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list().subscribe({
      next: (list) => {
        this.appointments.set(list);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível carregar seus agendamentos.'));
        this.loading.set(false);
      }
    });
  }

  cancel(appointment: Appointment): void {
    const quando = `${this.formatData(appointment.data)} às ${this.formatHora(appointment.hora)}`;
    if (!confirm(`Deseja cancelar o agendamento de ${quando}?`)) {
      return;
    }
    this.cancelingId.set(appointment.id);
    this.error.set(null);
    this.service.cancel(appointment.id).subscribe({
      next: (updated) => {
        this.appointments.update((list) =>
          list.map((a) => (a.id === updated.id ? updated : a))
        );
        this.notifications.refreshUnread();
        this.cancelingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível cancelar o agendamento.'));
        this.cancelingId.set(null);
      }
    });
  }

  formatData(iso: string): string {
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  formatHora(iso: string): string {
    return iso.slice(0, 5);
  }

  statusLabel(status: AppointmentStatus): string {
    switch (status) {
      case 'AGENDADO':
        return 'Agendado';
      case 'CANCELADO':
        return 'Cancelado';
      case 'CONCLUIDO':
        return 'Concluído';
    }
  }
}
