package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotNull;

public record PublicacionProductoDTO(
        @NotNull(message = "Debe indicar si el producto se publicará") Boolean publicado
) {}
