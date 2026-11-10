package zentra.modelo;

/** Producto que vende el estanco (licores, cervezas, mezcladores...). */
public class Producto {

    private int idProducto;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;
    private int stockMinimo;

    public Producto() {
    }

    public Producto(int idProducto, String nombre, String categoria,
                    double precio, int stock, int stockMinimo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    /** Suma la cantidad al stock (negativa para descontar una venta). */
    public void actualizarStock(int cantidad) {
        if (stock + cantidad < 0) {
            throw new IllegalArgumentException("Stock insuficiente para " + nombre);
        }
        stock += cantidad;
    }

    /** Está bajo stock si quedan igual o menos unidades que el mínimo. */
    public boolean estaBajoStock() {
        return stock <= stockMinimo;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
}
