package zentra;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import zentra.conexion.ConexionBD;
import java.nio.file.Path;
import zentra.controlador.FrontendControlador;
import zentra.controlador.ProductoControlador;
import zentra.controlador.ReporteControlador;
import zentra.controlador.VentaControlador;

/**
 * Punto de arranque de ZENTRA (proyecto NetBeans: Java with Ant).
 * Enciende una API REST en http://localhost:8080 que usa la base MySQL
 * zentra_bar y que consume el frontend (productos.html).
 */
public class Zentra {

    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {
        // Los mensajes de consola van sin tildes para que se lean bien en cualquier consola
        System.out.println("ZENTRA - Sistema de Gestion de Comandas e Inventario");

        try {
            ConexionBD.probar();
            System.out.println("Conectado a MySQL (base zentra_bar).");
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a MySQL: " + e.getMessage());
            System.err.println("Revisa que MySQL este encendido, que se hayan ejecutado los scripts "
                    + "de BACKEND/Base_Datos y que exista la variable DB_PASSWORD.");
            System.exit(1);
        }

        HttpServer servidor;
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", PUERTO), 0);
        } catch (BindException e) {
            System.err.println("El puerto " + PUERTO + " ya esta en uso. Deten la otra aplicacion "
                    + "(por ejemplo el backend de Spring Boot) y vuelve a ejecutar.");
            System.exit(1);
            return;
        }
        servidor.createContext(ProductoControlador.RUTA, new ProductoControlador());
        servidor.createContext(VentaControlador.RUTA, new VentaControlador());
        servidor.createContext(ReporteControlador.RUTA, new ReporteControlador());

        // Si encuentra la carpeta FRONTEND, también sirve las páginas
        Path frontend = FrontendControlador.buscarCarpeta();
        if (frontend != null) {
            servidor.createContext("/", new FrontendControlador(frontend));
        }

        servidor.setExecutor(Executors.newFixedThreadPool(4));
        servidor.start();

        System.out.println("API lista en http://localhost:" + PUERTO + "/api");
        System.out.println("  " + ProductoControlador.RUTA + "  " + VentaControlador.RUTA
                + "  " + ReporteControlador.RUTA);
        if (frontend != null) {
            System.out.println("Abre la pagina en: http://localhost:" + PUERTO + "/productos.html");
            System.out.println("  (frontend servido desde " + frontend + ")");
        } else {
            System.out.println("No se encontro la carpeta FRONTEND: abre las paginas con Live Server.");
        }
        System.out.println("Para detenerla, pulsa el boton rojo de NetBeans o Ctrl + C.");
    }
}
