package zentra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import zentra.conexion.ConexionBD;

/**
 * Registra ventas en MySQL. Los triggers de la base validan el stock,
 * lo descuentan y calculan el total de la venta.
 */
public class VentaDAO {

    /** Error que el usuario debe ver tal cual (producto inexistente, sin stock...). */
    public static class VentaRechazada extends Exception {
        public VentaRechazada(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Registra una venta de un producto. Mientras no exista el inicio de sesión,
     * la venta se asigna al primer usuario con rol VENDEDOR.
     * Devuelve {idVenta, total}.
     */
    public Map<String, Object> registrar(int idProducto, int cantidad)
            throws SQLException, VentaRechazada {
        try (Connection c = ConexionBD.obtener()) {
            c.setAutoCommit(false);
            try {
                int idUsuario = vendedorPorDefecto(c);
                double precio = precioDe(c, idProducto);

                int idVenta;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO venta (id_usuario) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idUsuario);
                    ps.executeUpdate();
                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        llaves.next();
                        idVenta = llaves.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario) "
                        + "VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, idVenta);
                    ps.setInt(2, idProducto);
                    ps.setInt(3, cantidad);
                    ps.setDouble(4, precio);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    if ("45000".equals(e.getSQLState())) { // SIGNAL del trigger de stock
                        throw new VentaRechazada("Stock insuficiente para este producto");
                    }
                    throw e;
                }

                double total;
                try (PreparedStatement ps = c.prepareStatement("SELECT total FROM venta WHERE id_venta = ?")) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getDouble(1);
                    }
                }
                c.commit();

                Map<String, Object> resultado = new LinkedHashMap<>();
                resultado.put("idVenta", idVenta);
                resultado.put("total", total);
                return resultado;
            } catch (SQLException | VentaRechazada | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Últimas líneas vendidas, de la más nueva a la más antigua. */
    public List<Map<String, Object>> recientes(int limite) throws SQLException {
        String sql = "SELECT v.id_venta, v.fecha, p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM detalle_venta d "
                + "JOIN venta v ON v.id_venta = d.id_venta "
                + "JOIN producto p ON p.id_producto = d.id_producto "
                + "ORDER BY v.fecha DESC, d.id_detalle_venta DESC LIMIT ?";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> lista = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("idVenta", rs.getInt("id_venta"));
                    fila.put("fecha", rs.getTimestamp("fecha").toLocalDateTime().toString());
                    fila.put("producto", rs.getString("nombre"));
                    fila.put("cantidad", rs.getInt("cantidad"));
                    fila.put("precioUnitario", rs.getDouble("precio_unitario"));
                    fila.put("subtotal", rs.getDouble("subtotal"));
                    lista.add(fila);
                }
                return lista;
            }
        }
    }

    private static int vendedorPorDefecto(Connection c) throws SQLException, VentaRechazada {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_usuario FROM usuario WHERE rol = 'VENDEDOR' ORDER BY id_usuario LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new VentaRechazada("No hay ningún usuario con rol VENDEDOR registrado");
            }
            return rs.getInt(1);
        }
    }

    private static double precioDe(Connection c, int idProducto) throws SQLException, VentaRechazada {
        try (PreparedStatement ps = c.prepareStatement("SELECT precio FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new VentaRechazada("El producto no existe");
                }
                return rs.getDouble(1);
            }
        }
    }
}
