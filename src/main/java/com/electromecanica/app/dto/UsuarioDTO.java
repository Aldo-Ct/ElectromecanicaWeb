package com.electromecanica.app.dto;

import com.electromecanica.app.entity.Rol;

import java.time.LocalDateTime;

public record UsuarioDTO(
        Long id,
        String nombre,
        String apellido,
        String correo,
        Rol rol,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
