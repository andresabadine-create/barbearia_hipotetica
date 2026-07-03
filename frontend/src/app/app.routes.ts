import { Routes } from '@angular/router';

import { authGuard } from './core/auth-guard';
import { adminGuard } from './core/admin-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'appointments' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register)
  },
  {
    path: 'appointments',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/appointments/appointment-list/appointment-list').then(
        (m) => m.AppointmentList
      )
  },
  {
    path: 'appointments/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/appointments/appointment-form/appointment-form').then(
        (m) => m.AppointmentForm
      )
  },
  {
    path: 'loyalty',
    canActivate: [authGuard],
    loadComponent: () => import('./features/loyalty/loyalty').then((m) => m.Loyalty)
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/notifications').then((m) => m.Notifications)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/admin').then((m) => m.Admin)
  },
  { path: '**', redirectTo: 'appointments' }
];
