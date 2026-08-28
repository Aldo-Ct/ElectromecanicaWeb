import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { Cliente, CrearVenta, DetalleVenta, Producto, RolUsuario, Venta } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';
import { CatalogoService } from '../../servicios/catalogo.service';
import { ProductoService } from '../../servicios/producto.service';
import { VentaService } from '../../servicios/venta.service';
import { formatearMoneda } from '../../compartido/moneda.pipe';

@Component({
  selector: 'app-ventas',
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.scss']
})
export class VentasComponent implements OnInit, OnDestroy {
  readonly factorPrecioConIgv = 1.18;
  readonly montosRapidos = [10, 20, 50, 100, 200, 500];
  ventas: Venta[] = [];
  productos: Producto[] = [];
  clientes: Cliente[] = [];
  productoSeleccionado = 0;
  cantidad = 1;
  codigoProducto = '';
  venta: CrearVenta = this.nuevaVenta();
  mensaje = '';
  mensajeError = '';
  procesando = false;
  escaneando = false;
  hayCamara = false;
  private lectorCodigo?: import('@zxing/library').BrowserMultiFormatReader;

  constructor(
    private ventaService: VentaService,
    private productoService: ProductoService,
    private catalogoService: CatalogoService,
    private autenticacion: AutenticacionService,
    private zona: NgZone
  ) {}

  get puedeVender(): boolean { return this.autenticacion.tieneRol([RolUsuario.VENTAS]); }
  get importeLista(): number {
    return this.redondear(this.venta.detalles.reduce(
      (suma, detalle) => suma + detalle.cantidad * (detalle.precioLista ?? detalle.precioUnitario), 0));
  }
  get importeProductos(): number {
    return this.redondear(this.venta.detalles.reduce(
      (suma, detalle) => suma + detalle.cantidad * Math.max(0, +detalle.precioUnitario || 0), 0));
  }
  get ahorroNegociacion(): number { return this.redondear(Math.max(0, this.importeLista - this.importeProductos)); }
  get descuentoAdicional(): number { return Math.max(0, +this.venta.descuento || 0); }
  get total(): number { return this.redondear(Math.max(0, this.importeProductos - this.descuentoAdicional)); }
  get baseImponible(): number { return this.redondear(this.total / this.factorPrecioConIgv); }
  get igv(): number { return this.redondear(this.total - this.baseImponible); }
  get montoRecibido(): number { return Math.max(0, +this.venta.montoRecibido || 0); }
  get vuelto(): number { return this.venta.metodoPago === 'EFECTIVO' ? this.redondear(Math.max(0, this.montoRecibido - this.total)) : 0; }
  get faltante(): number { return this.venta.metodoPago === 'EFECTIVO' ? this.redondear(Math.max(0, this.total - this.montoRecibido)) : 0; }
  get pagoValido(): boolean { return this.venta.metodoPago !== 'EFECTIVO' || this.montoRecibido >= this.total; }
  get puedeConfirmar(): boolean {
    return !this.procesando && !!this.venta.clienteId && this.venta.detalles.length > 0
      && this.venta.detalles.every(detalle => detalle.precioUnitario > 0)
      && this.descuentoAdicional <= this.importeProductos && this.total > 0 && this.pagoValido;
  }
  get opcionesEfectivo(): number[] { return this.montosRapidos.filter(monto => monto >= this.total).slice(0, 4); }

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogos();
    this.verificarCamara();
  }

  ngOnDestroy(): void { this.detenerEscaner(); }

  cargar(): void {
    this.ventaService.listar().subscribe({ next: ventas => this.ventas = ventas, error: error => this.mensajeError = error.message });
  }

  cargarCatalogos(): void {
    this.productoService.listar().subscribe(productos => this.productos = productos.filter(producto => producto.activo && producto.stock > 0));
    this.catalogoService.listarClientes().subscribe(clientes => this.clientes = clientes.filter(cliente => cliente.activo));
  }

  agregarProducto(): void {
    const producto = this.productos.find(item => item.id === this.productoSeleccionado);
    if (!producto || this.cantidad < 1) {
      this.mensajeError = 'Selecciona un producto y una cantidad válida.';
      return;
    }
    this.agregarAlCarrito(producto, this.cantidad);
    this.productoSeleccionado = 0;
    this.cantidad = 1;
  }

  procesarCodigo(codigo: string = this.codigoProducto): void {
    const identificador = this.extraerIdentificador(codigo);
    if (!identificador) {
      this.mensajeError = 'Ingresa o escanea un código QR, código de barras o SKU válido.';
      return;
    }
    const idNumerico = Number(identificador);
    const producto = this.productos.find(item => item.sku.toUpperCase() === identificador.toUpperCase())
      || this.productos.find(item => item.codigoBarras?.toUpperCase() === identificador.toUpperCase())
      || (Number.isInteger(idNumerico) ? this.productos.find(item => item.id === idNumerico) : undefined);
    if (!producto) {
      this.mensajeError = `No se encontró un producto activo con el código “${identificador}”.`;
      return;
    }
    this.agregarAlCarrito(producto, 1);
    this.codigoProducto = '';
  }

  async iniciarEscaner(): Promise<void> {
    this.mensajeError = '';
    if (!navigator.mediaDevices?.getUserMedia) {
      this.mensajeError = 'Este navegador no permite usar la cámara. Puedes ingresar el SKU manualmente.';
      return;
    }
    this.escaneando = true;
    await new Promise(resolve => setTimeout(resolve, 0));
    try {
      const { BrowserMultiFormatReader } = await import('@zxing/library');
      this.lectorCodigo = new BrowserMultiFormatReader(undefined, 400);
      const dispositivos = await this.lectorCodigo.listVideoInputDevices();
      const camaraPreferida = dispositivos.find(dispositivo => /back|rear|environment|trasera/i.test(dispositivo.label))
        || dispositivos[dispositivos.length - 1];
      await this.lectorCodigo.decodeFromVideoDevice(camaraPreferida?.deviceId || null, 'visor-escaner', resultado => {
        if (!resultado) return;
        this.zona.run(() => {
          this.codigoProducto = resultado.getText();
          this.detenerEscaner();
          this.procesarCodigo(this.codigoProducto);
        });
      });
    } catch (error) {
      this.detenerEscaner();
      this.mensajeError = this.mensajeCamara(error);
    }
  }

  detenerEscaner(): void {
    this.lectorCodigo?.reset();
    this.lectorCodigo = undefined;
    this.escaneando = false;
  }

  actualizarCantidad(detalle: DetalleVenta, cantidad: number): void {
    const producto = this.productos.find(item => item.id === detalle.productoId);
    const nuevaCantidad = Math.max(1, Math.floor(+cantidad || 1));
    if (producto && nuevaCantidad > producto.stock) {
      detalle.cantidad = producto.stock;
      this.mensajeError = `Solo hay ${producto.stock} unidades disponibles de ${producto.nombre}.`;
      return;
    }
    detalle.cantidad = nuevaCantidad;
    this.mensajeError = '';
  }

  actualizarPrecioAcordado(detalle: DetalleVenta, precio: number): void {
    detalle.precioUnitario = this.redondear(Math.max(0, +precio || 0));
    this.mensajeError = detalle.precioUnitario > 0
      ? ''
      : 'El precio final acordado debe ser mayor que cero.';
  }

  restaurarPrecioLista(detalle: DetalleVenta): void {
    detalle.precioUnitario = detalle.precioLista ?? detalle.precioUnitario;
    this.mensajeError = '';
  }

  quitar(detalle: DetalleVenta): void { this.venta.detalles = this.venta.detalles.filter(item => item !== detalle); }

  cambiarMetodoPago(): void { this.venta.montoRecibido = this.venta.metodoPago === 'EFECTIVO' ? 0 : this.total; }
  pagoExacto(): void { this.venta.montoRecibido = this.total; }
  usarMonto(monto: number): void { this.venta.montoRecibido = monto; }

  guardar(): void {
    if (!this.puedeConfirmar) return;
    this.procesando = true;
    this.mensaje = '';
    this.mensajeError = '';
    this.venta.impuesto = this.igv;
    if (this.venta.metodoPago !== 'EFECTIVO') this.venta.montoRecibido = this.venta.metodoPago === 'CREDITO' ? 0 : this.total;
    this.ventaService.crear(this.venta).subscribe({
      next: valor => {
        const vuelto = valor.metodoPago === 'EFECTIVO' ? ` Vuelto: ${formatearMoneda(valor.vuelto)}.` : '';
        this.mensaje = `Venta ${valor.numeroVenta} registrada correctamente.${vuelto} Descargando comprobante…`;
        this.descargarArchivo(valor, true);
        this.venta = this.nuevaVenta();
        this.procesando = false;
        this.cargar();
        this.cargarCatalogos();
      },
      error: error => { this.mensajeError = error.message; this.procesando = false; }
    });
  }

  descargar(venta: Venta): void { this.descargarArchivo(venta, false); }

  private descargarArchivo(venta: Venta, automatico: boolean): void {
    this.ventaService.descargarComprobante(venta.id).subscribe(archivo => {
      const enlace = document.createElement('a');
      const urlArchivo = URL.createObjectURL(archivo);
      enlace.href = urlArchivo;
      enlace.download = `comprobante-${venta.numeroVenta}.pdf`;
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
      window.setTimeout(() => URL.revokeObjectURL(urlArchivo), 1000);
      if (automatico) this.mensaje = `Venta ${venta.numeroVenta} registrada y comprobante descargado correctamente.`;
    }, error => {
      if (automatico) this.mensajeError = `La venta quedó registrada, pero no se pudo descargar el comprobante: ${error.message}`;
    });
  }

  private agregarAlCarrito(producto: Producto, cantidad: number): void {
    const existente = this.venta.detalles.find(detalle => detalle.productoId === producto.id);
    const nuevaCantidad = (existente?.cantidad || 0) + cantidad;
    if (nuevaCantidad > producto.stock) {
      this.mensajeError = `Stock insuficiente para ${producto.nombre}. Disponible: ${producto.stock}.`;
      return;
    }
    if (existente) existente.cantidad = nuevaCantidad;
    else this.venta.detalles.push({
      productoId: producto.id!, productoSku: producto.sku, productoNombre: producto.nombre,
      cantidad, precioLista: producto.precioVenta, precioUnitario: producto.precioVenta
    });
    this.mensajeError = '';
  }

  private extraerIdentificador(valor: string): string {
    const texto = valor.trim();
    if (!texto) return '';
    try {
      const contenido = JSON.parse(texto) as { sku?: string; codigo?: string; codigoBarras?: string; productoId?: number };
      return String(contenido.sku || contenido.codigoBarras || contenido.codigo || contenido.productoId || '').trim();
    } catch {
      try {
        const url = new URL(texto);
        return (url.searchParams.get('sku') || url.searchParams.get('codigoBarras') || url.searchParams.get('codigo') || url.searchParams.get('productoId')
          || url.pathname.split('/').filter(Boolean).pop() || '').trim();
      } catch { return texto; }
    }
  }

  private async verificarCamara(): Promise<void> {
    if (!navigator.mediaDevices?.enumerateDevices) return;
    try { this.hayCamara = (await navigator.mediaDevices.enumerateDevices()).some(dispositivo => dispositivo.kind === 'videoinput'); }
    catch { this.hayCamara = false; }
  }

  private mensajeCamara(error: unknown): string {
    const nombre = error instanceof DOMException ? error.name : '';
    if (nombre === 'NotAllowedError') return 'No se autorizó el uso de la cámara. Habilita el permiso o ingresa el SKU manualmente.';
    if (nombre === 'NotFoundError') return 'No se encontró una cámara disponible. Puedes ingresar el SKU manualmente.';
    return 'No se pudo iniciar el escáner. Revisa la cámara o ingresa el SKU manualmente.';
  }

  private redondear(valor: number): number { return Math.round((valor + Number.EPSILON) * 100) / 100; }

  private nuevaVenta(): CrearVenta {
    return {
      clienteId: 0, impuesto: 0, descuento: 0, metodoPago: 'EFECTIVO', montoRecibido: 0,
      tipoComprobante: 'BOLETA', observaciones: '', detalles: []
    };
  }
}
