package proyecto.ii.programacion.ii.enums;

public enum EstadoCuenta {
    ACTIVO, INACTIVO;

    @Override
    public String toString() {
        return this == ACTIVO ? "Active" : "Inactive";
    }
}
