package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoUsuarioDTO(@NotNull(message = "El estado es obligatorio") Boolean activo) {}
