package zentra.controlador;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Sirve las páginas de la carpeta FRONTEND en http://localhost:8080/
 * Así la página y la API están en la misma dirección: no hace falta
 * Live Server y el navegador no bloquea las peticiones (CORS).
 */
public class FrontendControlador implements HttpHandler {

    private static final Map<String, String> TIPOS = Map.of(
            "html", "text/html; charset=UTF-8",
            "css", "text/css; charset=UTF-8",
            "js", "text/javascript; charset=UTF-8",
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "svg", "image/svg+xml",
            "ico", "image/x-icon");

    private final Path carpeta;

    public FrontendControlador(Path carpeta) {
        this.carpeta = carpeta.toAbsolutePath().normalize();
    }

    /**
     * Busca la carpeta del frontend. Primero la propiedad -Dzentra.frontend,
     * luego la estructura del repositorio (BACKEND/Zentra -> ../../FRONTEND).
     * Devuelve null si no la encuentra.
     */
    public static Path buscarCarpeta() {
        String propiedad = System.getProperty("zentra.frontend");
        List<String> candidatas = propiedad != null
                ? List.of(propiedad)
                : List.of("../../FRONTEND", "../FRONTEND", "FRONTEND");
        for (String ruta : candidatas) {
            Path p = Path.of(ruta);
            if (Files.isRegularFile(p.resolve("productos.html"))) {
                return p.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    public Path getCarpeta() {
        return carpeta;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod()) && !"HEAD".equals(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }
            String ruta = ex.getRequestURI().getPath();
            if (ruta.equals("/")) {
                ruta = "/index.html";
            }
            Path archivo = carpeta.resolve(ruta.substring(1)).normalize();

            // Nunca se sirve nada fuera de la carpeta del frontend
            if (!archivo.startsWith(carpeta) || !Files.isRegularFile(archivo)) {
                byte[] texto = "Pagina no encontrada".getBytes();
                ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                ex.sendResponseHeaders(404, texto.length);
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(texto);
                }
                return;
            }

            String nombre = archivo.getFileName().toString();
            String extension = nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
            ex.getResponseHeaders().set("Content-Type",
                    TIPOS.getOrDefault(extension, "application/octet-stream"));
            ex.getResponseHeaders().set("Cache-Control", "no-cache");
            byte[] contenido = Files.readAllBytes(archivo);
            if ("HEAD".equals(ex.getRequestMethod())) {
                ex.sendResponseHeaders(200, -1);
                return;
            }
            ex.sendResponseHeaders(200, contenido.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(contenido);
            }
        } finally {
            ex.close();
        }
    }
}
