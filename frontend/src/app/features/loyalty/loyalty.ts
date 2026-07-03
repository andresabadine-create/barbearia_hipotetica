import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { LoyaltyService } from '../../core/loyalty';
import { LoyaltyCard } from '../../core/models';
import { apiErrorMessage } from '../../core/http-error';

@Component({
  selector: 'app-loyalty',
  imports: [RouterLink],
  templateUrl: './loyalty.html'
})
export class Loyalty {
  private readonly service = inject(LoyaltyService);

  readonly card = signal<LoyaltyCard | null>(null);
  readonly loading = signal(true);
  readonly redeeming = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  /** Um booleano por selo do cartão: true = carimbado. */
  readonly stamps = computed(() => {
    const c = this.card();
    return c ? Array.from({ length: c.meta }, (_, i) => i < c.carimbos) : [];
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.get().subscribe({
      next: (card) => {
        this.card.set(card);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível carregar seu cartão fidelidade.'));
        this.loading.set(false);
      }
    });
  }

  redeem(): void {
    this.redeeming.set(true);
    this.error.set(null);
    this.message.set(null);
    this.service.redeem().subscribe({
      next: (card) => {
        this.card.set(card);
        this.message.set('Corte grátis resgatado! Mostre esta tela na barbearia. 💈');
        this.redeeming.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(apiErrorMessage(err, 'Não foi possível resgatar agora.'));
        this.redeeming.set(false);
      }
    });
  }
}
