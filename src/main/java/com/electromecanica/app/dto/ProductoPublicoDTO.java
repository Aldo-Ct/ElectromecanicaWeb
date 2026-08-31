package com.electromecanica.app.dto;

import com.electromecanica.app.entity.TipoProducto;
import com.electromecanica.app.entity.UnidadMedida;

import java.math.BigDecimal;

public record ProductoPublicoDTO(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        Long categoriaId,
        String categoriaNombre,
        String marcaNombre,
        String modelo,
        TipoProducto tipoProducto,
        BigDecimal precioVenta,
        Integer stockDisponible,
        Boolean disponible,
        UnidadMedida unidadMedida,
        String imagenUrl,
        String fichaTecnicaUrl,
        String garantia,
        EspecificacionProductoDTO especificacion
) {}
