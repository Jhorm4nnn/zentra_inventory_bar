package zentra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import zentra.conexion.ConexionBD;
import zentra.modelo.Producto;

/** Acceso a la tabla producto (CRUD) con JDBC. */
public class ProductoDAO {

    private static final String COLUMNAS =
            "id_producto, nombre, categoria, precio, stock, stock_minimo";

    public List<Producto> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM producto ORDER BY id_producto";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    public Optional<Producto> buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM producto WHERE id_producto = ?";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM producto WHERE nombre LIKE ? ORDER BY id_producto";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return leerLista(rs);
            }
        }
    }

    public Producto crear(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (nombre, categoria, precio, stock, stock_minimo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, p);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                llaves.next();
                p.setIdProducto(llaves.getInt(1));
            }
            return p;
        }
    }

    /** Devuelve false si no existe un producto con ese id. */
    public boolean actualizar(Producto p) throws SQLException {
        if (buscarPorId(p.getIdProducto()).isEmpty()) {
            return false;
        }
        String sql = "UPDATE producto SET nombre = ?, categoria = ?, precio = ?, stock = ?, "
                + "stock_minimo = ? WHERE id_producto = ?";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            llenar(ps, p);
            ps.setInt(6, p.getIdProducto());
            ps.executeUpdate();
            return true;
        }
    }

    /** Devuelve false si no existe un producto con ese id. */
    public boolean eliminar(int id) throws SQLException {
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement("DELETE FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void llenar(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getNombre());
        ps.setString(2, p.getCategoria());
        ps.setDouble(3, p.getPrecio());
        ps.setInt(4, p.getStock());
        ps.setInt(5, p.getStockMinimo());
    }

    private static List<Producto> leerLista(ResultSet rs) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        while (rs.next()) {
            productos.add(leer(rs));
        }
        return productos;
    }

    private static Producto leer(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getInt("stock_minimo"));
    }
}
