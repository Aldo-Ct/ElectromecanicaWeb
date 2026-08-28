import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Devolucion } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class DevolucionService {
  constructor(private api: ApiService) {}
  listar(): Observable<Devolucion[]> { return this.api.obtener<Devolucion[]>('/devoluciones'); }
  crear(ventaId: number, motivo: string, detalles: Array<{ productoId: number; cantidad: number }>): Observable<Devolucion> {
    return this.api.crear<Devolucion>('/devoluciones', { ventaId, motivo, detalles });
  }
}
