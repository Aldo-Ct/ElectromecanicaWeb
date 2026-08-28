package com.electromecanica.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProveedorDTO(
        Long id,
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "\\d{11}", message = "El RUC debe contener 11 dígitos") String ruc,
        @NotBlank(message = "La razón social es obligatoria") String razonSocial,
        String nombreContacto,
        @Email(message = "El correo no tiene un formato válido") String correo,
        String telefono,
        String direccion,
        Boolean activo
) {}
