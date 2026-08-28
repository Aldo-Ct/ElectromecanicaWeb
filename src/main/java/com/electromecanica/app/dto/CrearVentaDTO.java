package com.electromecanica.app.dto;

import com.electromecanica.app.entity.MetodoPago;
import com.electromecanica.app.entity.TipoComprobante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CrearVentaDTO(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        BigDecimal impuesto,
        BigDecimal descuento,
        @NotNull(message = "El método de pago es obligatorio") MetodoPago metodoPago,
        BigDecimal montoRecibido,
        @NotNull(message = "El tipo de comprobante es obligatorio") TipoComprobante tipoComprobante,
        String observaciones,
        @NotEmpty(message = "La venta debe contener al menos un producto") List<@Valid SolicitudDetalleVentaDTO> detalles
) {}
