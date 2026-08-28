package com.electromecanica.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteDTO(
        Long id,
        @NotNull(message = "El producto es obligatorio") Long productoId,
        String productoNombre,
        String productoSku,
        @NotBlank(message = "El código de lote es obligatorio") String codigoLote,
        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser mayor que cero") Integer cantidad,
        Integer stockActual,
        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo") BigDecimal precioCompra,
        LocalDateTime fechaIngreso,
        @NotNull(message = "El proveedor es obligatorio") Long proveedorId,
        String proveedorRazonSocial,
        String observaciones
) {}
