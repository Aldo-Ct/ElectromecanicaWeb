ALTER TABLE productos
    ADD COLUMN codigo_barras VARCHAR(120);

CREATE UNIQUE INDEX idx_producto_codigo_barras
    ON productos (LOWER(codigo_barras))
    WHERE codigo_barras IS NOT NULL AND BTRIM(codigo_barras) <> '';

ALTER TABLE detalles_venta
    ADD COLUMN precio_lista NUMERIC(14,2);

-- Las ventas de las versiones anteriores guardaban precios sin IGV en sus detalles.
-- Se convierten una sola vez para conservar el mismo total cobrado con la nueva semántica.
UPDATE detalles_venta
SET precio_lista = ROUND(precio_unitario * 1.18, 2),
    precio_unitario = ROUND(precio_unitario * 1.18, 2),
    subtotal = ROUND(subtotal * 1.18, 2);

ALTER TABLE detalles_venta
    ALTER COLUMN precio_lista SET NOT NULL;

-- Desde esta versión subtotal representa el valor de venta sin IGV y descuento es un
-- importe final con IGV incluido. El total histórico no cambia.
UPDATE ventas
SET descuento = ROUND(descuento * 1.18, 2),
    subtotal = ROUND(total / 1.18, 2),
    impuesto = total - ROUND(total / 1.18, 2);

COMMENT ON COLUMN productos.codigo_barras IS 'Código del fabricante, alternativo al SKU y al QR interno';
COMMENT ON COLUMN productos.precio_compra IS 'Precio final de compra con IGV incluido';
COMMENT ON COLUMN productos.precio_venta IS 'Precio final de venta con IGV incluido';
COMMENT ON COLUMN detalles_venta.precio_lista IS 'Precio final de lista con IGV incluido al momento de la venta';
COMMENT ON COLUMN detalles_venta.precio_unitario IS 'Precio final acordado con el cliente, con IGV incluido';
COMMENT ON COLUMN ventas.subtotal IS 'Valor de venta sin IGV después de descuentos';
COMMENT ON COLUMN ventas.impuesto IS 'IGV contenido en el precio final de la venta';
COMMENT ON COLUMN ventas.descuento IS 'Descuento adicional aplicado sobre importes finales con IGV incluido';
