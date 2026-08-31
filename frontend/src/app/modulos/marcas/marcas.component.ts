import { Component, OnInit } from '@angular/core';
import { Marca } from '../../modelos/dominio.model';
import { CatalogoService } from '../../servicios/catalogo.service';

@Component({
    selector: 'app-marcas', templateUrl: './marcas.component.html',
    standalone: false
})
export class MarcasComponent implements OnInit {
  marcas: Marca[] = [];
  marca: Marca = { nombre: '', descripcion: '', logoUrl: '', activo: true };
  editandoId?: number;
  mensajeError = '';
  constructor(private catalogoService: CatalogoService) {}
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.catalogoService.listarMarcas().subscribe({ next: v => this.marcas = v, error: e => this.mensajeError = e.message }); }
  guardar(): void { const op = this.editandoId ? this.catalogoService.actualizarMarca(this.editandoId, this.marca) : this.catalogoService.crearMarca(this.marca); op.subscribe({ next: () => { this.cancelar(); this.cargar(); }, error: e => this.mensajeError = e.message }); }
  editar(m: Marca): void { this.editandoId = m.id; this.marca = { ...m }; }
  desactivar(m: Marca): void { if (m.id && confirm(`¿Desactivar ${m.nombre}?`)) this.catalogoService.desactivarMarca(m.id).subscribe(() => this.cargar()); }
  cancelar(): void { this.editandoId = undefined; this.marca = { nombre: '', descripcion: '', logoUrl: '', activo: true }; }
}
