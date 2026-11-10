package zentra.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexiones con la base de datos MySQL zentra_bar.
 * La contraseña NO se escribe en el código: se lee de la variable de
 * entorno DB_PASSWORD (la misma que usa el backend de Spring Boot).
 */
public final class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/zentra_bar?serverTimezone=America/Bogota";
    private static final String USUARIO = "root";

    private ConexionBD() {
    }

    public static Connection obtener() throws SQLException {
        String clave = System.getenv("DB_PASSWORD");
        if (clave == null) {
            throw new SQLException("Falta la variable de entorno DB_PASSWORD con la clave de MySQL");
        }
        return DriverManager.getConnection(URL, USUARIO, clave);
    }

    /** Prueba la conexión al arrancar, para avisar pronto si algo falla. */
    public static void probar() throws SQLException {
        try (Connection conexion = obtener()) {
            conexion.isValid(2);
        }
    }
}
