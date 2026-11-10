-- =====================================================================
-- ZENTRA
-- 02: Vistas y triggers (reglas del diagrama de clases)
--
--   Producto.estaBajoStock()      -> vista v_productos_bajo_stock
--   TipoReporte.VENTAS_DEL_DIA    -> vista v_ventas_del_dia
--   Producto.actualizarStock()    -> trigger: descuenta stock al vender
--   Venta.calcularTotal()         -> trigger: recalcula el total
--
-- Ejecutar después de 01_crear_base_datos.sql. Se puede repetir.
-- =====================================================================

USE zentra_bar;

-- Productos con stock igual o menor al mínimo (RF05)
CREATE OR REPLACE VIEW v_productos_bajo_stock AS
SELECT id_producto, nombre, categoria, stock, stock_minimo
FROM producto
WHERE stock <= stock_minimo;

-- Resumen de las ventas de hoy (RF05)
CREATE OR REPLACE VIEW v_ventas_del_dia AS
SELECT DATE(fecha)          AS fecha,
       COUNT(*)             AS cantidad_ventas,
       COALESCE(SUM(total), 0) AS total_vendido
FROM venta
WHERE DATE(fecha) = CURDATE()
GROUP BY DATE(fecha);

DROP TRIGGER IF EXISTS tr_detalle_validar_stock;
DROP TRIGGER IF EXISTS tr_detalle_descontar_stock;

DELIMITER $$

-- Antes de vender: no se puede vender más de lo que hay en stock (RF04)
CREATE TRIGGER tr_detalle_validar_stock
BEFORE INSERT ON detalle_venta
FOR EACH ROW
BEGIN
    DECLARE disponible INT;
    SELECT stock INTO disponible FROM producto
    WHERE id_producto = NEW.id_producto
    FOR UPDATE;
    IF disponible < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente para el producto';
    END IF;
END$$

-- Después de vender: descuenta el stock y recalcula el total (RF03, RF04)
CREATE TRIGGER tr_detalle_descontar_stock
AFTER INSERT ON detalle_venta
FOR EACH ROW
BEGIN
    UPDATE producto
    SET stock = stock - NEW.cantidad
    WHERE id_producto = NEW.id_producto;

    UPDATE venta
    SET total = (SELECT COALESCE(SUM(subtotal), 0)
                 FROM detalle_venta
                 WHERE id_venta = NEW.id_venta)
    WHERE id_venta = NEW.id_venta;
END$$

DELIMITER ;
