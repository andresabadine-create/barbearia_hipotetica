export type Role = 'USER' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: number;
  nome: string;
  email: string;
  role: Role;
}

export interface CurrentUser {
  userId: number;
  nome: string;
  email: string;
  role: Role;
}

export interface AdminUser {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  role: Role;
}

export type AppointmentStatus = 'AGENDADO' | 'CANCELADO' | 'CONCLUIDO';

export interface Appointment {
  id: number;
  data: string; // ISO date: yyyy-MM-dd
  hora: string; // ISO time: HH:mm[:ss]
  status: AppointmentStatus;
}

/** Agendamento vencido aguardando confirmação de conclusão pelo admin. */
export interface AdminAppointment {
  id: number;
  userId: number;
  clienteNome: string;
  data: string; // ISO date: yyyy-MM-dd
  hora: string; // ISO time: HH:mm[:ss]
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

export interface LoyaltyCard {
  carimbos: number; // cortes no cartão atual (0..meta-1)
  meta: number; // cortes necessários para um grátis
  recompensasDisponiveis: number; // cortes grátis ainda não resgatados
  cortesConcluidos: number; // total de cortes concluídos
}

export interface Notification {
  id: number;
  mensagem: string;
  lida: boolean;
  createdAt: string; // ISO local date-time: yyyy-MM-ddTHH:mm:ss
}
