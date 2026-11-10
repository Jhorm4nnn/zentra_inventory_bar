package zentra.controlador;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import zentra.dao.VentaDAO;
import zentra.util.Json;

/**
 * Atiende /api/ventas (usado por FRONTEND/ventas.js):
 *   POST /api/ventas            {"idProducto":1,"cantidad":2}  201, 400
 *   GET  /api/ventas/recientes  200 lista de las últimas líneas vendidas
 */
public class VentaControlador implements HttpHandler {

    public static final String RUTA = "/api/ventas";

    private final VentaDAO dao = new VentaDAO();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!ProductoControlador.permitirOrigen(ex)) {
                ProductoControlador.responder(ex, 403, null);
                return;
            }
            String metodo = ex.getRequestMethod();
            String resto = ex.getRequestURI().getPath().substring(RUTA.length()).replaceAll("/$", "");
            if ("OPTIONS".equals(metodo)) {
                ProductoControlador.responder(ex, 204, null);
            } else if (resto.isEmpty() && "POST".equals(metodo)) {
                registrar(ex);
            } else if ("/recientes".equals(resto) && "GET".equals(metodo)) {
                ProductoControlador.responder(ex, 200, Json.objetos(dao.recientes(10)));
            } else {
                ProductoControlador.responder(ex, 404, null);
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos (codigo " + e.getErrorCode() + "): " + e.getMessage());
            ProductoControlador.responder(ex, 500, Json.mensajes(Map.of("error", "Error en la base de datos")));
        } catch (RuntimeException e) {
            e.printStackTrace();
            ProductoControlador.responder(ex, 500, Json.mensajes(Map.of("error", "Error interno del servidor")));
        } finally {
            ex.close();
        }
    }

    private void registrar(HttpExchange ex) throws IOException, SQLException {
        String tipo = ex.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.toLowerCase().startsWith("application/json")) {
            ProductoControlador.responder(ex, 415, Json.mensajes(Map.of("error", "El cuerpo debe enviarse como JSON")));
            return;
        }
        Map<String, Object> datos;
        try {
            datos = Json.leerObjeto(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            ProductoControlador.responder(ex, 400, Json.mensajes(Map.of("error", "JSON mal escrito: " + e.getMessage())));
            return;
        }

        Map<String, String> errores = new LinkedHashMap<>();
        Integer idProducto = entero(datos.get("idProducto"));
        Integer cantidad = entero(datos.get("cantidad"));
        if (idProducto == null || idProducto <= 0) {
            errores.put("producto", "Elige un producto de la lista");
        }
        if (cantidad == null || cantidad <= 0) {
            errores.put("cantidad", "La cantidad debe ser un número entero mayor que cero");
        }
        if (!errores.isEmpty()) {
            ProductoControlador.responder(ex, 400, Json.mensajes(errores));
            return;
        }

        try {
            ProductoControlador.responder(ex, 201, Json.objeto(dao.registrar(idProducto, cantidad)));
        } catch (VentaDAO.VentaRechazada e) {
            ProductoControlador.responder(ex, 400, Json.mensajes(Map.of("error", e.getMessage())));
        }
    }

    /** Devuelve el número si es entero; null si falta o tiene decimales. */
    private static Integer entero(Object valor) {
        if (valor instanceof Double d && d == Math.rint(d) && Math.abs(d) < Integer.MAX_VALUE) {
            return d.intValue();
        }
        return null;
    }
}
