import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { Devolucion, RolUsuario, Venta } from '../../modelos/dominio.model';
import { AutenticacionService } from '../../servicios/autenticacion.service';
import { DevolucionService } from '../../servicios/devolucion.service';
import { VentaService } from '../../servicios/venta.service';

@Component({
    selector: 'app-devoluciones',
    templateUrl: './devoluciones.component.html',
    styleUrls: ['./devoluciones.component.scss'],
    standalone: false
})
export class DevolucionesComponent implements OnInit, OnDestroy {
  readonly plazoDevolucionDias = 7;
  devoluciones: Devolucion[] = [];
  ventas: Venta[] = [];
  ventasVencidas: Venta[] = [];
  ventaId = 0;
  terminoBusqueda = '';
  motivo = '';
  cantidades: Record<number, number> = {};
  mensaje = '';
  mensajeError = '';
  escaneando = false;
  hayCamara = false;
  ventaFueraPlazo?: Venta;
  private lectorCodigo?: import('@zxing/library').BrowserMultiFormatReader;
  private temporizadorBusqueda?: ReturnType<typeof setTimeout>;
  private ventaVencidaNotificadaId?: number;

  constructor(
    private devolucionService: DevolucionService,
    private ventaService: VentaService,
    private autenticacion: AutenticacionService,
    private zona: NgZone
  ) {}

  get puedeProcesar(): boolean { return this.autenticacion.tieneRol([RolUsuario.SOPORTE]); }
  get ventaSeleccionada(): Venta | undefined { return this.ventas.find(venta => venta.id === this.ventaId); }
  get ventasFiltradas(): Venta[] {
    const termino = this.normalizar(this.terminoBusqueda);
    if (!termino) return [];
    return this.buscarCoincidencias(this.ventas, termino).slice(0, 8);
  }

  ngOnInit(): void {
    this.cargar();
    this.cargarVentas();
    this.verificarCamara();
  }

  ngOnDestroy(): void {
    this.detenerEscaner();
    if (this.temporizadorBusqueda) clearTimeout(this.temporizadorBusqueda);
  }

  cargar(): void {
    this.devolucionService.listar().subscribe({
      next: devoluciones => this.devoluciones = devoluciones,
      error: error => this.mensajeError = error.message
    });
  }

  cargarVentas(): void {
    this.ventaService.listar().subscribe({
      next: ventas => {
        const ventasEvaluables = ventas.filter(venta => venta.estado !== 'ANULADA'
          && venta.estado !== 'DEVUELTA_TOTAL');
        this.ventas = ventasEvaluables.filter(venta => this.estaDentroDelPlazo(venta));
        this.ventasVencidas = ventasEvaluables.filter(venta => !this.estaDentroDelPlazo(venta));
        if (this.ventaId && !this.ventas.some(venta => venta.id === this.ventaId)) this.limpiarSeleccion();
      },
      error: error => this.mensajeError = error.message
    });
  }

  seleccionarVenta(venta: Venta): void {
    this.ventaId = venta.id;
    this.terminoBusqueda = venta.comprobante?.numero || venta.numeroVenta;
    this.cantidades = {};
    this.mensajeError = '';
  }

  alCambiarBusqueda(valor: string): void {
    this.mensajeError = '';
    if (this.ventaId) {
      this.ventaId = 0;
      this.cantidades = {};
    }
    if (this.temporizadorBusqueda) clearTimeout(this.temporizadorBusqueda);
    const termino = this.normalizar(valor);
    if (!termino) {
      this.ventaVencidaNotificadaId = undefined;
      this.cerrarAvisoPlazo();
      return;
    }

    this.temporizadorBusqueda = setTimeout(() => {
      if (this.buscarCoincidencias(this.ventas, termino).length) return;
      const vencidas = this.buscarCoincidencias(this.ventasVencidas, termino);
      if (vencidas.length === 1 && vencidas[0].id !== this.ventaVencidaNotificadaId) {
        this.abrirAvisoPlazo(vencidas[0]);
      }
    }, 450);
  }

  seleccionarPrimera(): void {
    const ventaQr = this.buscarVentaPorReferencia(this.terminoBusqueda);
    const venta = ventaQr || this.ventasFiltradas[0];
    if (venta) {
      this.seleccionarVenta(venta);
      return;
    }
    const ventaVencida = this.buscarVentaPorReferenciaEn(this.ventasVencidas, this.terminoBusqueda);
    if (ventaVencida) {
      this.abrirAvisoPlazo(ventaVencida);
      return;
    }
    this.mensajeError = 'No se encontró una venta disponible con esos datos.';
  }

  procesarCodigo(codigo: string): void {
    const venta = this.buscarVentaPorReferencia(codigo);
    if (!venta) {
      this.terminoBusqueda = codigo.trim();
      const ventaVencida = this.buscarVentaPorReferenciaEn(this.ventasVencidas, codigo);
      if (ventaVencida) {
        this.abrirAvisoPlazo(ventaVencida);
        return;
      }
      this.mensajeError = 'No se encontró una venta disponible con el código escaneado.';
      return;
    }
    this.seleccionarVenta(venta);
  }

  limpiarSeleccion(): void {
    this.ventaId = 0;
    this.terminoBusqueda = '';
    this.cantidades = {};
    this.ventaVencidaNotificadaId = undefined;
  }

  cerrarAvisoPlazo(): void { this.ventaFueraPlazo = undefined; }

  async iniciarEscaner(): Promise<void> {
    this.mensajeError = '';
    if (!navigator.mediaDevices?.getUserMedia) {
      this.mensajeError = 'Este navegador no permite usar la cámara. Escribe el número de la boleta.';
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
      await this.lectorCodigo.decodeFromVideoDevice(
        camaraPreferida?.deviceId || null,
        'visor-escaner-devolucion',
        resultado => {
          if (!resultado) return;
          this.zona.run(() => {
            const contenido = resultado.getText();
            this.detenerEscaner();
            this.procesarCodigo(contenido);
          });
        }
      );
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

  guardar(): void {
    const detalles = Object.entries(this.cantidades)
      .filter(([, cantidad]) => cantidad > 0)
      .map(([productoId, cantidad]) => ({ productoId: +productoId, cantidad }));
    if (!detalles.length) {
      this.mensajeError = 'Indica al menos una cantidad a devolver.';
      return;
    }
    this.mensajeError = '';
    const venta = this.ventaSeleccionada;
    this.devolucionService.crear(this.ventaId, this.motivo, detalles).subscribe({
      next: devolucion => {
        this.mensaje = `Devolución ${devolucion.numeroDevolucion} aplicada y stock repuesto.`;
        this.limpiarSeleccion();
        this.motivo = '';
        this.cargar();
        this.cargarVentas();
      },
      error: error => {
        if (venta && /plazo de devoluci[oó]n|7 d[ií]as|venci[oó]/i.test(error.message)) {
          this.abrirAvisoPlazo(venta);
        } else {
          this.mensajeError = error.message;
        }
      }
    });
  }

  private buscarVentaPorReferencia(valor: string): Venta | undefined {
    return this.buscarVentaPorReferenciaEn(this.ventas, valor);
  }

  private buscarVentaPorReferenciaEn(ventas: Venta[], valor: string): Venta | undefined {
    const texto = valor.trim();
    if (!texto) return undefined;
    const idQr = texto.match(/VENTA\s*:\s*(\d+)/i)?.[1];
    if (idQr) return ventas.find(venta => venta.id === Number(idQr));

    let referencia = texto.match(/COMPROBANTE\s*:\s*([^|]+)/i)?.[1]?.trim() || texto;
    try {
      const contenido = JSON.parse(texto) as { ventaId?: number; numeroVenta?: string; comprobante?: string };
      if (contenido.ventaId) return ventas.find(venta => venta.id === contenido.ventaId);
      referencia = contenido.comprobante || contenido.numeroVenta || referencia;
    } catch { /* El QR oficial usa el formato VENTA:id|COMPROBANTE:número. */ }

    const normalizada = this.normalizar(referencia);
    return ventas.find(venta => this.normalizar(venta.numeroVenta) === normalizada
      || this.normalizar(venta.comprobante?.numero || '') === normalizada)
      || ventas.find(venta => this.textoBusqueda(venta).includes(normalizada));
  }

  private buscarCoincidencias(ventas: Venta[], termino: string): Venta[] {
    return ventas.filter(venta => this.textoBusqueda(venta).includes(termino));
  }

  private abrirAvisoPlazo(venta: Venta): void {
    this.mensajeError = '';
    this.ventaVencidaNotificadaId = venta.id;
    this.ventaFueraPlazo = venta;
  }

  private textoBusqueda(venta: Venta): string {
    return this.normalizar([
      venta.id,
      venta.numeroVenta,
      venta.comprobante?.numero,
      venta.clienteNombre,
      venta.clienteNumeroDocumento
    ].filter(valor => valor !== undefined && valor !== null).join(' '));
  }

  fechaLimiteDevolucion(venta: Venta): Date {
    return new Date(new Date(venta.fecha).getTime() + this.plazoDevolucionDias * 24 * 60 * 60 * 1000);
  }

  private estaDentroDelPlazo(venta: Venta): boolean {
    return Date.now() < this.fechaLimiteDevolucion(venta).getTime();
  }

  private normalizar(valor: string): string {
    return valor.toLocaleLowerCase('es-PE').normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
  }

  private async verificarCamara(): Promise<void> {
    if (!navigator.mediaDevices?.enumerateDevices) return;
    try {
      this.hayCamara = (await navigator.mediaDevices.enumerateDevices())
        .some(dispositivo => dispositivo.kind === 'videoinput');
    } catch { this.hayCamara = false; }
  }

  private mensajeCamara(error: unknown): string {
    const nombre = error instanceof DOMException ? error.name : '';
    if (nombre === 'NotAllowedError') return 'No se autorizó la cámara. Habilita el permiso o escribe el número de boleta.';
    if (nombre === 'NotFoundError') return 'No se encontró una cámara. Puedes escribir el número de boleta.';
    return 'No se pudo iniciar el escáner. Revisa la cámara o busca la boleta manualmente.';
  }
}
