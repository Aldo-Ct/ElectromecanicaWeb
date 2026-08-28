package com.electromecanica.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearDevolucionDTO(
        @NotNull(message = "La venta es obligatoria") Long ventaId,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotEmpty(message = "La devolución debe contener al menos un producto") List<@Valid SolicitudDetalleDevolucionDTO> detalles
) {}
