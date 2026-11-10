# Base_Datos: base de datos MySQL de ZENTRA

Base `zentra_bar`, creada a partir del diagrama de clases de la Entrega 3.

| Clase del diagrama | Tabla o regla en MySQL |
|---|---|
| `Usuario` + `RolUsuario` | `usuario` (el rol es un `ENUM`; la contraseña se guarda como hash SHA-256) |
| `Producto` | `producto` |
| `Venta` | `venta` (cada venta pertenece a un usuario) |
| `DetalleVenta` | `detalle_venta` (si se borra la venta, se borran sus detalles) |
| `Reporte` + `TipoReporte` | `reporte` (el tipo es un `ENUM`) |
| `Producto.estaBajoStock()` | vista `v_productos_bajo_stock` |
| `TipoReporte.VENTAS_DEL_DIA` | vista `v_ventas_del_dia` |
| `Producto.actualizarStock()` | trigger que descuenta el stock al vender y bloquea la venta si no alcanza |
| `Venta.calcularTotal()` y `DetalleVenta.calcularSubtotal()` | trigger que recalcula el total y columna `subtotal` calculada |

## Archivos, en orden

| Archivo | Qué hace |
|---|---|
| `01_crear_base_datos.sql` | Crea la base `zentra_bar` y sus cinco tablas |
| `02_vistas_y_triggers.sql` | Crea las vistas de reportes y los triggers de inventario |
| `03_datos_prueba.sql` | Carga dos usuarios, siete productos y una venta de ejemplo |
| `04_consultas.sql` | Consultas para revisar inventario, ventas e inicio de sesión |

Los tres primeros se pueden ejecutar varias veces: no borran datos ni duplican los de prueba.

## Cómo ejecutarlos

**En MySQL Workbench:** abre cada archivo con *File → Open SQL Script*, en orden, y pulsa el rayo (⚡) para ejecutarlo completo.

**En la consola**, desde esta carpeta:

```bash
mysql -u root -p < 01_crear_base_datos.sql
mysql -u root -p < 02_vistas_y_triggers.sql
mysql -u root -p < 03_datos_prueba.sql
```

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMINISTRADOR |
| `vendedor` | `vendedor123` | VENDEDOR |

Son solo para practicar: cámbienlos antes de usar el sistema con datos reales.

> Esta base (`zentra_bar`) es distinta de `zentradb`, la que crea sola el backend de Spring Boot, para que los dos no se mezclen.
