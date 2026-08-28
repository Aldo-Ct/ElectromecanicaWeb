package com.electromecanica.app.dto;

import java.math.BigDecimal;

public record DetalleVentaDTO(
        Long id,
        Long productoId,
        String productoSku,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioLista,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
