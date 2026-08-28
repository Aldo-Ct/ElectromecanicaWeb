package com.electromecanica.app.dto;

import com.electromecanica.app.entity.AccionAuditoria;

import java.time.LocalDateTime;

public record AuditoriaDTO(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        AccionAuditoria accion,
        String entidad,
        String entidadId,
        String descripcion,
        String valorAnterior,
        String valorNuevo,
        LocalDateTime fecha,
        String direccionIp
) {}
