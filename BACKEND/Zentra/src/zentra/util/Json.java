package zentra.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import zentra.modelo.Producto;

/**
 * Conversión mínima entre JSON y Java, sin librerías externas.
 * Lee objetos planos como {"nombre":"Ron","precio":45000} y escribe productos.
 */
public final class Json {

    private final String texto;
    private int pos;

    private Json(String texto) {
        this.texto = texto;
    }

    // ------------------------------------------------------------ escribir

    public static String producto(Producto p) {
        return "{\"id\":" + p.getIdProducto()
                + ",\"nombre\":" + cadena(p.getNombre())
                + ",\"categoria\":" + cadena(p.getCategoria())
                + ",\"precio\":" + p.getPrecio()
                + ",\"stock\":" + p.getStock()
                + ",\"stockMinimo\":" + p.getStockMinimo() + "}";
    }

    public static String productos(List<Producto> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(producto(lista.get(i)));
        }
        return sb.append(']').toString();
    }

    /** Escribe un objeto {"clave":"mensaje", ...} para los errores. */
    public static String mensajes(Map<String, String> mensajes) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> e : mensajes.entrySet()) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(cadena(e.getKey())).append(':').append(cadena(e.getValue()));
        }
        return sb.append('}').toString();
    }

    public static String cadena(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    // ------------------------------------------------------------ leer

    /** Lee un objeto JSON plano. Lanza IllegalArgumentException si está mal escrito. */
    public static Map<String, Object> leerObjeto(String texto) {
        Json lector = new Json(texto == null ? "" : texto);
        Map<String, Object> objeto = lector.objeto();
        lector.espacios();
        if (lector.pos != lector.texto.length()) {
            throw new IllegalArgumentException("Hay texto de más después del JSON");
        }
        return objeto;
    }

    private Map<String, Object> objeto() {
        espacios();
        esperar('{');
        Map<String, Object> mapa = new LinkedHashMap<>();
        espacios();
        if (mirar() == '}') {
            pos++;
            return mapa;
        }
        while (true) {
            espacios();
            String clave = cadenaJson();
            espacios();
            esperar(':');
            espacios();
            mapa.put(clave, valor());
            espacios();
            char c = siguiente();
            if (c == '}') {
                return mapa;
            }
            if (c != ',') {
                throw new IllegalArgumentException("Se esperaba ',' o '}'");
            }
        }
    }

    private Object valor() {
        char c = mirar();
        if (c == '"') {
            return cadenaJson();
        }
        if (texto.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (texto.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        if (texto.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        int inicio = pos;
        while (pos < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(pos)) >= 0) {
            pos++;
        }
        if (inicio == pos) {
            throw new IllegalArgumentException("Valor no válido");
        }
        return Double.valueOf(texto.substring(inicio, pos));
    }

    private String cadenaJson() {
        esperar('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = siguiente();
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = siguiente();
            switch (e) {
                case '"', '\\', '/' -> sb.append(e);
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    if (pos + 4 > texto.length()) {
                        throw new IllegalArgumentException("Escape \\u incompleto");
                    }
                    sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> throw new IllegalArgumentException("Escape no válido");
            }
        }
    }

    private void espacios() {
        while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) {
            pos++;
        }
    }

    private char mirar() {
        if (pos >= texto.length()) {
            throw new IllegalArgumentException("El JSON terminó antes de tiempo");
        }
        return texto.charAt(pos);
    }

    private char siguiente() {
        char c = mirar();
        pos++;
        return c;
    }

    private void esperar(char c) {
        if (siguiente() != c) {
            throw new IllegalArgumentException("Se esperaba '" + c + "'");
        }
    }
}
