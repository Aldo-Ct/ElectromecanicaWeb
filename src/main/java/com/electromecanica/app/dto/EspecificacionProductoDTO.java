package com.electromecanica.app.dto;

import java.math.BigDecimal;

public record EspecificacionProductoDTO(
        String voltaje,
        String corriente,
        String potencia,
        String frecuencia,
        Integer fases,
        String gradoProteccion,
        String material,
        String dimensiones,
        BigDecimal peso,
        String diametro,
        String capacidad,
        String velocidad
) {}
