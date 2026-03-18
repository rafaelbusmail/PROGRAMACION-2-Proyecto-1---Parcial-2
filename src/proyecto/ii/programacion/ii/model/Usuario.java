package proyecto.ii.programacion.ii.model;

import proyecto.ii.programacion.ii.enums.EstadoCuenta;
import proyecto.ii.programacion.ii.enums.Genero;
import proyecto.ii.programacion.ii.enums.TipoCuenta;

// clase abstracta 
public abstract class Usuario {

    protected String username;
    protected String password;
    protected String nombreCompleto;
    protected Genero genero;
    protected int edad;
    protected String fechaRegistro;   // "dd/MM/yyyy"
    protected EstadoCuenta estado;
    protected TipoCuenta tipoCuenta;
    protected String rutaFotoPerfil;

    public Usuario(String username, String password, String nombreCompleto,
            Genero genero, int edad, String fechaRegistro,
            EstadoCuenta estado, TipoCuenta tipoCuenta, String rutaFotoPerfil) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.edad = edad;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.tipoCuenta = tipoCuenta;
        this.rutaFotoPerfil = rutaFotoPerfil;
    }

    public abstract String getResumen();

    //Getter
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Genero getGenero() {
        return genero;
    }

    public int getEdad() {
        return edad;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public String getRutaFotoPerfil() {
        return rutaFotoPerfil;
    }

    // Setter
    public void setPassword(String p) {
        this.password = p;
    }

    public void setNombreCompleto(String n) {
        this.nombreCompleto = n;
    }

    public void setEstado(EstadoCuenta e) {
        this.estado = e;
    }

    public void setTipoCuenta(TipoCuenta t) {
        this.tipoCuenta = t;
    }

    public void setRutaFotoPerfil(String ruta) {
        this.rutaFotoPerfil = ruta;
    }

    public boolean isActivo() {
        return estado == EstadoCuenta.ACTIVO;
    }

    public boolean isPublico() {
        return tipoCuenta == TipoCuenta.PUBLICA;
    }

    @Override
    public String toString() {
        return username;
    }
}
