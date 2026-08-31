import { Component, OnInit } from '@angular/core';
import { Cliente } from '../../modelos/dominio.model';
import { CatalogoService } from '../../servicios/catalogo.service';

@Component({
    selector: 'app-clientes', templateUrl: './clientes.component.html',
    standalone: false
})
export class ClientesComponent implements OnInit {
  clientes: Cliente[] = [];
  cliente: Cliente = this.nuevo();
  editandoId?: number;
  mensajeError = '';
  constructor(private catalogoService: CatalogoService) {}
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.catalogoService.listarClientes().subscribe({ next: v => this.clientes = v, error: e => this.mensajeError = e.message }); }
  guardar(): void { const op = this.editandoId ? this.catalogoService.actualizarCliente(this.editandoId, this.cliente) : this.catalogoService.crearCliente(this.cliente); op.subscribe({ next: () => { this.cancelar(); this.cargar(); }, error: e => this.mensajeError = e.message }); }
  editar(c: Cliente): void { this.editandoId = c.id; this.cliente = { ...c }; }
  desactivar(c: Cliente): void { if (c.id && confirm(`¿Desactivar ${c.nombreCompleto}?`)) this.catalogoService.desactivarCliente(c.id).subscribe(() => this.cargar()); }
  cancelar(): void { this.editandoId = undefined; this.cliente = this.nuevo(); }
  private nuevo(): Cliente { return { tipoDocumento: 'DNI', numeroDocumento: '', razonSocial: '', nombres: '', apellidos: '', correo: '', telefono: '', direccion: '', activo: true }; }
}
