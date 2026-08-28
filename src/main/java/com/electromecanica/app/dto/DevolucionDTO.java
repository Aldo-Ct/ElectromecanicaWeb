package com.electromecanica.app.dto;

import com.electromecanica.app.entity.EstadoDevolucion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DevolucionDTO(
        Long id,
        String numeroDevolucion,
        Long ventaId,
        String numeroVenta,
        Long clienteId,
        String clienteNombre,
        Long usuarioId,
        String usuarioNombre,
        LocalDateTime fecha,
        String motivo,
        BigDecimal total,
        EstadoDevolucion estado,
        List<DetalleDevolucionDTO> detalles
) {}
