import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { RolUsuario, Usuario } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private api: ApiService) {}
  listar(): Observable<Usuario[]> { return this.api.obtener<Usuario[]>('/usuarios'); }
  crear(nombre: string, apellido: string, correo: string, contrasena: string, rol: RolUsuario): Observable<Usuario> {
    return this.api.crear<Usuario>('/usuarios', { nombre, apellido, correo, contrasena, rol });
  }
  cambiarEstado(id: number, activo: boolean): Observable<Usuario> {
    return this.api.actualizar<Usuario>(`/usuarios/${id}/estado`, { activo });
  }
  cambiarContrasena(contrasenaActual: string, contrasenaNueva: string): Observable<void> {
    return this.api.actualizar<void>('/usuarios/contrasena', { contrasenaActual, contrasenaNueva });
  }
}
