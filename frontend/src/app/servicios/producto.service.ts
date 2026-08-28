import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Producto } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  constructor(private api: ApiService) {}
  listar(buscar = ''): Observable<Producto[]> {
    return this.api.obtener<Producto[]>(`/productos${buscar ? `?buscar=${encodeURIComponent(buscar)}` : ''}`);
  }
  crear(producto: Producto): Observable<Producto> { return this.api.crear<Producto>('/productos', producto); }
  actualizar(id: number, producto: Producto): Observable<Producto> { return this.api.actualizar<Producto>(`/productos/${id}`, producto); }
  desactivar(id: number): Observable<void> { return this.api.eliminar(`/productos/${id}`); }
}
