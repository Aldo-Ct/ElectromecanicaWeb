package com.electromecanica.app.dto;

import java.math.BigDecimal;

public record DetalleDevolucionDTO(
        Long id,
        Long productoId,
        String productoSku,
        String productoNombre,
        Integer cantidad,
        BigDecimal monto
) {}
