package com.electromecanica.app.service;

import com.electromecanica.app.dto.*;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.ProductoRepository;
import com.electromecanica.app.repository.VentaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VentaService {
    private static final BigDecimal FACTOR_PRECIO_CON_IGV = new BigDecimal("1.18");
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteService clienteService;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;
    private final ComprobanteVentaService comprobanteVentaService;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        ClienteService clienteService, InventarioService inventarioService,
                        AuditoriaService auditoriaService, ComprobanteVentaService comprobanteVentaService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteService = clienteService;
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
        this.comprobanteVentaService = comprobanteVentaService;
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listar() {
        return ventaRepository.listarCompletas().stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerDTO(Long id) {
        return convertir(obtenerCompleta(id));
    }

    @Transactional
    public VentaDTO crear(CrearVentaDTO dto, Usuario vendedor, HttpServletRequest solicitudHttp) {
        Cliente cliente = clienteService.obtener(dto.clienteId());
        if (!cliente.getActivo()) {
            throw new IllegalArgumentException("El cliente está inactivo");
        }
        BigDecimal descuento = normalizarMonto(dto.descuento(), "El descuento");

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        Map<Long, BigDecimal> preciosSolicitados = new HashMap<>();
        for (SolicitudDetalleVentaDTO detalle : dto.detalles()) {
            cantidades.merge(detalle.productoId(), detalle.cantidad(), Integer::sum);
            if (detalle.precioUnitario() != null) {
                if (detalle.precioUnitario().signum() < 0) {
                    throw new IllegalArgumentException("El precio unitario no puede ser negativo");
                }
                preciosSolicitados.put(detalle.productoId(), detalle.precioUnitario());
            }
        }

        Venta venta = Venta.builder()
                .numeroVenta(generarNumero("VEN"))
                .cliente(cliente)
                .vendedor(vendedor)
                .fecha(LocalDateTime.now())
                .impuesto(BigDecimal.ZERO)
                .descuento(descuento)
                .montoRecibido(BigDecimal.ZERO)
                .vuelto(BigDecimal.ZERO)
                .metodoPago(dto.metodoPago())
                .estado(EstadoVenta.COMPLETADA)
                .observaciones(dto.observaciones())
                .build();

        BigDecimal importeProductos = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.bloquearPorId(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + entrada.getKey()));
            int cantidad = entrada.getValue();
            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo");
            }
            if (producto.getStock() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock());
            }
            BigDecimal precioLista = normalizarMonto(producto.getPrecioVenta(), "El precio de lista");
            BigDecimal precio = normalizarMonto(
                    preciosSolicitados.getOrDefault(producto.getId(), precioLista), "El precio final acordado");
            if (precio.signum() == 0) {
                throw new IllegalArgumentException("El precio final acordado debe ser mayor que cero");
            }
            BigDecimal subtotalDetalle = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            venta.agregarDetalle(DetalleVenta.builder().producto(producto).cantidad(cantidad)
                    .precioLista(precioLista).precioUnitario(precio).subtotal(subtotalDetalle).build());
            importeProductos = importeProductos.add(subtotalDetalle);
            producto.setStock(producto.getStock() - cantidad);
            inventarioService.registrarMovimiento(producto, TipoMovimientoInventario.VENTA, -cantidad,
                    venta.getNumeroVenta(), vendedor, "Salida por venta");
        }
        if (descuento.compareTo(importeProductos) > 0) {
            throw new IllegalArgumentException("El descuento adicional no puede superar el importe de los productos");
        }
        BigDecimal total = importeProductos.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        BigDecimal baseImponible = total.divide(FACTOR_PRECIO_CON_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal impuesto = total.subtract(baseImponible).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoRecibido;
        BigDecimal vuelto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (dto.metodoPago() == MetodoPago.EFECTIVO) {
            montoRecibido = normalizarMonto(dto.montoRecibido(), "El monto recibido");
            if (montoRecibido.compareTo(total) < 0) {
                throw new IllegalArgumentException("El monto recibido es menor que el total de la venta");
            }
            vuelto = montoRecibido.subtract(total).setScale(2, RoundingMode.HALF_UP);
        } else if (dto.metodoPago() == MetodoPago.CREDITO) {
            montoRecibido = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            montoRecibido = total;
        }
        venta.setSubtotal(baseImponible);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setMontoRecibido(montoRecibido);
        venta.setVuelto(vuelto);
        ventaRepository.saveAndFlush(venta);

        String serie = dto.tipoComprobante() == TipoComprobante.FACTURA ? "F001" : "B001";
        String numeroComprobante = serie + "-" + String.format("%08d", venta.getId());
        ComprobanteVenta comprobante = ComprobanteVenta.builder()
                .venta(venta).tipo(dto.tipoComprobante()).serie(serie).numero(numeroComprobante)
                .fechaEmision(LocalDateTime.now())
                .pdfUrl("/api/ventas/" + venta.getId() + "/comprobante/pdf")
                .build();
        venta.setComprobante(comprobante);
        ventaRepository.save(venta);

        auditoriaService.registrar(AccionAuditoria.CREACION, vendedor, "Venta", venta.getId(),
                "Venta registrada: " + venta.getNumeroVenta(), null, venta.getTotal().toPlainString(), solicitudHttp);
        return convertir(venta);
    }

    @Transactional
    public VentaDTO anular(Long id, Usuario usuario, String motivo, HttpServletRequest solicitudHttp) {
        Venta venta = ventaRepository.bloquearCompletaPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
            throw new IllegalArgumentException("Solo se puede anular una venta completada sin devoluciones");
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoRepository.bloquearPorId(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            producto.setStock(producto.getStock() + detalle.getCantidad());
            inventarioService.registrarMovimiento(producto, TipoMovimientoInventario.AJUSTE, detalle.getCantidad(),
                    venta.getNumeroVenta(), usuario, "Reposición por anulación: " + motivo);
        }
        venta.setEstado(EstadoVenta.ANULADA);
        auditoriaService.registrar(AccionAuditoria.ANULACION, usuario, "Venta", id,
                "Venta anulada: " + motivo, EstadoVenta.COMPLETADA.name(), EstadoVenta.ANULADA.name(), solicitudHttp);
        return convertir(venta);
    }

    @Transactional(readOnly = true)
    public byte[] generarComprobantePdf(Long id) {
        return comprobanteVentaService.generarPdf(obtenerCompleta(id));
    }

    public Venta obtenerCompleta(Long id) {
        return ventaRepository.buscarCompletaPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
    }

    public VentaDTO convertir(Venta venta) {
        List<DetalleVentaDTO> detalles = venta.getDetalles().stream().map(detalle -> new DetalleVentaDTO(
                detalle.getId(), detalle.getProducto().getId(), detalle.getProducto().getSku(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getPrecioLista(),
                detalle.getPrecioUnitario(), detalle.getSubtotal())).toList();
        BigDecimal importeLista = venta.getDetalles().stream()
                .map(detalle -> detalle.getPrecioLista().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal importeProductos = venta.getDetalles().stream().map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuentoNegociacion = importeLista.subtract(importeProductos).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        ComprobanteVenta comprobante = venta.getComprobante();
        ComprobanteVentaDTO comprobanteDTO = comprobante == null ? null : new ComprobanteVentaDTO(
                comprobante.getId(), comprobante.getTipo(), comprobante.getNumero(), comprobante.getSerie(),
                comprobante.getFechaEmision(), comprobante.getPdfUrl());
        return new VentaDTO(venta.getId(), venta.getNumeroVenta(), venta.getCliente().getId(),
                venta.getCliente().getNombreCompleto(), venta.getCliente().getNumeroDocumento(), venta.getVendedor().getId(),
                venta.getVendedor().getNombre() + " " + venta.getVendedor().getApellido(), venta.getFecha(),
                importeLista, descuentoNegociacion, importeProductos,
                venta.getSubtotal(), venta.getImpuesto(), venta.getDescuento(), venta.getTotal(),
                venta.getMontoRecibido(), venta.getVuelto(),
                venta.getMetodoPago(), venta.getEstado(), venta.getObservaciones(), detalles, comprobanteDTO);
    }

    private BigDecimal normalizarMonto(BigDecimal monto, String nombre) {
        BigDecimal normalizado = monto == null ? BigDecimal.ZERO : monto.setScale(2, RoundingMode.HALF_UP);
        if (normalizado.signum() < 0) {
            throw new IllegalArgumentException(nombre + " no puede ser negativo");
        }
        return normalizado;
    }

    private String generarNumero(String prefijo) {
        return prefijo + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
