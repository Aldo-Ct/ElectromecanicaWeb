import { Component, OnInit } from '@angular/core';
import { ResumenReporte } from '../../modelos/dominio.model';
import { ReporteService } from '../../servicios/reporte.service';

@Component({ selector: 'app-reportes', templateUrl: './reportes.component.html' })
export class ReportesComponent implements OnInit {
  resumen?: ResumenReporte;
  mensajeError = '';
  constructor(private reporteService: ReporteService) {}
  ngOnInit(): void { this.reporteService.resumen().subscribe({ next: valor => this.resumen = valor, error: error => this.mensajeError = error.message }); }
}
