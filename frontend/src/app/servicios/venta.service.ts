import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CrearVenta, Venta } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class VentaService {
  constructor(private api: ApiService) {}
  listar(): Observable<Venta[]> { return this.api.obtener<Venta[]>('/ventas'); }
  crear(venta: CrearVenta): Observable<Venta> { return this.api.crear<Venta>('/ventas', venta); }
  descargarComprobante(id: number): Observable<Blob> { return this.api.obtenerArchivo(`/ventas/${id}/comprobante/pdf`); }
}
