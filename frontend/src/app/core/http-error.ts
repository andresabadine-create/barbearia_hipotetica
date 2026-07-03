import { HttpErrorResponse } from '@angular/common/http';

import { ApiError } from './models';

/** Extrai a mensagem amigável do corpo padronizado da API, com fallback. */
export function apiErrorMessage(err: HttpErrorResponse, fallback: string): string {
  const body = err.error as ApiError | undefined;
  return body?.message ?? fallback;
}
