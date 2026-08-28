package com.electromecanica.app.service;

import com.electromecanica.app.dto.*;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.DevolucionRepository;
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
public class DevolucionService {
    private static final int PLAZO_DEVOLUCION_DIAS = 7;
    private static final DateTimeFormatter FORMATO_FECHA_LIMITE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DevolucionRepository devolucionRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;

    public DevolucionService(DevolucionRepository devolucionRepository, VentaRepository ventaRepository,
                             ProductoRepository productoRepository, InventarioService inventarioService,
                             AuditoriaService auditoriaService) {
        this.devolucionRepository = devolucionRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<DevolucionDTO> listar() {
        return devolucionRepository.listarCompletas().stream().map(this::convertir).toList();
    }

    @Transactional
    public DevolucionDTO crear(CrearDevolucionDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        Venta venta = ventaRepository.bloquearCompletaPorId(dto.ventaId())
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        if (venta.getEstado() == EstadoVenta.ANULADA || venta.getEstado() == EstadoVenta.DEVUELTA_TOTAL) {
            throw new IllegalArgumentException("La venta no admite más devoluciones");
        }
        validarPlazoDevolucion(venta);

        Map<Long, Integer> vendido = new HashMap<>();
        Map<Long, BigDecimal> precio = new HashMap<>();
        Map<Long, Producto> productos = new HashMap<>();
        for (DetalleVenta detalle : venta.getDetalles()) {
            Long productoId = detalle.getProducto().getId();
            vendido.merge(productoId, detalle.getCantidad(), Integer::sum);
            precio.putIfAbsent(productoId, detalle.getPrecioUnitario());
            productos.put(productoId, detalle.getProducto());
        }

        Map<Long, Integer> solicitado = new LinkedHashMap<>();
        for (SolicitudDetalleDevolucionDTO detalle : dto.detalles()) {
            solicitado.merge(detalle.productoId(), detalle.cantidad(), Integer::sum);
        }

        Map<Long, Long> devueltoAntes = new HashMap<>();
        for (Map.Entry<Long, Integer> entrada : solicitado.entrySet()) {
            Integer cantidadVendida = vendido.get(entrada.getKey());
            if (cantidadVendida == null) {
                throw new IllegalArgumentException("El producto " + entrada.getKey() + " no pertenece a la venta");
            }
            long cantidadDevuelta = devolucionRepository.cantidadDevuelta(venta.getId(), entrada.getKey());
            devueltoAntes.put(entrada.getKey(), cantidadDevuelta);
            if (cantidadDevuelta + entrada.getValue() > cantidadVendida) {
                throw new IllegalArgumentException("La cantidad solicitada supera lo vendido y aún no devuelto");
            }
        }

        Devolucion devolucion = Devolucion.builder()
                .numeroDevolucion(generarNumero())
                .venta(venta)
                .cliente(venta.getCliente())
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .motivo(dto.motivo().trim())
                .estado(EstadoDevolucion.APLICADA)
                .build();
        BigDecimal importeProductos = venta.getDetalles().stream().map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal factorMontoFinal = importeProductos.signum() == 0
                ? BigDecimal.ONE
                : venta.getTotal().divide(importeProductos, 10, RoundingMode.HALF_UP);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entrada : solicitado.entrySet()) {
            Producto producto = productoRepository.bloquearPorId(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            BigDecimal montoBase = precio.get(entrada.getKey()).multiply(BigDecimal.valueOf(entrada.getValue()));
            BigDecimal monto = montoBase.multiply(factorMontoFinal).setScale(2, RoundingMode.HALF_UP);
            devolucion.agregarDetalle(DetalleDevolucion.builder().producto(producto)
                    .cantidad(entrada.getValue()).monto(monto).build());
            total = total.add(monto);
            producto.setStock(producto.getStock() + entrada.getValue());
            inventarioService.registrarMovimiento(producto, TipoMovimientoInventario.DEVOLUCION, entrada.getValue(),
                    devolucion.getNumeroDevolucion(), usuario, "Reposición por devolución de venta " + venta.getNumeroVenta());
        }
        devolucion.setTotal(total);
        devolucionRepository.save(devolucion);

        boolean devolucionTotal = vendido.entrySet().stream().allMatch(entrada -> {
            long anterior = devueltoAntes.containsKey(entrada.getKey())
                    ? devueltoAntes.get(entrada.getKey())
                    : devolucionRepository.cantidadDevuelta(venta.getId(), entrada.getKey());
            int actual = solicitado.getOrDefault(entrada.getKey(), 0);
            return anterior + actual >= entrada.getValue();
        });
        venta.setEstado(devolucionTotal ? EstadoVenta.DEVUELTA_TOTAL : EstadoVenta.DEVUELTA_PARCIAL);

        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Devolucion", devolucion.getId(),
                "Devolución aplicada a la venta " + venta.getNumeroVenta(), null, total.toPlainString(), solicitudHttp);
        return convertir(devolucion);
    }

    private DevolucionDTO convertir(Devolucion devolucion) {
        List<DetalleDevolucionDTO> detalles = devolucion.getDetalles().stream().map(detalle -> new DetalleDevolucionDTO(
                detalle.getId(), detalle.getProducto().getId(), detalle.getProducto().getSku(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getMonto())).toList();
        return new DevolucionDTO(devolucion.getId(), devolucion.getNumeroDevolucion(), devolucion.getVenta().getId(),
                devolucion.getVenta().getNumeroVenta(), devolucion.getCliente().getId(), devolucion.getCliente().getNombreCompleto(),
                devolucion.getUsuario().getId(), devolucion.getUsuario().getNombre() + " " + devolucion.getUsuario().getApellido(),
                devolucion.getFecha(), devolucion.getMotivo(), devolucion.getTotal(), devolucion.getEstado(), detalles);
    }

    private void validarPlazoDevolucion(Venta venta) {
        LocalDateTime fechaLimite = venta.getFecha().plusDays(PLAZO_DEVOLUCION_DIAS);
        if (!LocalDateTime.now().isBefore(fechaLimite)) {
            throw new IllegalArgumentException("El plazo de devolución de 7 días ya venció. Fecha límite: "
                    + fechaLimite.format(FORMATO_FECHA_LIMITE));
        }
    }

    private String generarNumero() {
        return "DEV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
