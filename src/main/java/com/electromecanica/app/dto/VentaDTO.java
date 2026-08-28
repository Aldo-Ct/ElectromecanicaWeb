package com.electromecanica.app.dto;

import com.electromecanica.app.entity.EstadoVenta;
import com.electromecanica.app.entity.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaDTO(
        Long id,
        String numeroVenta,
        Long clienteId,
        String clienteNombre,
        String clienteNumeroDocumento,
        Long vendedorId,
        String vendedorNombre,
        LocalDateTime fecha,
        BigDecimal importeLista,
        BigDecimal descuentoNegociacion,
        BigDecimal importeProductos,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal descuento,
        BigDecimal total,
        BigDecimal montoRecibido,
        BigDecimal vuelto,
        MetodoPago metodoPago,
        EstadoVenta estado,
        String observaciones,
        List<DetalleVentaDTO> detalles,
        ComprobanteVentaDTO comprobante
) {}
