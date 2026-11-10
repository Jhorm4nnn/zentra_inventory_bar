# ZENTRA

Sistema de gestión de ventas e inventario para el Estanco Dejavu. Este proyecto incluye el CRUD de productos: una API REST con Spring Boot y una página web que la consume.

| Carpeta | Contenido |
|---|---|
| `BACKEND/` | API REST con Spring Boot 4.1.2 y Java 21 |
| `BACKEND/Zentra/` | Backend para NetBeans (Java with Ant); ver su README |
| `BACKEND/Base_Datos/` | Scripts MySQL de la base `zentra_bar`, según el diagrama de clases |
| `FRONTEND/` | Páginas HTML y CSS; `productos.html` ya usa la API |
| `DATABASE/` | Script SQL de referencia |
| `TESTING/` | Colección de Postman |
| `DOCUMENTACION/` | Entregas y capturas de Postman |

## Cómo ejecutar el backend

Necesitas JDK 21 o superior. En PowerShell, desde `BACKEND/`:

```bash
.\mvnw.cmd spring-boot:run
```

Espera a ver `Started ZentraApplication`. Para detenerlo, pulsa `Ctrl + C`. Por defecto usa H2 en memoria, así que los datos se borran al apagarlo.

Para ver la tabla, abre `http://localhost:8080/h2-console` con estos datos:

- JDBC URL: `jdbc:h2:mem:zentradb`
- Usuario: `sa`
- Contraseña: vacía

## Cómo abrir el frontend

Abre `FRONTEND/` en VS Code y usa Live Server en `productos.html`. La API solo acepta el puerto 5500: `http://127.0.0.1:5500` o `http://localhost:5500`. Si usas otro, agrégalo en `BACKEND/src/main/java/com/proyecto/zentra/config/CorsConfig.java`.

## Endpoints

| Método | Dirección | Respuesta correcta | Errores |
|---|---|---|---|
| GET | `/api/productos` | 200 y la lista | |
| GET | `/api/productos/{id}` | 200 y el producto | 404 |
| GET | `/api/productos/buscar?nombre=texto` | 200 y la lista | |
| POST | `/api/productos` | 201 y el producto creado | 400 |
| PUT | `/api/productos/{id}` | 200 y el producto actualizado | 400, 404 |
| DELETE | `/api/productos/{id}` | 204 sin contenido | 404 |

## Pruebas con Postman

1. En Postman, pulsa **Import** y elige `TESTING/API-Productos.postman_collection.json`.
2. Enciende el backend con H2, que es la opción por defecto. Debe estar recién iniciado, para que el primer producto tenga el id 1. En MySQL los números no se reutilizan, así que allí debes cambiar el 1 por el id que devuelva la petición de crear.
3. Ejecuta las peticiones en orden.
4. Guarda las capturas en `DOCUMENTACION/capturas/`.

## Cambiar a MySQL

1. Si aún no lo tienes, instala MySQL Community Server y MySQL Workbench.
2. Crea la variable con tu contraseña en CMD y vuelve a abrir la terminal:

   ```bash
   setx DB_PASSWORD "tu_contraseña"
   ```

3. Desde `BACKEND/`, arranca con el perfil `mysql`:

   ```bash
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```

La base `zentradb` y la tabla `producto` se crean solas. La contraseña nunca se escribe en el repositorio.
