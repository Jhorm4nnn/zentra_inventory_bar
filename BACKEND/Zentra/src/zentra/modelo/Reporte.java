package zentra.modelo;

import java.time.LocalDateTime;

/** Reporte generado por un usuario: ventas del día o productos bajo stock. */
public class Reporte {

    private LocalDateTime fechaGeneracion;
    private TipoReporte tipo;

    public Reporte() {
    }

    public Reporte(TipoReporte tipo) {
        this.tipo = tipo;
    }

    /** Marca el momento en que se genera el reporte. */
    public void generar() {
        fechaGeneracion = LocalDateTime.now();
    }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public TipoReporte getTipo() { return tipo; }
    public void setTipo(TipoReporte tipo) { this.tipo = tipo; }
}
