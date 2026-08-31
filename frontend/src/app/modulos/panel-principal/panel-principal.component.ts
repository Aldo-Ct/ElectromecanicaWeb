import { Component, OnInit } from '@angular/core';
import { Producto, ResumenReporte } from '../../modelos/dominio.model';
import { ProductoService } from '../../servicios/producto.service';
import { ReporteService } from '../../servicios/reporte.service';

@Component({
    selector: 'app-panel-principal', templateUrl: './panel-principal.component.html',
    standalone: false
})
export class PanelPrincipalComponent implements OnInit {
  resumen?: ResumenReporte;
  productosCriticos: Producto[] = [];
  cargando = true;
  mensajeError = '';

  constructor(private reporteService: ReporteService, private productoService: ProductoService) {}

  ngOnInit(): void {
    this.reporteService.resumen().subscribe({
      next: resumen => { this.resumen = resumen; this.cargando = false; },
      error: error => { this.mensajeError = error.message; this.cargando = false; }
    });
    this.productoService.listar().subscribe({
      next: productos => this.productosCriticos = productos.filter(p => p.activo && p.stock <= p.stockMinimo).slice(0, 6)
    });
  }
}
