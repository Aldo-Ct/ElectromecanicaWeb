export enum RolUsuario {
  ADMINISTRADOR = 'ADMINISTRADOR',
  GERENTE = 'GERENTE',
  VENTAS = 'VENTAS',
  ALMACEN = 'ALMACEN',
  INVENTARIO = 'INVENTARIO',
  SOPORTE = 'SOPORTE',
  ANALISTA = 'ANALISTA'
}

export interface UsuarioSesion {
  usuarioId: number;
  nombre: string;
  apellido: string;
  correo: string;
  rol: RolUsuario;
}

export interface RespuestaAutenticacion extends UsuarioSesion {
  token: string;
  tipo: string;
}

export interface Categoria {
  id?: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
}

export interface Marca {
  id?: number;
  nombre: string;
  descripcion?: string;
  logoUrl?: string;
  activo: boolean;
}

export interface EspecificacionProducto {
  voltaje?: string;
  corriente?: string;
  potencia?: string;
  frecuencia?: string;
  fases?: number | null;
  gradoProteccion?: string;
  material?: string;
  dimensiones?: string;
  peso?: number | null;
  diametro?: string;
  capacidad?: string;
  velocidad?: string;
}

export interface Producto {
  id?: number;
  sku: string;
  codigoBarras?: string;
  nombre: string;
  descripcion?: string;
  categoriaId: number;
  categoriaNombre?: string;
  marcaId: number;
  marcaNombre?: string;
  modelo?: string;
  tipoProducto: 'ELECTRICO' | 'MECANICO' | 'OTRO';
  precioCompra: number;
  precioVenta: number;
  stock: number;
  stockMinimo: number;
  unidadMedida: 'UNIDAD' | 'METRO' | 'KILOGRAMO' | 'LITRO' | 'JUEGO' | 'CAJA';
  imagenUrl?: string;
  fichaTecnicaUrl?: string;
  garantia?: string;
  activo: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  especificacion?: EspecificacionProducto;
}

export interface Cliente {
  id?: number;
  tipoDocumento: 'DNI' | 'RUC' | 'CARNET_EXTRANJERIA' | 'PASAPORTE';
  numeroDocumento: string;
  razonSocial?: string;
  nombres?: string;
  apellidos?: string;
  correo?: string;
  telefono?: string;
  direccion?: string;
  activo: boolean;
  nombreCompleto?: string;
}

export interface Proveedor {
  id?: number;
  ruc: string;
  razonSocial: string;
  nombreContacto?: string;
  correo?: string;
  telefono?: string;
  direccion?: string;
  activo: boolean;
}

export interface Lote {
  id?: number;
  productoId: number;
  productoNombre?: string;
  productoSku?: string;
  codigoLote: string;
  cantidad: number;
  stockActual?: number;
  precioCompra: number;
  fechaIngreso?: string;
  proveedorId: number;
  proveedorRazonSocial?: string;
  observaciones?: string;
}

export interface MovimientoInventario {
  id: number;
  productoId: number;
  productoNombre: string;
  productoSku: string;
  tipo: 'ENTRADA' | 'VENTA' | 'DEVOLUCION' | 'AJUSTE';
  cantidad: number;
  fecha: string;
  referencia?: string;
  usuarioNombre: string;
  observaciones?: string;
}

export interface DetalleVenta {
  id?: number;
  productoId: number;
  productoSku?: string;
  productoNombre?: string;
  cantidad: number;
  precioLista?: number;
  precioUnitario: number;
  subtotal?: number;
}

export interface CrearVenta {
  clienteId: number;
  impuesto: number;
  descuento: number;
  metodoPago: 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA' | 'BILLETERA_DIGITAL' | 'CREDITO';
  montoRecibido: number;
  tipoComprobante: 'BOLETA' | 'FACTURA';
  observaciones?: string;
  detalles: DetalleVenta[];
}

export interface Venta {
  id: number;
  numeroVenta: string;
  clienteId: number;
  clienteNombre: string;
  clienteNumeroDocumento: string;
  vendedorId: number;
  vendedorNombre: string;
  fecha: string;
  importeLista: number;
  descuentoNegociacion: number;
  importeProductos: number;
  subtotal: number;
  impuesto: number;
  descuento: number;
  total: number;
  montoRecibido: number;
  vuelto: number;
  metodoPago: string;
  estado: 'COMPLETADA' | 'ANULADA' | 'DEVUELTA_PARCIAL' | 'DEVUELTA_TOTAL';
  observaciones?: string;
  detalles: DetalleVenta[];
  comprobante?: { numero: string; tipo: string; pdfUrl: string };
}

export interface Devolucion {
  id: number;
  numeroDevolucion: string;
  ventaId: number;
  numeroVenta: string;
  clienteNombre: string;
  usuarioNombre: string;
  fecha: string;
  motivo: string;
  total: number;
  estado: string;
  detalles: Array<{ productoId: number; productoSku: string; productoNombre: string; cantidad: number; monto: number }>;
}

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  rol: RolUsuario;
  activo: boolean;
  fechaCreacion: string;
}

export interface ResumenReporte {
  productosActivos: number;
  productosBajoMinimo: number;
  unidadesEnStock: number;
  ventasDelDia: number;
  ingresosDelDia: number;
  clientesActivos: number;
  proveedoresActivos: number;
  devolucionesDelDia: number;
}

export interface Auditoria {
  id: number;
  usuarioNombre: string;
  accion: string;
  entidad: string;
  entidadId?: string;
  descripcion: string;
  fecha: string;
  direccionIp?: string;
}
