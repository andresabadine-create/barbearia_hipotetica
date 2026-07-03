import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { AdminService } from '../../core/admin';
import { AdminAppointment, AdminUser } from '../../core/models';
import { apiErrorMessage } from '../../core/http-error';

@Component({
  selector: 'app-admin',
  imports: [RouterLink],
  templateUrl: './admin.html'
})
export class Admin {
  private readonly service = inject(AdminService);

  readonly users = signal<AdminUser[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly pending = signal<AdminAppointment[]>([]);
  readonly concludingId = signal<number | null>(null);
  readonly pendingError = signal<string | null>(null);

  readonly mensagem = signal('');
  readonly selected = signal<Set<number>>(new Set());
  readonly sending = signal(false);
  readonly sendError = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  readonly selectedCount = computed(() => this.selected().size);
  readonly allSelected = computed(
    () => this.users().length > 0 && this.selected().size === this.users().length
  );
  readonly canSend = computed(
    () => this.mensagem().trim().length > 0 && this.selected().size > 0
  );

  constructor() {
    this.load();
    this.loadPending();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível carregar os cadastros.'));
        this.loading.set(false);
      }
    });
  }

  loadPending(): void {
    this.pendingError.set(null);
    this.service.listPendingAppointments().subscribe({
      next: (items) => this.pending.set(items),
      error: (err: HttpErrorResponse) =>
        this.pendingError.set(
          apiErrorMessage(err, 'Não foi possível carregar os atendimentos pendentes.')
        )
    });
  }

  conclude(appointment: AdminAppointment): void {
    this.concludingId.set(appointment.id);
    this.pendingError.set(null);
    this.service.concludeAppointment(appointment.id).subscribe({
      next: () => {
        this.pending.update((list) => list.filter((a) => a.id !== appointment.id));
        this.concludingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.pendingError.set(apiErrorMessage(err, 'Não foi possível concluir o atendimento.'));
        this.concludingId.set(null);
      }
    });
  }

  /** "2026-07-01" -> "01/07/2026" */
  formatData(iso: string): string {
    const [ano, mes, dia] = iso.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  formatHora(iso: string): string {
    return iso.slice(0, 5);
  }

  isSelected(id: number): boolean {
    return this.selected().has(id);
  }

  toggle(id: number): void {
    this.selected.update((set) => {
      const next = new Set(set);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
    this.success.set(null);
  }

  toggleAll(): void {
    this.selected.update((set) =>
      set.size === this.users().length ? new Set() : new Set(this.users().map((u) => u.id))
    );
    this.success.set(null);
  }

  updateMensagem(value: string): void {
    this.mensagem.set(value);
    this.success.set(null);
  }

  send(): void {
    const texto = this.mensagem().trim();
    const ids = [...this.selected()];
    if (!texto || ids.length === 0) {
      return;
    }
    this.sending.set(true);
    this.sendError.set(null);
    this.success.set(null);
    this.service.sendAviso(texto, ids).subscribe({
      next: () => {
        this.success.set(`Aviso enviado para ${ids.length} destinatário(s).`);
        this.mensagem.set('');
        this.selected.set(new Set());
        this.sending.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.sendError.set(apiErrorMessage(err, 'Não foi possível enviar o aviso.'));
        this.sending.set(false);
      }
    });
  }
}
