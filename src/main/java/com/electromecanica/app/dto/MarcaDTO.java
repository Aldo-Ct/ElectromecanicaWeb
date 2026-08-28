package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotBlank;

public record MarcaDTO(
        Long id,
        @NotBlank(message = "El nombre de la marca es obligatorio") String nombre,
        String descripcion,
        String logoUrl,
        Boolean activo
) {}
