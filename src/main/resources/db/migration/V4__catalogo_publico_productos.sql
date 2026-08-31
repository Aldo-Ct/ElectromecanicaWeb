ALTER TABLE productos
    ADD COLUMN publicado_venta BOOLEAN NOT NULL DEFAULT FALSE;

-- La carga existente queda visible para conservar el catálogo actual. Desde esta
-- migración cada producto puede ocultarse de la web sin desactivarlo del inventario.
UPDATE productos
SET publicado_venta = TRUE
WHERE activo = TRUE;

CREATE INDEX idx_producto_publicacion
    ON productos (publicado_venta, activo);

COMMENT ON COLUMN productos.publicado_venta IS
    'Indica si el producto activo puede mostrarse en el catálogo público';
