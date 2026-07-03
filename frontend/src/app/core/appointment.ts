import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Appointment } from './models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);

  list(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>('/api/appointments');
  }

  create(data: string, hora: string): Observable<Appointment> {
    return this.http.post<Appointment>('/api/appointments', { data, hora });
  }

  cancel(id: number): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/cancel`, {});
  }
}
