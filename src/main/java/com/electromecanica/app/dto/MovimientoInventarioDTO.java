package com.electromecanica.app.dto;

import com.electromecanica.app.entity.TipoMovimientoInventario;

import java.time.LocalDateTime;

public record MovimientoInventarioDTO(
        Long id,
        Long productoId,
        String productoNombre,
        String productoSku,
        TipoMovimientoInventario tipo,
        Integer cantidad,
        LocalDateTime fecha,
        String referencia,
        Long usuarioId,
        String usuarioNombre,
        String observaciones
) {}
