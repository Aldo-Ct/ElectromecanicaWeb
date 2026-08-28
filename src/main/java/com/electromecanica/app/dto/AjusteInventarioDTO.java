package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotNull;

public record AjusteInventarioDTO(
        @NotNull(message = "El producto es obligatorio") Long productoId,
        @NotNull(message = "La cantidad del ajuste es obligatoria") Integer cantidad,
        String referencia,
        String observaciones
) {}
