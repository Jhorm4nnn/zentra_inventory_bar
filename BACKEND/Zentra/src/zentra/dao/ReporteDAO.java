package zentra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import zentra.conexion.ConexionBD;
import zentra.modelo.Producto;

/** Consulta las vistas de reportes de la base de datos (RF05). */
public class ReporteDAO {

    /** Ventas de hoy: {fecha, cantidadVentas, totalVendido}. Si no hay ventas, devuelve ceros. */
    public Map<String, Object> ventasDelDia() throws SQLException {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("fecha", LocalDate.now().toString());
        r.put("cantidadVentas", 0);
        r.put("totalVendido", 0.0);
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT fecha, cantidad_ventas, total_vendido FROM v_ventas_del_dia");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                r.put("fecha", rs.getDate("fecha").toLocalDate().toString());
                r.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                r.put("totalVendido", rs.getDouble("total_vendido"));
            }
        }
        return r;
    }

    /** Productos con stock igual o menor al mínimo. */
    public List<Producto> bajoStock() throws SQLException {
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT p.id_producto, p.nombre, p.categoria, p.precio, p.stock, p.stock_minimo "
                     + "FROM v_productos_bajo_stock v JOIN producto p ON p.id_producto = v.id_producto "
                     + "ORDER BY p.stock - p.stock_minimo, p.nombre");
             ResultSet rs = ps.executeQuery()) {
            List<Producto> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Producto(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getDouble(4), rs.getInt(5), rs.getInt(6)));
            }
            return lista;
        }
    }
}
