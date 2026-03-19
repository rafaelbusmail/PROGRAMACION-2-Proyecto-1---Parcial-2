package proyecto.ii.programacion.ii.model;

import proyecto.ii.programacion.ii.enums.EstadoMensaje;
import proyecto.ii.programacion.ii.enums.TipoMensaje;

public class Mensaje {

    public static final int ID_MAX = 36;
    public static final int USERNAME_MAX = 30;
    public static final int FECHA_MAX = 10;
    public static final int HORA_MAX = 5;
    public static final int CONTENIDO_MAX = 300;
    public static final int TIPO_MAX = 7;    // "TEXTO" o "STICKER"
    public static final int ESTADO_MAX = 9;    // "LEIDO" o "NO_LEIDO"

    //chars * 2 bytes cada uno
    public static final int RECORD_SIZE
            = (ID_MAX + USERNAME_MAX + USERNAME_MAX + FECHA_MAX
            + HORA_MAX + CONTENIDO_MAX + TIPO_MAX + ESTADO_MAX) * 2;

    private String id;
    private String emisor;
    private String receptor;
    private String fecha;
    private String hora;
    private String contenido;
    private TipoMensaje tipo;
    private EstadoMensaje estado;

    public Mensaje(String id, String emisor, String receptor, String fecha,
            String hora, String contenido, TipoMensaje tipo, EstadoMensaje estado) {
        this.id = id;
        this.emisor = emisor;
        this.receptor = receptor;
        this.fecha = fecha;
        this.hora = hora;
        this.contenido = contenido;
        this.tipo = tipo;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getContenido() {
        return contenido;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public EstadoMensaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoMensaje e) {
        this.estado = e;
    }

    public boolean esNoLeido() {
        return estado == EstadoMensaje.NO_LEIDO;
    }

    public void setEmisor(String e) {
        this.emisor = e;
    }

    public void setReceptor(String r) {
        this.receptor = r;
    }

}
