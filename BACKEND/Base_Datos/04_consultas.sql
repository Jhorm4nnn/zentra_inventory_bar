-- =====================================================================
-- ZENTRA
-- 04: Consultas útiles para revisar la base de datos
-- =====================================================================

USE zentra_bar;

-- Inventario actual (RF04)
SELECT id_producto, nombre, categoria, precio, stock, stock_minimo
FROM producto
ORDER BY categoria, nombre;

-- Productos con bajo stock (RF05)
SELECT * FROM v_productos_bajo_stock;

-- Ventas del día (RF05)
SELECT * FROM v_ventas_del_dia;

-- Detalle de cada venta con el vendedor que la registró (RF03)
SELECT v.id_venta, v.fecha, u.nombre AS vendedor,
       p.nombre AS producto, d.cantidad, d.precio_unitario, d.subtotal, v.total
FROM venta v
JOIN usuario u        ON u.id_usuario  = v.id_usuario
JOIN detalle_venta d  ON d.id_venta    = v.id_venta
JOIN producto p       ON p.id_producto = d.id_producto
ORDER BY v.id_venta, d.id_detalle_venta;

-- Iniciar sesión (Usuario.iniciarSesion): devuelve el usuario si la clave es correcta
SELECT id_usuario, nombre, rol
FROM usuario
WHERE usuario = 'admin' AND contrasena = SHA2('admin123', 256);
