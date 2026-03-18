package proyecto.ii.programacion.ii.enums;

public enum TipoCuenta {
    PUBLICA, PRIVADA;

    @Override
    public String toString() {
        return this == PUBLICA ? "Public" : "Private";
    }
}
