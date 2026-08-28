package com.electromecanica.app.dto;

import com.electromecanica.app.entity.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClienteDTO(
        Long id,
        @NotNull(message = "El tipo de documento es obligatorio") TipoDocumento tipoDocumento,
        @NotBlank(message = "El número de documento es obligatorio") String numeroDocumento,
        String razonSocial,
        String nombres,
        String apellidos,
        @Email(message = "El correo no tiene un formato válido") String correo,
        String telefono,
        String direccion,
        Boolean activo,
        String nombreCompleto
) {}
