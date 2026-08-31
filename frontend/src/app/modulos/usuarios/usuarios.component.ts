import { Component, OnInit } from '@angular/core';
import { RolUsuario, Usuario } from '../../modelos/dominio.model';
import { UsuarioService } from '../../servicios/usuario.service';

@Component({
    selector: 'app-usuarios', templateUrl: './usuarios.component.html',
    standalone: false
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  roles = Object.values(RolUsuario);
  nuevo = { nombre: '', apellido: '', correo: '', contrasena: '', rol: RolUsuario.VENTAS };
  mensaje = '';
  mensajeError = '';
  constructor(private usuarioService: UsuarioService) {}
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.usuarioService.listar().subscribe({ next: v => this.usuarios = v, error: e => this.mensajeError = e.message }); }
  guardar(): void { const u = this.nuevo; this.usuarioService.crear(u.nombre, u.apellido, u.correo, u.contrasena, u.rol).subscribe({ next: () => { this.mensaje = 'Usuario creado con contraseña cifrada.'; this.nuevo = { nombre: '', apellido: '', correo: '', contrasena: '', rol: RolUsuario.VENTAS }; this.cargar(); }, error: e => this.mensajeError = e.message }); }
  cambiarEstado(usuario: Usuario): void { this.usuarioService.cambiarEstado(usuario.id, !usuario.activo).subscribe({ next: () => this.cargar(), error: e => this.mensajeError = e.message }); }
}
