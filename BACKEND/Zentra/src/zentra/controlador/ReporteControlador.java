package zentra.controlador;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import zentra.dao.ReporteDAO;
import zentra.util.Json;

/**
 * Atiende /api/reportes (usado por FRONTEND/reportes.js):
 *   GET /api/reportes/ventas-del-dia  {"fecha":..,"cantidadVentas":..,"totalVendido":..}
 *   GET /api/reportes/bajo-stock      lista de productos con stock bajo
 */
public class ReporteControlador implements HttpHandler {

    public static final String RUTA = "/api/reportes";

    private final ReporteDAO dao = new ReporteDAO();

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
            } else if (!"GET".equals(metodo)) {
                ProductoControlador.responder(ex, 405, null);
            } else if ("/ventas-del-dia".equals(resto)) {
                ProductoControlador.responder(ex, 200, Json.objeto(dao.ventasDelDia()));
            } else if ("/bajo-stock".equals(resto)) {
                ProductoControlador.responder(ex, 200, Json.productos(dao.bajoStock()));
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
}
