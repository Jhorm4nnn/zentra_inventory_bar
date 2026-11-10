# Zentra: backend para NetBeans (Java with Ant)

Proyecto **Java with Ant → Java Application** de NetBeans. Expone la API de productos en `http://localhost:8080/api/productos` sobre la base MySQL `zentra_bar`, y es compatible con `FRONTEND/productos.html` sin cambiarle nada.

## Estructura (MVC)

| Paquete | Contenido |
|---|---|
| `zentra` | `Zentra.java`, la clase principal que enciende el servidor |
| `zentra.modelo` | Las clases del diagrama: `Usuario`, `RolUsuario`, `Producto`, `Venta`, `DetalleVenta`, `Reporte`, `TipoReporte` |
| `zentra.conexion` | `ConexionBD`: conexión JDBC con MySQL |
| `zentra.dao` | `ProductoDAO`: CRUD de productos con SQL |
| `zentra.controlador` | `ProductoControlador`: atiende las peticiones HTTP del frontend |
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
2. Pulsa **Run Project** (F6). En la ventana *Output* debe salir `API lista en http://localhost:8080/api/productos`.
3. Abre `FRONTEND/productos.html` con Live Server (puerto 5500) y verás los productos de MySQL.
4. Para detenerlo, pulsa el cuadrado rojo de la ventana *Output*.

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
