package zentra.modelo;

/** Persona que usa el sistema: vendedor o administrador. */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String usuario;
    private String contrasena; // hash SHA-256, nunca la clave en texto plano
    private RolUsuario rol;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
}
