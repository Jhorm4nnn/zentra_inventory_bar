-- =====================================================================
-- ZENTRA
-- 03: Datos de prueba
--
-- Usuarios de PRUEBA (cámbienlos antes de usar el sistema de verdad):
--   admin    / admin123     (ADMINISTRADOR)
--   vendedor / vendedor123  (VENDEDOR)
--
-- Ejecutar después de 02_vistas_y_triggers.sql. Se puede repetir:
-- no duplica usuarios, productos ni la venta de ejemplo.
-- =====================================================================

USE zentra_bar;

INSERT IGNORE INTO usuario (nombre, usuario, contrasena, rol) VALUES
    ('Administrador', 'admin',    SHA2('admin123', 256),    'ADMINISTRADOR'),
    ('Vendedor',      'vendedor', SHA2('vendedor123', 256), 'VENDEDOR');

INSERT IGNORE INTO producto (nombre, categoria, precio, stock, stock_minimo) VALUES
    ('Ron Medellín 750 ml',             'Licores',      45000, 25, 10),
    ('Aguardiente Antioqueño 750 ml',   'Licores',      38000, 18,  8),
    ('Whisky Old Parr 750 ml',          'Licores',     120000,  6,  5),
    ('Cerveza Águila lata',             'Cervezas',      3500, 60, 24),
    ('Cerveza Club Colombia lata',      'Cervezas',      4000,  5, 12),
    ('Gaseosa Coca-Cola 1.5 L',         'Mezcladores',   6500, 20,  6),
    ('Agua con gas 600 ml',             'Mezcladores',   2500, 30, 10);

-- Venta de ejemplo: solo se crea si todavía no hay ventas.
-- Los triggers descuentan el stock y calculan el total solos.
-- Los productos se leen antes en variables, porque MySQL no deja que un
-- trigger modifique "producto" mientras la misma instrucción la está leyendo.
SET @sin_ventas := (SELECT COUNT(*) = 0 FROM venta);
SELECT id_producto, precio INTO @ron, @precio_ron
FROM producto WHERE nombre = 'Ron Medellín 750 ml';
SELECT id_producto, precio INTO @cerveza, @precio_cerveza
FROM producto WHERE nombre = 'Cerveza Águila lata';

START TRANSACTION;

INSERT INTO venta (id_usuario)
SELECT id_usuario FROM usuario
WHERE usuario = 'vendedor' AND @sin_ventas;

SET @id_venta := IF(@sin_ventas, LAST_INSERT_ID(), NULL);

INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario)
SELECT @id_venta, @ron, 2, @precio_ron FROM DUAL WHERE @sin_ventas;

INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario)
SELECT @id_venta, @cerveza, 6, @precio_cerveza FROM DUAL WHERE @sin_ventas;

COMMIT;
