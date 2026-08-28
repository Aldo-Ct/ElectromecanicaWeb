import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ResumenReporte } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  constructor(private api: ApiService) {}
  resumen(): Observable<ResumenReporte> { return this.api.obtener<ResumenReporte>('/reportes/resumen'); }
}
