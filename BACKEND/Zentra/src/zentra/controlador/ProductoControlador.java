package zentra.controlador;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import zentra.dao.ProductoDAO;
import zentra.modelo.Producto;
import zentra.util.Json;

/**
 * Atiende /api/productos con las mismas direcciones y respuestas que espera
 * FRONTEND/productos.js:
 *   GET    /api/productos                 200 lista
 *   GET    /api/productos/{id}            200 o 404
 *   GET    /api/productos/buscar?nombre=  200 lista
 *   POST   /api/productos                 201, o 400 con mensajes
 *   PUT    /api/productos/{id}            200, 400 o 404
 *   DELETE /api/productos/{id}            204 o 404
 */
public class ProductoControlador implements HttpHandler {

    public static final String RUTA = "/api/productos";

    /**
     * Orígenes del frontend autorizados: páginas de este mismo equipo
     * (localhost o 127.0.0.1) en cualquier puerto, como Live Server (5500)
     * o el propio backend (8080).
     */
    private static final Pattern ORIGENES =
            Pattern.compile("http://(localhost|127\\.0\\.0\\.1)(:\\d{1,5})?");

    private final ProductoDAO dao = new ProductoDAO();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!permitirOrigen(ex)) {
                responder(ex, 403, null);
                return;
            }
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                responder(ex, 204, null);
                return;
            }
            atender(ex);
        } catch (SQLException e) {
            System.err.println("Error de base de datos (codigo " + e.getErrorCode() + "): " + e.getMessage());
            responder(ex, 500, Json.mensajes(Map.of("error", "Error en la base de datos")));
        } catch (RuntimeException e) {
            e.printStackTrace();
            responder(ex, 500, Json.mensajes(Map.of("error", "Error interno del servidor")));
        } finally {
            ex.close();
        }
    }

    private void atender(HttpExchange ex) throws IOException, SQLException {
        String metodo = ex.getRequestMethod();
        String resto = ex.getRequestURI().getPath().substring(RUTA.length());
        if (resto.endsWith("/")) {
            resto = resto.substring(0, resto.length() - 1);
        }

        if (resto.isEmpty()) {
            switch (metodo) {
                case "GET" -> responder(ex, 200, Json.productos(dao.listar()));
                case "POST" -> crear(ex);
                default -> responder(ex, 405, null);
            }
            return;
        }

        if ("/buscar".equals(resto) && "GET".equals(metodo)) {
            String nombre = parametro(ex, "nombre");
            responder(ex, 200, Json.productos(dao.buscarPorNombre(nombre == null ? "" : nombre)));
            return;
        }

        Integer id = idDeRuta(resto);
        if (id == null) {
            responder(ex, 404, null);
            return;
        }
        switch (metodo) {
            case "GET" -> {
                Optional<Producto> p = dao.buscarPorId(id);
                if (p.isPresent()) {
                    responder(ex, 200, Json.producto(p.get()));
                } else {
                    responder(ex, 404, null);
                }
            }
            case "PUT" -> actualizar(ex, id);
            case "DELETE" -> eliminar(ex, id);
            default -> responder(ex, 405, null);
        }
    }

    private void crear(HttpExchange ex) throws IOException, SQLException {
        Producto p = leerProducto(ex);
        if (p == null) {
            return; // ya se respondió 400
        }
        try {
            dao.crear(p);
            responder(ex, 201, Json.producto(p));
        } catch (SQLIntegrityConstraintViolationException e) {
            responder(ex, 400, Json.mensajes(Map.of("error", "Ya existe un producto con ese nombre")));
        }
    }

    private void actualizar(HttpExchange ex, int id) throws IOException, SQLException {
        Producto p = leerProducto(ex);
        if (p == null) {
            return;
        }
        p.setIdProducto(id);
        try {
            if (dao.actualizar(p)) {
                responder(ex, 200, Json.producto(p));
            } else {
                responder(ex, 404, null);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            responder(ex, 400, Json.mensajes(Map.of("error", "Ya existe un producto con ese nombre")));
        }
    }

    private void eliminar(HttpExchange ex, int id) throws IOException, SQLException {
        try {
            responder(ex, dao.eliminar(id) ? 204 : 404, null);
        } catch (SQLIntegrityConstraintViolationException e) {
            // La base de datos protege el historial: un producto vendido no se borra
            responder(ex, 400, Json.mensajes(Map.of("error",
                    "No se puede eliminar: el producto ya tiene ventas registradas")));
        }
    }

    /** Lee y valida el JSON del cuerpo. Si hay errores responde 400 y devuelve null. */
    private Producto leerProducto(HttpExchange ex) throws IOException {
        String tipo = ex.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.toLowerCase().startsWith("application/json")) {
            responder(ex, 415, Json.mensajes(Map.of("error", "El cuerpo debe enviarse como JSON")));
            return null;
        }
        Map<String, Object> datos;
        try {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            datos = Json.leerObjeto(cuerpo);
        } catch (IllegalArgumentException e) {
            responder(ex, 400, Json.mensajes(Map.of("error", "JSON mal escrito: " + e.getMessage())));
            return null;
        }

        Map<String, String> errores = new LinkedHashMap<>();
        Producto p = new Producto();
        p.setNombre(texto(datos.get("nombre")));
        p.setCategoria(texto(datos.get("categoria")));
        p.setPrecio(numero(datos.get("precio"), "precio", errores));
        p.setStock((int) numero(datos.get("stock"), "stock", errores));
        p.setStockMinimo((int) numero(datos.get("stockMinimo"), "stockMinimo", errores));

        if (p.getNombre() == null || p.getNombre().isBlank()) {
            errores.put("nombre", "El nombre es obligatorio");
        } else if (p.getNombre().length() > 100) {
            errores.put("nombre", "El nombre no puede tener más de 100 caracteres");
        }
        if (p.getCategoria() == null || p.getCategoria().isBlank()) {
            errores.put("categoria", "La categoría es obligatoria");
        } else if (p.getCategoria().length() > 50) {
            errores.put("categoria", "La categoría no puede tener más de 50 caracteres");
        }
        if (!errores.containsKey("precio") && p.getPrecio() <= 0) {
            errores.put("precio", "El precio debe ser mayor que cero");
        }
        if (!errores.containsKey("stock") && p.getStock() < 0) {
            errores.put("stock", "El stock no puede ser negativo");
        }
        if (!errores.containsKey("stockMinimo") && p.getStockMinimo() < 0) {
            errores.put("stockMinimo", "El stock mínimo no puede ser negativo");
        }

        if (!errores.isEmpty()) {
            responder(ex, 400, Json.mensajes(errores));
            return null;
        }
        p.setNombre(p.getNombre().trim());
        p.setCategoria(p.getCategoria().trim());
        return p;
    }

    private static String texto(Object valor) {
        return valor instanceof String s ? s : null;
    }

    private static double numero(Object valor, String campo, Map<String, String> errores) {
        if (valor == null) {
            return 0;
        }
        if (valor instanceof Double d && !d.isNaN() && !d.isInfinite()) {
            return d;
        }
        errores.put(campo, "Debe ser un número");
        return 0;
    }

    private static Integer idDeRuta(String resto) {
        try {
            return Integer.valueOf(resto.substring(1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    static String parametro(HttpExchange ex, String nombre) {
        String consulta = ex.getRequestURI().getRawQuery();
        if (consulta == null) {
            return null;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            String clave = igual < 0 ? par : par.substring(0, igual);
            if (nombre.equals(URLDecoder.decode(clave, StandardCharsets.UTF_8))) {
                return igual < 0 ? "" : URLDecoder.decode(par.substring(igual + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /** Si la petición viene de otra dirección, solo se acepta si es el frontend. */
    static boolean permitirOrigen(HttpExchange ex) {
        String origen = ex.getRequestHeaders().getFirst("Origin");
        if (origen == null) {
            return true; // Postman, curl o el navegador directo
        }
        if (!ORIGENES.matcher(origen).matches()) {
            return false;
        }
        Headers h = ex.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", origen);
        h.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
        h.set("Access-Control-Allow-Headers", "Content-Type");
        h.set("Vary", "Origin");
        return true;
    }

    static void responder(HttpExchange ex, int codigo, String json) throws IOException {
        if (json == null) {
            ex.sendResponseHeaders(codigo, -1);
            return;
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}
