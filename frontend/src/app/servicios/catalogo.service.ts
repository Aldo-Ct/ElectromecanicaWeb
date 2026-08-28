import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Categoria, Cliente, Marca, Proveedor } from '../modelos/dominio.model';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  constructor(private api: ApiService) {}

  listarCategorias(): Observable<Categoria[]> { return this.api.obtener<Categoria[]>('/categorias'); }
  crearCategoria(valor: Categoria): Observable<Categoria> { return this.api.crear<Categoria>('/categorias', valor); }
  actualizarCategoria(id: number, valor: Categoria): Observable<Categoria> { return this.api.actualizar<Categoria>(`/categorias/${id}`, valor); }
  desactivarCategoria(id: number): Observable<void> { return this.api.eliminar(`/categorias/${id}`); }

  listarMarcas(): Observable<Marca[]> { return this.api.obtener<Marca[]>('/marcas'); }
  crearMarca(valor: Marca): Observable<Marca> { return this.api.crear<Marca>('/marcas', valor); }
  actualizarMarca(id: number, valor: Marca): Observable<Marca> { return this.api.actualizar<Marca>(`/marcas/${id}`, valor); }
  desactivarMarca(id: number): Observable<void> { return this.api.eliminar(`/marcas/${id}`); }

  listarClientes(): Observable<Cliente[]> { return this.api.obtener<Cliente[]>('/clientes'); }
  crearCliente(valor: Cliente): Observable<Cliente> { return this.api.crear<Cliente>('/clientes', valor); }
  actualizarCliente(id: number, valor: Cliente): Observable<Cliente> { return this.api.actualizar<Cliente>(`/clientes/${id}`, valor); }
  desactivarCliente(id: number): Observable<void> { return this.api.eliminar(`/clientes/${id}`); }

  listarProveedores(): Observable<Proveedor[]> { return this.api.obtener<Proveedor[]>('/proveedores'); }
  crearProveedor(valor: Proveedor): Observable<Proveedor> { return this.api.crear<Proveedor>('/proveedores', valor); }
  actualizarProveedor(id: number, valor: Proveedor): Observable<Proveedor> { return this.api.actualizar<Proveedor>(`/proveedores/${id}`, valor); }
  desactivarProveedor(id: number): Observable<void> { return this.api.eliminar(`/proveedores/${id}`); }
}
