import { Component, OnInit } from '@angular/core';
import { Auditoria } from '../../modelos/dominio.model';
import { AuditoriaService } from '../../servicios/auditoria.service';

@Component({
    selector: 'app-auditoria', templateUrl: './auditoria.component.html',
    standalone: false
})
export class AuditoriaComponent implements OnInit {
  registros: Auditoria[] = [];
  mensajeError = '';
  constructor(private auditoriaService: AuditoriaService) {}
  ngOnInit(): void { this.auditoriaService.listar().subscribe({ next: v => this.registros = v, error: e => this.mensajeError = e.message }); }
}
