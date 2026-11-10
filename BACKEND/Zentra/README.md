# Zentra: backend para NetBeans (Java with Ant)

Proyecto **Java with Ant → Java Application** de NetBeans. Expone la API de productos en `http://localhost:8080/api/productos` sobre la base MySQL `zentra_bar`, y es compatible con `FRONTEND/productos.html` sin cambiarle nada.

## Estructura (MVC)

| Paquete | Contenido |
|---|---|
| `zentra` | `Zentra.java`, la clase principal que enciende el servidor |
| `zentra.modelo` | Las clases del diagrama: `Usuario`, `RolUsuario`, `Producto`, `Venta`, `DetalleVenta`, `Reporte`, `TipoReporte` |
| `zentra.conexion` | `ConexionBD`: conexión JDBC con MySQL |
| `zentra.dao` | `ProductoDAO`, `VentaDAO` y `ReporteDAO`: consultas SQL |
| `zentra.controlador` | `ProductoControlador`, `VentaControlador` y `ReporteControlador` atienden la API; `FrontendControlador` sirve las páginas |
| `zentra.util` | `Json`: convierte entre JSON y objetos Java |

No usa Maven ni descargas: solo el JDK y el driver de MySQL, que ya está en `lib/`.

## Antes de ejecutar

1. Ejecuta los scripts de `../Base_Datos` en MySQL, en orden.
2. Crea la variable de entorno `DB_PASSWORD` con la clave de `root` y **reinicia NetBeans** para que la lea:

   ```bash
   setx DB_PASSWORD "tu_contraseña"
   ```

## Abrir y ejecutar en NetBeans

1. *File → Open Project* y elige la carpeta `BACKEND/Zentra`. Aparece con el icono de taza de café.
2. Pulsa **Run Project** (F6). En la ventana *Output* debe salir `Abre la pagina en: http://localhost:8080/productos.html`.
3. Abre esa dirección en el navegador. El backend sirve también las páginas de `FRONTEND`, así que **no hace falta Live Server**.
4. Para detenerlo, pulsa el cuadrado rojo de la ventana *Output*.

El backend encuentra el frontend porque busca `../../FRONTEND`, así que abre el proyecto desde el repositorio clonado (`zentra_inventory_bar/BACKEND/Zentra`), no desde una copia suelta. Si la copia está en otro lugar, en *Properties → Run → VM Options* pon `-Dzentra.frontend=RUTA_A_FRONTEND`.

También puedes seguir usando Live Server: la API acepta páginas de `localhost` o `127.0.0.1` en cualquier puerto. Lo que **no** funciona es abrir el HTML con doble clic (`file:///`): el navegador lo bloquea y la página dice «¿Está encendida la API?».

El backend de Spring Boot y este proyecto usan el mismo puerto 8080: enciende solo uno a la vez.

## Endpoints

| Método | Dirección | Respuesta |
|---|---|---|
| GET | `/api/productos` | 200 y la lista |
| GET | `/api/productos/{id}` | 200, o 404 si no existe |
| GET | `/api/productos/buscar?nombre=texto` | 200 y la lista |
| POST | `/api/productos` | 201, o 400 con el campo que falló |
| PUT | `/api/productos/{id}` | 200, 400 o 404 |
| DELETE | `/api/productos/{id}` | 204, 404, o 400 si el producto ya tiene ventas |
| POST | `/api/ventas` con `{"idProducto":1,"cantidad":2}` | 201 y `{idVenta, total}`, o 400 (sin stock, producto inexistente) |
| GET | `/api/ventas/recientes` | 200 y las últimas 10 líneas vendidas |
| GET | `/api/reportes/ventas-del-dia` | 200 y `{fecha, cantidadVentas, totalVendido}` |
| GET | `/api/reportes/bajo-stock` | 200 y los productos con stock igual o menor al mínimo |

## Páginas del frontend que usa

| Página | Script | Qué hace |
|---|---|---|
| `productos.html` | `productos.js` | CRUD de productos |
| `inventario.html` | `inventario.js` | Muestra el stock y su estado, y lo actualiza |
| `ventas.html` | `ventas.js` | Registra ventas (el precio y el total se calculan solos) y lista las recientes |
| `reportes.html` | `reportes.js` | Ventas del día y productos con bajo stock |

Ventas y reportes solo existen en este backend, no en el de Spring Boot. Mientras no haya inicio de sesión, cada venta se registra a nombre del primer usuario con rol VENDEDOR.
