import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly raiz = environment.apiUrl;

  constructor(private http: HttpClient) {}

  obtener<T>(ruta: string): Observable<T> {
    return this.http.get<T>(`${this.raiz}${ruta}`).pipe(catchError(error => this.procesarError(error)));
  }

  obtenerArchivo(ruta: string): Observable<Blob> {
    return this.http.get(`${this.raiz}${ruta}`, { responseType: 'blob' })
      .pipe(catchError(error => this.procesarError(error)));
  }

  crear<T>(ruta: string, cuerpo: unknown): Observable<T> {
    return this.http.post<T>(`${this.raiz}${ruta}`, cuerpo).pipe(catchError(error => this.procesarError(error)));
  }

  actualizar<T>(ruta: string, cuerpo: unknown): Observable<T> {
    return this.http.put<T>(`${this.raiz}${ruta}`, cuerpo).pipe(catchError(error => this.procesarError(error)));
  }

  eliminar(ruta: string): Observable<void> {
    return this.http.delete<void>(`${this.raiz}${ruta}`).pipe(catchError(error => this.procesarError(error)));
  }

  private procesarError(error: HttpErrorResponse): Observable<never> {
    const erroresCampos = Array.isArray(error.error?.erroresCampos)
      ? error.error.erroresCampos.map((detalle: { mensaje: string }) => detalle.mensaje).join('. ')
      : '';
    const mensaje = erroresCampos || error.error?.mensaje || this.mensajePorEstado(error.status);
    return throwError(() => new Error(mensaje));
  }

  private mensajePorEstado(estado: number): string {
    if (estado === 0) return 'No se pudo conectar con el servidor.';
    if (estado === 401) return 'La sesión no es válida. Inicia sesión nuevamente.';
    if (estado === 403) return 'No tienes permiso para realizar esta operación.';
    if (estado === 404) return 'No se encontró la información solicitada.';
    return 'No se pudo completar la operación.';
  }
}
