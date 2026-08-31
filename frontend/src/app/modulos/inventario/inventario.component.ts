import { Component, OnInit } from '@angular/core';
import { MovimientoInventario, Producto, RolUsuario } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';
import { InventarioService } from '../../servicios/inventario.service';
import { ProductoService } from '../../servicios/producto.service';

@Component({
    selector: 'app-inventario', templateUrl: './inventario.component.html',
    standalone: false
})
export class InventarioComponent implements OnInit {
  movimientos: MovimientoInventario[] = [];
  productos: Producto[] = [];
  ajuste = { productoId: 0, cantidad: 0, referencia: '', observaciones: '' };
  mensaje = '';
  mensajeError = '';
  constructor(private inventarioService: InventarioService, private productoService: ProductoService, private autenticacion: AutenticacionService) {}
  get puedeAjustar(): boolean { return this.autenticacion.tieneRol([RolUsuario.ALMACEN, RolUsuario.INVENTARIO]); }
  ngOnInit(): void { this.cargar(); this.cargarProductos(); }
  cargar(): void { this.inventarioService.listarMovimientos().subscribe({ next: v => this.movimientos = v, error: e => this.mensajeError = e.message }); }
  cargarProductos(): void { this.productoService.listar().subscribe({ next: v => this.productos = v.filter(p => p.activo), error: e => this.mensajeError = e.message }); }
  guardarAjuste(): void { this.mensaje = ''; this.mensajeError = ''; this.inventarioService.ajustar(this.ajuste.productoId, this.ajuste.cantidad, this.ajuste.referencia, this.ajuste.observaciones).subscribe({ next: () => { this.mensaje = 'Ajuste aplicado; historial y stock actualizados.'; this.ajuste = { productoId: 0, cantidad: 0, referencia: '', observaciones: '' }; this.cargar(); this.cargarProductos(); }, error: e => this.mensajeError = e.message }); }
  etiquetaCantidad(m: MovimientoInventario): string { return m.cantidad > 0 ? `+${m.cantidad}` : `${m.cantidad}`; }
}
