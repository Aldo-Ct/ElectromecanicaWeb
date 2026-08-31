import { Component, OnInit } from '@angular/core';
import { Proveedor } from '../../modelos/dominio.model';
import { CatalogoService } from '../../servicios/catalogo.service';

@Component({
    selector: 'app-proveedores', templateUrl: './proveedores.component.html',
    standalone: false
})
export class ProveedoresComponent implements OnInit {
  proveedores: Proveedor[] = [];
  proveedor: Proveedor = this.nuevo();
  editandoId?: number;
  mensajeError = '';
  constructor(private catalogoService: CatalogoService) {}
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.catalogoService.listarProveedores().subscribe({ next: v => this.proveedores = v, error: e => this.mensajeError = e.message }); }
  guardar(): void { const op = this.editandoId ? this.catalogoService.actualizarProveedor(this.editandoId, this.proveedor) : this.catalogoService.crearProveedor(this.proveedor); op.subscribe({ next: () => { this.cancelar(); this.cargar(); }, error: e => this.mensajeError = e.message }); }
  editar(p: Proveedor): void { this.editandoId = p.id; this.proveedor = { ...p }; }
  desactivar(p: Proveedor): void { if (p.id && confirm(`¿Desactivar ${p.razonSocial}?`)) this.catalogoService.desactivarProveedor(p.id).subscribe(() => this.cargar()); }
  cancelar(): void { this.editandoId = undefined; this.proveedor = this.nuevo(); }
  private nuevo(): Proveedor { return { ruc: '', razonSocial: '', nombreContacto: '', correo: '', telefono: '', direccion: '', activo: true }; }
}
