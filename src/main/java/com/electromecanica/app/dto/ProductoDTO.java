package com.electromecanica.app.dto;

import com.electromecanica.app.entity.TipoProducto;
import com.electromecanica.app.entity.UnidadMedida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDTO(
        Long id,
        @NotBlank(message = "El SKU es obligatorio") String sku,
        @Size(max = 120, message = "El código de barras no puede superar 120 caracteres") String codigoBarras,
        @NotBlank(message = "El nombre del producto es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        String categoriaNombre,
        @NotNull(message = "La marca es obligatoria") Long marcaId,
        String marcaNombre,
        String modelo,
        @NotNull(message = "El tipo de producto es obligatorio") TipoProducto tipoProducto,
        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de compra no puede ser negativo") BigDecimal precioCompra,
        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de venta final debe ser mayor que cero") BigDecimal precioVenta,
        @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @Min(value = 0, message = "El stock mínimo no puede ser negativo") Integer stockMinimo,
        @NotNull(message = "La unidad de medida es obligatoria") UnidadMedida unidadMedida,
        String imagenUrl,
        String fichaTecnicaUrl,
        String garantia,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        EspecificacionProductoDTO especificacion
) {}
