import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { RolUsuario } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';
import { UsuarioService } from '../../servicios/usuario.service';

interface ElementoMenu {
  etiqueta: string;
  ruta: string;
  simbolo: string;
  roles: RolUsuario[];
}

@Component({
    selector: 'app-estructura-administrativa',
    templateUrl: './estructura-administrativa.component.html',
    styleUrls: ['./estructura-administrativa.component.scss'],
    standalone: false
})
export class EstructuraAdministrativaComponent {
  menuAbierto = false;
  menuPerfilAbierto = false;
  cambioContrasenaAbierto = false;
  procesandoContrasena = false;
  mostrarContrasenas = false;
  mensajeContrasena = '';
  errorContrasena = '';
  contrasenas = { actual: '', nueva: '', confirmacion: '' };
  readonly elementos: ElementoMenu[] = [
    { etiqueta: 'Panel principal', ruta: '/panel', simbolo: '▦', roles: Object.values(RolUsuario) },
    { etiqueta: 'Productos', ruta: '/productos', simbolo: '⚙', roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO, RolUsuario.VENTAS, RolUsuario.GERENTE] },
    { etiqueta: 'Categorías', ruta: '/categorias', simbolo: '◫', roles: [RolUsuario.ALMACEN] },
    { etiqueta: 'Marcas', ruta: '/marcas', simbolo: '◆', roles: [RolUsuario.ALMACEN] },
    { etiqueta: 'Inventario', ruta: '/inventario', simbolo: '⇄', roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO, RolUsuario.VENTAS, RolUsuario.GERENTE] },
    { etiqueta: 'Almacén', ruta: '/almacen', simbolo: '▤', roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO] },
    { etiqueta: 'Ventas', ruta: '/ventas', simbolo: 'S/', roles: [RolUsuario.VENTAS, RolUsuario.GERENTE, RolUsuario.SOPORTE] },
    { etiqueta: 'Clientes', ruta: '/clientes', simbolo: '◎', roles: [RolUsuario.VENTAS, RolUsuario.SOPORTE, RolUsuario.GERENTE] },
    { etiqueta: 'Proveedores', ruta: '/proveedores', simbolo: '▣', roles: [RolUsuario.ALMACEN, RolUsuario.GERENTE] },
    { etiqueta: 'Devoluciones', ruta: '/devoluciones', simbolo: '↩', roles: [RolUsuario.SOPORTE, RolUsuario.GERENTE] },
    { etiqueta: 'Usuarios', ruta: '/usuarios', simbolo: '♙', roles: [RolUsuario.ADMINISTRADOR] },
    { etiqueta: 'Reportes', ruta: '/reportes', simbolo: '◒', roles: [RolUsuario.ANALISTA, RolUsuario.GERENTE] },
    { etiqueta: 'Auditoría', ruta: '/auditoria', simbolo: '✓', roles: [RolUsuario.ADMINISTRADOR] }
  ];

  constructor(
    public autenticacion: AutenticacionService,
    private enrutador: Router,
    private usuarioService: UsuarioService
  ) {}

  get visibles(): ElementoMenu[] { return this.elementos.filter(item => this.autenticacion.tieneRol(item.roles)); }

  alternarPerfil(): void {
    this.menuPerfilAbierto = !this.menuPerfilAbierto;
  }

  cerrarPerfil(): void {
    this.menuPerfilAbierto = false;
  }

  abrirCambioContrasena(): void {
    this.menuPerfilAbierto = false;
    this.cambioContrasenaAbierto = true;
    this.mensajeContrasena = '';
    this.errorContrasena = '';
    this.contrasenas = { actual: '', nueva: '', confirmacion: '' };
  }

  cerrarCambioContrasena(): void {
    if (this.procesandoContrasena) return;
    this.cambioContrasenaAbierto = false;
    this.mostrarContrasenas = false;
  }

  cambiarContrasena(): void {
    if (this.procesandoContrasena) return;
    this.errorContrasena = '';
    this.mensajeContrasena = '';
    if (this.contrasenas.nueva.length < 8) {
      this.errorContrasena = 'La contraseña nueva debe tener al menos 8 caracteres.';
      return;
    }
    if (this.contrasenas.nueva !== this.contrasenas.confirmacion) {
      this.errorContrasena = 'La confirmación no coincide con la contraseña nueva.';
      return;
    }
    if (this.contrasenas.actual === this.contrasenas.nueva) {
      this.errorContrasena = 'La contraseña nueva debe ser diferente de la actual.';
      return;
    }

    this.procesandoContrasena = true;
    this.usuarioService.cambiarContrasena(this.contrasenas.actual, this.contrasenas.nueva)
      .pipe(finalize(() => this.procesandoContrasena = false))
      .subscribe({
        next: () => {
          this.mensajeContrasena = 'Contraseña actualizada correctamente.';
          this.contrasenas = { actual: '', nueva: '', confirmacion: '' };
        },
        error: error => this.errorContrasena = error.message
      });
  }

  @HostListener('document:keydown.escape')
  cerrarElementosFlotantes(): void {
    if (this.cambioContrasenaAbierto) {
      this.cerrarCambioContrasena();
      return;
    }
    this.cerrarPerfil();
  }

  salir(): void {
    this.menuPerfilAbierto = false;
    this.menuAbierto = false;
    this.autenticacion.salir();
    this.enrutador.navigate(['/acceso']);
  }
}
