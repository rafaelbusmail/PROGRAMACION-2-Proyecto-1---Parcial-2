package proyecto.ii.programacion.ii.model;

public class Sticker {

    public static final int NOMBRE_MAX = 50;
    public static final int RUTA_MAX = 300;
    public static final int RECORD_SIZE = (NOMBRE_MAX + RUTA_MAX) * 2;

    private String nombre;
    private String rutaImagen;

    public Sticker(String nombre, String rutaImagen) {
        this.nombre = nombre;
        this.rutaImagen = rutaImagen;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
