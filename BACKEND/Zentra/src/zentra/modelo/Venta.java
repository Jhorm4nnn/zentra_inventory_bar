package zentra.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Venta registrada por un usuario. Contiene uno o más detalles. */
public class Venta {

    private int idVenta;
    private LocalDateTime fecha;
    private double total;
    private Usuario usuario;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
        this.fecha = LocalDateTime.now();
    }

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        calcularTotal();
    }

    public double calcularTotal() {
        total = 0;
        for (DetalleVenta detalle : detalles) {
            total += detalle.calcularSubtotal();
        }
        return total;
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DetalleVenta> getDetalles() { return detalles; }
}
