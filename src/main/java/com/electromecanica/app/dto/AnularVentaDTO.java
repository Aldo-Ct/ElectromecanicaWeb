package com.electromecanica.app.dto;

import jakarta.validation.constraints.NotBlank;

public record AnularVentaDTO(@NotBlank(message = "El motivo de anulación es obligatorio") String motivo) {}
