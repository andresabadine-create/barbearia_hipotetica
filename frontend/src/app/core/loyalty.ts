import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { LoyaltyCard } from './models';

@Injectable({ providedIn: 'root' })
export class LoyaltyService {
  private readonly http = inject(HttpClient);

  get(): Observable<LoyaltyCard> {
    return this.http.get<LoyaltyCard>('/api/loyalty');
  }

  redeem(): Observable<LoyaltyCard> {
    return this.http.post<LoyaltyCard>('/api/loyalty/resgatar', {});
  }
}
