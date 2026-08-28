ALTER TABLE ventas
    ADD COLUMN monto_recibido NUMERIC(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN vuelto NUMERIC(14,2) NOT NULL DEFAULT 0;

COMMENT ON COLUMN ventas.impuesto IS 'Monto de IGV calculado por el servidor con la tasa vigente del 18%';
COMMENT ON COLUMN ventas.monto_recibido IS 'Monto entregado por el cliente cuando el pago es en efectivo';
COMMENT ON COLUMN ventas.vuelto IS 'Vuelto calculado por el servidor para pagos en efectivo';
