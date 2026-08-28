import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RolUsuario } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';

interface ElementoMenu {
  etiqueta: string;
  ruta: string;
  simbolo: string;
  roles: RolUsuario[];
}

@Component({
  selector: 'app-estructura-administrativa',
  templateUrl: './estructura-administrativa.component.html',
  styleUrls: ['./estructura-administrativa.component.scss']
})
export class EstructuraAdministrativaComponent {
  menuAbierto = false;
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

  constructor(public autenticacion: AutenticacionService, private enrutador: Router) {}

  get visibles(): ElementoMenu[] { return this.elementos.filter(item => this.autenticacion.tieneRol(item.roles)); }

  salir(): void {
    this.autenticacion.salir();
    this.enrutador.navigate(['/acceso']);
  }
}
