package com.electromecanica.app.dto;

import com.electromecanica.app.entity.TipoComprobante;

import java.time.LocalDateTime;

public record ComprobanteVentaDTO(
        Long id,
        TipoComprobante tipo,
        String numero,
        String serie,
        LocalDateTime fechaEmision,
        String pdfUrl
) {}
