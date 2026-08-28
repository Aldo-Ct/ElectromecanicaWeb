package com.electromecanica.app.exception;

import java.time.LocalDateTime;
import java.util.List;

public record RespuestaError(
        LocalDateTime fecha,
        int estado,
        String mensaje,
        String ruta,
        List<ErrorCampo> erroresCampos
) {
    public record ErrorCampo(String campo, String mensaje) {}
}
