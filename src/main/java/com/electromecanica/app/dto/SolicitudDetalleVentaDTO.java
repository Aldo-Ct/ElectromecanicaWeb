package com.electromecanica.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record SolicitudDetalleVentaDTO(
        @NotNull(message = "El producto es obligatorio") Long productoId,
        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser mayor que cero") Integer cantidad,
        @DecimalMin(value = "0.01", message = "El precio final acordado debe ser mayor que cero")
        BigDecimal precioUnitario
) {}
