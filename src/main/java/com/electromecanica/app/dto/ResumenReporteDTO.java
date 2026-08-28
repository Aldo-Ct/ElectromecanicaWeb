package com.electromecanica.app.dto;

import java.math.BigDecimal;

public record ResumenReporteDTO(
        long productosActivos,
        long productosBajoMinimo,
        int unidadesEnStock,
        long ventasDelDia,
        BigDecimal ingresosDelDia,
        long clientesActivos,
        long proveedoresActivos,
        long devolucionesDelDia
) {}
