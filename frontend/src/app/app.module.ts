import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { InterceptorToken } from './autenticacion/interceptor-token';
import { AccesoComponent } from './autenticacion/acceso/acceso.component';
import { EstructuraAdministrativaComponent } from './nucleo/estructura-administrativa/estructura-administrativa.component';
import { PanelPrincipalComponent } from './modulos/panel-principal/panel-principal.component';
import { ProductosComponent } from './modulos/productos/productos.component';
import { CategoriasComponent } from './modulos/categorias/categorias.component';
import { MarcasComponent } from './modulos/marcas/marcas.component';
import { InventarioComponent } from './modulos/inventario/inventario.component';
import { AlmacenComponent } from './modulos/almacen/almacen.component';
import { VentasComponent } from './modulos/ventas/ventas.component';
import { ClientesComponent } from './modulos/clientes/clientes.component';
import { ProveedoresComponent } from './modulos/proveedores/proveedores.component';
import { DevolucionesComponent } from './modulos/devoluciones/devoluciones.component';
import { UsuariosComponent } from './modulos/usuarios/usuarios.component';
import { ReportesComponent } from './modulos/reportes/reportes.component';
import { AuditoriaComponent } from './modulos/auditoria/auditoria.component';
import { MonedaPipe } from './compartido/moneda.pipe';

@NgModule({
  declarations: [AppComponent, AccesoComponent, EstructuraAdministrativaComponent, PanelPrincipalComponent,
    ProductosComponent, CategoriasComponent, MarcasComponent, InventarioComponent, AlmacenComponent,
    VentasComponent, ClientesComponent, ProveedoresComponent, DevolucionesComponent, UsuariosComponent,
    ReportesComponent, AuditoriaComponent, MonedaPipe],
  imports: [BrowserModule, FormsModule, ReactiveFormsModule, HttpClientModule, AppRoutingModule],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: InterceptorToken, multi: true }],
  bootstrap: [AppComponent]
})
export class AppModule {}
