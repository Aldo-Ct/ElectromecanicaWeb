package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaDTO(
        @NotBlank(message = "La contraseña actual es obligatoria") String contrasenaActual,
        @NotBlank(message = "La contraseña nueva es obligatoria") @Size(min = 8, message = "La contraseña nueva debe tener al menos 8 caracteres") String contrasenaNueva
) {}
