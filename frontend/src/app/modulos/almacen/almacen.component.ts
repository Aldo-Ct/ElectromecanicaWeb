import { Component, OnInit } from '@angular/core';
import { Lote, Producto, Proveedor } from '../../modelos/dominio.model';
import { CatalogoService } from '../../servicios/catalogo.service';
import { InventarioService } from '../../servicios/inventario.service';
import { ProductoService } from '../../servicios/producto.service';

@Component({ selector: 'app-almacen', templateUrl: './almacen.component.html' })
export class AlmacenComponent implements OnInit {
  lotes: Lote[] = [];
  productos: Producto[] = [];
  proveedores: Proveedor[] = [];
  lote: Lote = this.nuevo();
  mensaje = '';
  mensajeError = '';
  constructor(private inventarioService: InventarioService, private productoService: ProductoService, private catalogoService: CatalogoService) {}
  ngOnInit(): void { this.cargar(); this.cargarProductos(); this.catalogoService.listarProveedores().subscribe(v => this.proveedores = v.filter(p => p.activo)); }
  cargar(): void { this.inventarioService.listarLotes().subscribe({ next: v => this.lotes = v, error: e => this.mensajeError = e.message }); }
  cargarProductos(): void { this.productoService.listar().subscribe({ next: v => this.productos = v.filter(p => p.activo), error: e => this.mensajeError = e.message }); }
  seleccionarProducto(): void { const p = this.productos.find(v => v.id === this.lote.productoId); if (p) this.lote.precioCompra = p.precioCompra; }
  guardar(): void { this.mensaje = ''; this.mensajeError = ''; this.inventarioService.registrarIngreso(this.lote).subscribe({ next: () => { this.mensaje = 'Ingreso registrado. El stock actual fue actualizado.'; this.lote = this.nuevo(); this.cargar(); this.cargarProductos(); }, error: e => this.mensajeError = e.message }); }
  private nuevo(): Lote { return { productoId: 0, proveedorId: 0, codigoLote: '', cantidad: 1, precioCompra: 0, observaciones: '' }; }
}
