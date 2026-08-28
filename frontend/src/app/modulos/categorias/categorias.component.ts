import { Component, OnInit } from '@angular/core';
import { Categoria } from '../../modelos/dominio.model';
import { CatalogoService } from '../../servicios/catalogo.service';

@Component({ selector: 'app-categorias', templateUrl: './categorias.component.html' })
export class CategoriasComponent implements OnInit {
  categorias: Categoria[] = [];
  categoria: Categoria = { nombre: '', descripcion: '', activo: true };
  editandoId?: number;
  mensajeError = '';
  constructor(private catalogoService: CatalogoService) {}
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.catalogoService.listarCategorias().subscribe({ next: v => this.categorias = v, error: e => this.mensajeError = e.message }); }
  guardar(): void {
    const operacion = this.editandoId ? this.catalogoService.actualizarCategoria(this.editandoId, this.categoria) : this.catalogoService.crearCategoria(this.categoria);
    operacion.subscribe({ next: () => { this.cancelar(); this.cargar(); }, error: e => this.mensajeError = e.message });
  }
  editar(c: Categoria): void { this.editandoId = c.id; this.categoria = { ...c }; }
  desactivar(c: Categoria): void { if (c.id && confirm(`¿Desactivar ${c.nombre}?`)) this.catalogoService.desactivarCategoria(c.id).subscribe(() => this.cargar()); }
  cancelar(): void { this.editandoId = undefined; this.categoria = { nombre: '', descripcion: '', activo: true }; }
}
