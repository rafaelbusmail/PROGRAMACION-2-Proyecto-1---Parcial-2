package proyecto.ii.programacion.ii.model;

public class Comentario {

    public static final int ID_MAX = 36;
    public static final int POST_ID_MAX = 36;
    public static final int USERNAME_MAX = 30;
    public static final int CONTENIDO_MAX = 200;
    public static final int FECHA_MAX = 10;
    public static final int HORA_MAX = 5;
    // tamano fijo del registro en bytes
    public static final int RECORD_SIZE
            = (ID_MAX + POST_ID_MAX + USERNAME_MAX + CONTENIDO_MAX
            + FECHA_MAX + HORA_MAX) * 2;

    private String id;
    private String postId;
    private String username;
    private String contenido;
    private String fecha;
    private String hora;

    public Comentario(String id, String postId, String username,
            String contenido, String fecha, String hora) {
        this.id = id;
        this.postId = postId;
        this.username = username;
        this.contenido = contenido;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getContenido() {
        return contenido;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setUsername(String u) {
        this.username = u;
    }

}
