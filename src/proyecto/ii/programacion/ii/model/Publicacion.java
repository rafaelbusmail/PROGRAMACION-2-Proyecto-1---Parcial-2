package proyecto.ii.programacion.ii.model;

// Cada publicacion en el feed
public class Publicacion {

    public static final int ID_MAX = 36;   //
    public static final int USERNAME_MAX = 30;
    public static final int FECHA_MAX = 10;   // dd/MM/yyyy
    public static final int HORA_MAX = 5;    // HH:mm
    public static final int CONTENIDO_MAX = 220;
    public static final int HASHTAGS_MAX = 100;
    public static final int MENCIONES_MAX = 100;
    public static final int RUTA_MAX = 300;
    public static final int TIPO_MAX = 10;   // "IMAGE", "TEXT"

    //chars (2 bytes) + int likes (4)
    public static final int RECORD_SIZE
            = (ID_MAX + USERNAME_MAX + FECHA_MAX + HORA_MAX
            + CONTENIDO_MAX + HASHTAGS_MAX + MENCIONES_MAX
            + RUTA_MAX + TIPO_MAX) * 2 + 4;

    private String id;
    private String usernameAutor;
    private String fecha;
    private String hora;
    private String contenido;
    private String hashtags;      // separados por espacio
    private String menciones;     // separados por espacio
    private String rutaImagen;
    private String tipoMultimedia; // "IMAGE" o "TEXT"
    private int likes;

    public Publicacion(String id, String usernameAutor, String fecha, String hora,
            String contenido, String hashtags, String menciones,
            String rutaImagen, String tipoMultimedia, int likes) {
        this.id = id;
        this.usernameAutor = usernameAutor;
        this.fecha = fecha;
        this.hora = hora;
        this.contenido = contenido;
        this.hashtags = hashtags;
        this.menciones = menciones;
        this.rutaImagen = rutaImagen;
        this.tipoMultimedia = tipoMultimedia;
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public String getUsernameAutor() {
        return usernameAutor;
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

    public String getHashtags() {
        return hashtags;
    }

    public String getMenciones() {
        return menciones;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public String getTipoMultimedia() {
        return tipoMultimedia;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int l) {
        this.likes = l;
    }

    public void incrementarLikes() {
        this.likes++;
    }

    public void decrementarLikes() {
        if (likes > 0) {
            likes--;
        }
    }

    public boolean tieneImagen() {
        return rutaImagen != null && !rutaImagen.trim().isEmpty();
    }

    //contenido contiene un hashtag
    public boolean contieneHashtag(String tag) {
        if (hashtags == null) {
            return false;
        }
        return hashtags.toLowerCase().contains(tag.toLowerCase());
    }

    // se menciona a un usuario en el campo menciones (busca @username)
    public boolean mencionaA(String username) {
        if (menciones == null) {
            return false;
        }
        return menciones.toLowerCase().contains("@" + username.toLowerCase());
    }

    // likes se guardan en el campo menciones con formato ~user1~user2~
    // esto no colisiona con menciones reales que usan @
    public boolean yaDioLike(String username) {
        if (menciones == null) {
            return false;
        }
        return menciones.toLowerCase().contains("~" + username.toLowerCase() + "~");
    }

    // agrega el like de un usuario al campo menciones, respetando el limite de 100 chars
    // retorna true si se pudo agregar, false si no hay espacio
    public boolean agregarLike(String username) {
        if (yaDioLike(username)) {
            return true;
        }
        String token = "~" + username + "~";
        // extraer la parte de menciones reales (sin tildes)
        String mencionesReales = getMencionesReales();
        String likesActuales = getLikesRaw();
        String nuevoLikes = likesActuales + token;
        String nuevo = nuevoLikes + mencionesReales;
        if (nuevo.length() > MENCIONES_MAX) {
            return false;
        }
        this.menciones = nuevo;
        return true;
    }

    // remueve el like de un usuario del campo menciones
    public void quitarLike(String username) {
        if (menciones == null) {
            return;
        }
        String token = "~" + username.toLowerCase() + "~";
        menciones = menciones.toLowerCase().replace(token, "~")
                .replaceAll("~+", "~").replace("~", "");
        // reconstruir correctamente preservando el resto
        String likesRaw = getLikesRaw().toLowerCase().replace(
                "~" + username.toLowerCase() + "~", "");
        this.menciones = likesRaw + getMencionesReales();
    }

    // parte del campo menciones que contiene solo los tokens ~user~
    private String getLikesRaw() {
        if (menciones == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean enTilde = false;
        StringBuilder token = new StringBuilder();
        for (char c : menciones.toCharArray()) {
            if (c == '~') {
                if (enTilde) {
                    sb.append("~").append(token).append("~");
                    token = new StringBuilder();
                    enTilde = false;
                } else {
                    enTilde = true;
                }
            } else if (enTilde) {
                token.append(c);
            }
        }
        return sb.toString();
    }

    // parte del campo menciones sin los tokens de likes
    private String getMencionesReales() {
        if (menciones == null) {
            return "";
        }
        // eliminar todo lo que este entre tildes
        return menciones.replaceAll("~[^~]*~", "").trim();
    }

    @Override
    public String toString() {
        return usernameAutor + ": " + contenido;
    }

    public void setUsernameAutor(String u) {
        this.usernameAutor = u;
    }

    public void setRutaImagen(String r) {
        this.rutaImagen = r;
    }

}
