import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { AppointmentService } from '../../../core/appointment';
import { NotificationService } from '../../../core/notification';
import { apiErrorMessage } from '../../../core/http-error';

/** Horário de funcionamento da barbearia (24h) e passo entre atendimentos. */
const HORA_ABERTURA = 8;
const HORA_FECHAMENTO = 20;
const INTERVALO_MINUTOS = 30;

@Component({
  selector: 'app-appointment-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './appointment-form.html'
})
export class AppointmentForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AppointmentService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  /** Data mínima (hoje) para o seletor — a validação definitiva é do backend. */
  readonly today = new Date().toISOString().slice(0, 10);

  /** Grade fixa de horários, de 30 em 30 minutos, dentro do expediente. */
  readonly slots = buildSlots(HORA_ABERTURA, HORA_FECHAMENTO, INTERVALO_MINUTOS);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    data: ['', [Validators.required]],
    hora: ['', [Validators.required]]
  });

  constructor() {
    // Ao trocar a data, descarta um horário que tenha ficado no passado (hoje).
    this.form.controls.data.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        const hora = this.form.controls.hora.value;
        if (hora && this.isSlotDisabled(hora)) {
          this.form.controls.hora.setValue('');
        }
      });
  }

  get selectedHora(): string {
    return this.form.controls.hora.value;
  }

  selectHora(slot: string): void {
    this.form.controls.hora.setValue(slot);
    this.form.controls.hora.markAsTouched();
  }

  /** Desabilita horários já passados quando a data escolhida é o dia de hoje. */
  isSlotDisabled(slot: string): boolean {
    if (this.form.controls.data.value !== this.today) {
      return false;
    }
    const now = new Date();
    const agora = `${pad(now.getHours())}:${pad(now.getMinutes())}`;
    return slot <= agora;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const { data, hora } = this.form.getRawValue();
    this.service.create(data, hora).subscribe({
      next: () => {
        this.notifications.refreshUnread();
        this.router.navigate(['/appointments']);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível criar o agendamento.'));
        this.loading.set(false);
      }
    });
  }
}

/** Gera "HH:mm" de horaInicio até horaFim (exclusivo), no passo informado. */
function buildSlots(horaInicio: number, horaFim: number, passoMin: number): string[] {
  const slots: string[] = [];
  for (let min = horaInicio * 60; min < horaFim * 60; min += passoMin) {
    slots.push(`${pad(Math.floor(min / 60))}:${pad(min % 60)}`);
  }
  return slots;
}

function pad(n: number): string {
  return n.toString().padStart(2, '0');
}
