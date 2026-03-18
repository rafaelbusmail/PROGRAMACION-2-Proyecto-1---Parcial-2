package proyecto.ii.programacion.ii.enums;

public enum ModoVista {
    MOBILE, DESKTOP;

    // Dimensiones según modo
    public int getAncho() {
        return this == MOBILE ? 390 : 1366;
    }

    public int getAlto() {
        return this == MOBILE ? 844 : 768;
    }
}
