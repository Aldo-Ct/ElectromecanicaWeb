import { Component, OnDestroy, OnInit } from '@angular/core';
import { Categoria, Marca, Producto, RolUsuario } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';
import { CatalogoService } from '../../servicios/catalogo.service';
import { ProductoService } from '../../servicios/producto.service';
import { formatearMoneda } from '../../compartido/moneda.pipe';

@Component({
    selector: 'app-productos',
    templateUrl: './productos.component.html',
    styleUrls: ['./productos.component.scss'],
    standalone: false
})
export class ProductosComponent implements OnInit, OnDestroy {
  productos: Producto[] = [];
  categorias: Categoria[] = [];
  marcas: Marca[] = [];
  buscar = '';
  editandoId?: number;
  guardando = false;
  actualizandoPublicacionId?: number;
  mensaje = '';
  mensajeError = '';
  producto: Producto = this.nuevoProducto();
  productoEtiqueta?: Producto;
  urlQr = '';
  private contenidoSvgQr = '';

  constructor(
    private productoService: ProductoService,
    private catalogoService: CatalogoService,
    private autenticacion: AutenticacionService
  ) {}

  get puedeEditar(): boolean {
    return this.autenticacion.tieneRol([RolUsuario.ALMACEN]);
  }

  ngOnInit(): void {
    this.cargar();
    this.catalogoService.listarCategorias().subscribe(valores => this.categorias = valores.filter(v => v.activo));
    this.catalogoService.listarMarcas().subscribe(valores => this.marcas = valores.filter(v => v.activo));
  }

  ngOnDestroy(): void { this.cerrarEtiqueta(); }

  cargar(): void {
    this.productoService.listar(this.buscar).subscribe({
      next: valores => this.productos = valores,
      error: error => this.mensajeError = error.message
    });
  }

  editar(valor: Producto): void {
    this.editandoId = valor.id;
    this.producto = { ...valor, especificacion: { ...(valor.especificacion || {}) } };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  guardar(): void {
    this.guardando = true;
    this.mensaje = '';
    this.mensajeError = '';
    const operacion = this.editandoId
      ? this.productoService.actualizar(this.editandoId, this.producto)
      : this.productoService.crear(this.producto);
    operacion.subscribe({
      next: () => {
        this.mensaje = this.editandoId ? 'Producto actualizado correctamente.' : 'Producto creado correctamente.';
        this.cancelar();
        this.cargar();
        this.guardando = false;
      },
      error: error => { this.mensajeError = error.message; this.guardando = false; }
    });
  }

  desactivar(valor: Producto): void {
    if (!valor.id || !confirm(`¿Desactivar ${valor.nombre}?`)) return;
    this.productoService.desactivar(valor.id).subscribe({ next: () => this.cargar(), error: error => this.mensajeError = error.message });
  }

  cambiarPublicacion(valor: Producto): void {
    if (!valor.id || this.actualizandoPublicacionId) return;
    const publicar = !valor.publicadoVenta;
    this.actualizandoPublicacionId = valor.id;
    this.mensaje = '';
    this.mensajeError = '';
    this.productoService.actualizarPublicacion(valor.id, publicar).subscribe({
      next: actualizado => {
        valor.publicadoVenta = actualizado.publicadoVenta;
        this.mensaje = publicadoTexto(actualizado.publicadoVenta);
        this.actualizandoPublicacionId = undefined;
      },
      error: error => {
        this.mensajeError = error.message;
        this.actualizandoPublicacionId = undefined;
      }
    });
  }

  async abrirEtiqueta(valor: Producto): Promise<void> {
    this.cerrarEtiqueta();
    this.productoEtiqueta = valor;
    try {
      const { BrowserQRCodeSvgWriter } = await import('@zxing/library');
      const elementoSvg = new BrowserQRCodeSvgWriter().write(valor.sku, 280, 280);
      elementoSvg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
      this.contenidoSvgQr = new XMLSerializer().serializeToString(elementoSvg);
      this.urlQr = URL.createObjectURL(new Blob([this.contenidoSvgQr], { type: 'image/svg+xml;charset=utf-8' }));
    } catch {
      this.productoEtiqueta = undefined;
      this.mensajeError = 'No se pudo generar el QR. Recarga la página e inténtalo nuevamente.';
    }
  }

  cerrarEtiqueta(): void {
    if (this.urlQr) URL.revokeObjectURL(this.urlQr);
    this.urlQr = '';
    this.contenidoSvgQr = '';
    this.productoEtiqueta = undefined;
  }

  descargarQr(): void {
    if (!this.productoEtiqueta || !this.contenidoSvgQr) return;
    const urlDescarga = URL.createObjectURL(new Blob([this.contenidoSvgQr], { type: 'image/svg+xml;charset=utf-8' }));
    const enlace = document.createElement('a');
    enlace.href = urlDescarga;
    enlace.download = `QR-${this.productoEtiqueta.sku}.svg`;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    window.setTimeout(() => URL.revokeObjectURL(urlDescarga), 1000);
  }

  imprimirEtiqueta(): void {
    if (!this.productoEtiqueta || !this.contenidoSvgQr) return;
    const producto = this.productoEtiqueta;
    const ventanaImpresion = window.open('', '_blank', 'width=520,height=680');
    if (!ventanaImpresion) {
      this.mensajeError = 'El navegador bloqueó la ventana de impresión. Habilita las ventanas emergentes para este sitio.';
      return;
    }
    const precio = formatearMoneda(producto.precioVenta);
    ventanaImpresion.document.write(`<!doctype html><html lang="es"><head><meta charset="utf-8"><title>Etiqueta ${this.escaparHtml(producto.sku)}</title><style>body{font-family:Arial,sans-serif;display:grid;place-items:center;margin:0;padding:24px}.etiqueta{width:330px;border:2px solid #172b4d;border-radius:14px;padding:20px;text-align:center}.empresa{font-size:12px;font-weight:800;letter-spacing:.12em;color:#176aad}.nombre{font-size:19px;font-weight:800;margin:10px 0 2px}.sku{font:700 15px monospace;color:#334e68}.qr svg{width:250px;height:250px}.barra{font:600 12px monospace;margin-top:4px}.precio{font-size:22px;font-weight:900;margin-top:10px}.nota{font-size:11px;color:#52667a;margin-top:2px}@media print{body{padding:0}.etiqueta{break-inside:avoid}}</style></head><body><div class="etiqueta"><div class="empresa">ELECTROMECÁNICA</div><div class="nombre">${this.escaparHtml(producto.nombre)}</div><div class="sku">${this.escaparHtml(producto.sku)}</div><div class="qr">${this.contenidoSvgQr}</div>${producto.codigoBarras ? `<div class="barra">Código: ${this.escaparHtml(producto.codigoBarras)}</div>` : ''}<div class="precio">${precio}</div><div class="nota">Precio final con IGV incluido</div></div><script>window.onload=()=>{window.print();window.close()}<\/script></body></html>`);
    ventanaImpresion.document.close();
  }

  cancelar(): void { this.editandoId = undefined; this.producto = this.nuevoProducto(); }

  private nuevoProducto(): Producto {
    return {
      sku: '', codigoBarras: '', nombre: '', descripcion: '', categoriaId: 0, marcaId: 0, modelo: '',
      tipoProducto: 'ELECTRICO', precioCompra: 0, precioVenta: 0, stock: 0, stockMinimo: 2,
      unidadMedida: 'UNIDAD', garantia: '12 meses', activo: true, publicadoVenta: false, especificacion: {}
    };
  }

  private escaparHtml(valor: string): string {
    return valor.replace(/[&<>'"]/g, caracter => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[caracter] || caracter));
  }
}

function publicadoTexto(publicado: boolean): string {
  return publicado ? 'El producto ya está visible en la tienda web.' : 'El producto fue ocultado de la tienda web.';
}
