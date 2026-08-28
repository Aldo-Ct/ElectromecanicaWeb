import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccesoComponent } from './autenticacion/acceso/acceso.component';
import { GuardiaAutenticacion } from './autenticacion/guardia-autenticacion';
import { GuardiaRol } from './autenticacion/guardia-rol';
import { RolUsuario } from './modelos/dominio.model';
import { EstructuraAdministrativaComponent } from './nucleo/estructura-administrativa/estructura-administrativa.component';
import { AlmacenComponent } from './modulos/almacen/almacen.component';
import { AuditoriaComponent } from './modulos/auditoria/auditoria.component';
import { CategoriasComponent } from './modulos/categorias/categorias.component';
import { ClientesComponent } from './modulos/clientes/clientes.component';
import { DevolucionesComponent } from './modulos/devoluciones/devoluciones.component';
import { InventarioComponent } from './modulos/inventario/inventario.component';
import { MarcasComponent } from './modulos/marcas/marcas.component';
import { PanelPrincipalComponent } from './modulos/panel-principal/panel-principal.component';
import { ProductosComponent } from './modulos/productos/productos.component';
import { ProveedoresComponent } from './modulos/proveedores/proveedores.component';
import { ReportesComponent } from './modulos/reportes/reportes.component';
import { UsuariosComponent } from './modulos/usuarios/usuarios.component';
import { VentasComponent } from './modulos/ventas/ventas.component';

const rutas: Routes = [
  { path: 'acceso', component: AccesoComponent },
  {
    path: '', component: EstructuraAdministrativaComponent, canActivate: [GuardiaAutenticacion],
    children: [
      { path: 'panel', component: PanelPrincipalComponent },
      { path: 'productos', component: ProductosComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO, RolUsuario.VENTAS, RolUsuario.GERENTE] } },
      { path: 'categorias', component: CategoriasComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN] } },
      { path: 'marcas', component: MarcasComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN] } },
      { path: 'inventario', component: InventarioComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO, RolUsuario.VENTAS, RolUsuario.GERENTE] } },
      { path: 'almacen', component: AlmacenComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN, RolUsuario.INVENTARIO] } },
      { path: 'ventas', component: VentasComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.VENTAS, RolUsuario.GERENTE, RolUsuario.SOPORTE] } },
      { path: 'clientes', component: ClientesComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.VENTAS, RolUsuario.SOPORTE, RolUsuario.GERENTE] } },
      { path: 'proveedores', component: ProveedoresComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ALMACEN, RolUsuario.GERENTE] } },
      { path: 'devoluciones', component: DevolucionesComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.SOPORTE, RolUsuario.GERENTE] } },
      { path: 'usuarios', component: UsuariosComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ADMINISTRADOR] } },
      { path: 'reportes', component: ReportesComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ANALISTA, RolUsuario.GERENTE] } },
      { path: 'auditoria', component: AuditoriaComponent, canActivate: [GuardiaRol], data: { roles: [RolUsuario.ADMINISTRADOR] } },
      { path: '', redirectTo: 'panel', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'panel' }
];

@NgModule({ imports: [RouterModule.forRoot(rutas)], exports: [RouterModule] })
export class AppRoutingModule {}
