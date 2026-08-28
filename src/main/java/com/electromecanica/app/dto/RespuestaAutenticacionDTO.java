package com.electromecanica.app.dto;

import com.electromecanica.app.entity.Rol;

public record RespuestaAutenticacionDTO(
        String token,
        String tipo,
        Long usuarioId,
        String nombre,
        String apellido,
        String correo,
        Rol rol
) {}
