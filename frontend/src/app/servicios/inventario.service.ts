import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Lote, MovimientoInventario } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  constructor(private api: ApiService) {}
  listarLotes(): Observable<Lote[]> { return this.api.obtener<Lote[]>('/inventario/lotes'); }
  registrarIngreso(lote: Lote): Observable<Lote> { return this.api.crear<Lote>('/inventario/ingresos', lote); }
  listarMovimientos(): Observable<MovimientoInventario[]> { return this.api.obtener<MovimientoInventario[]>('/inventario/movimientos'); }
  ajustar(productoId: number, cantidad: number, referencia: string, observaciones: string): Observable<MovimientoInventario> {
    return this.api.crear<MovimientoInventario>('/inventario/ajustes', { productoId, cantidad, referencia, observaciones });
  }
}
