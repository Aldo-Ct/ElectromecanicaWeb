import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { RespuestaAutenticacion, RolUsuario, UsuarioSesion } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class AutenticacionService {
  private readonly claveToken = 'electromecanica_token';
  private readonly claveUsuario = 'electromecanica_usuario';
  private readonly usuarioActual = new BehaviorSubject<UsuarioSesion | null>(this.leerUsuario());
  readonly usuario$ = this.usuarioActual.asObservable();

  constructor(private api: ApiService) {}

  acceder(correo: string, contrasena: string): Observable<RespuestaAutenticacion> {
    return this.api.crear<RespuestaAutenticacion>('/autenticacion/acceso', { correo, contrasena }).pipe(
      tap(respuesta => {
        const usuario: UsuarioSesion = {
          usuarioId: respuesta.usuarioId,
          nombre: respuesta.nombre,
          apellido: respuesta.apellido,
          correo: respuesta.correo,
          rol: respuesta.rol
        };
        localStorage.setItem(this.claveToken, respuesta.token);
        localStorage.setItem(this.claveUsuario, JSON.stringify(usuario));
        this.usuarioActual.next(usuario);
      })
    );
  }

  salir(): void {
    this.api.crear<void>('/autenticacion/salida', {}).subscribe({ error: () => undefined });
    localStorage.removeItem(this.claveToken);
    localStorage.removeItem(this.claveUsuario);
    this.usuarioActual.next(null);
  }

  token(): string | null { return localStorage.getItem(this.claveToken); }
  usuario(): UsuarioSesion | null { return this.usuarioActual.value; }

  autenticado(): boolean {
    const token = this.token();
    if (!token || !this.usuario()) return false;
    try {
      const contenido = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      if (!contenido.exp || contenido.exp * 1000 <= Date.now()) {
        this.limpiar();
        return false;
      }
      return true;
    } catch {
      this.limpiar();
      return false;
    }
  }

  tieneRol(roles: RolUsuario[]): boolean {
    const rol = this.usuario()?.rol;
    return !!rol && (rol === RolUsuario.ADMINISTRADOR || roles.includes(rol));
  }

  private leerUsuario(): UsuarioSesion | null {
    try {
      const valor = localStorage.getItem(this.claveUsuario);
      return valor ? JSON.parse(valor) as UsuarioSesion : null;
    } catch { return null; }
  }

  private limpiar(): void {
    localStorage.removeItem(this.claveToken);
    localStorage.removeItem(this.claveUsuario);
    this.usuarioActual.next(null);
  }
}
