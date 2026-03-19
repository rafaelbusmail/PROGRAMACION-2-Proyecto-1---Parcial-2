package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.model.Publicacion;
import java.io.*;
import java.util.ArrayList;

/*
 * insta ins

 *   id            : 36 chars * 2 = 72 bytes
 *   usernameAutor : 30 chars * 2 = 60 bytes
 *   fecha         : 10 chars * 2 = 20 bytes
 *   hora          :  5 chars * 2 = 10 bytes
 *   contenido     : 220 chars * 2 = 440 bytes
 *   hashtags      : 100 chars * 2 = 200 bytes
 *   menciones     : 100 chars * 2 = 200 bytes
 *   rutaImagen    : 300 chars * 2 = 600 bytes
 *   tipoMultimedia:  10 chars * 2 = 20 bytes
 *   likes         : int            = 4 bytes
 *   TOTAL: 1626 bytes
 */
public class PublicacionStorage {

    private RandomAccessFile raf;
    private final String ruta;

    public PublicacionStorage(String username) throws IOException {
        this.ruta = FileManager.getRutaInsta(username);
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(new File(ruta), "rw");
    }

    public void agregar(Publicacion p) throws IOException {
        raf.seek(raf.length());
        escribirRegistro(p);
    }

    public ArrayList<Publicacion> leerTodas() throws IOException {
        ArrayList<Publicacion> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            Publicacion p = leerRegistro();
            if (p != null) {
                lista.add(0, p); // insertar al inicio = mas reciente primero
            }
        }
        return lista;
    }

    public void eliminar(String postId) throws IOException {
        ArrayList<Publicacion> lista = leerTodas();
        lista.removeIf(p -> p.getId().equals(postId));
        raf.seek(0);
        raf.setLength(0);
        for (int i = lista.size() - 1; i >= 0; i--) {
            escribirRegistro(lista.get(i));
        }
    }

    // reescribe todo el archivo con la lista actualizada (usado al propagar username)
    public void reescribirTodas(ArrayList<Publicacion> lista) throws IOException {
        raf.seek(0);
        raf.setLength(0);
        for (Publicacion p : lista) {
            escribirRegistro(p);
        }
    }

    public void actualizarLikes(String postId, int nuevoLikes) throws IOException {
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();
            Publicacion p = leerRegistro();
            if (p != null && p.getId().equals(postId)) {
                long posLikes = pos + Publicacion.RECORD_SIZE - 4;
                raf.seek(posLikes);
                raf.writeInt(nuevoLikes);
                return;
            }
        }
    }

    // actualiza likes Y el campo menciones (donde guardamos quienes dieron like)
    // reescribe el registro completo para modificar ambos campos
    public void actualizarLikesYMenciones(String postId, int nuevoLikes,
            String nuevasMenciones) throws IOException {
        ArrayList<Publicacion> lista = leerTodas();
        for (Publicacion p : lista) {
            if (p.getId().equals(postId)) {
                p.setLikes(nuevoLikes);
                // setMenciones necesario - usar reflexion no, mejor agregar setter
                // como no hay setter para menciones, reconstruimos el post
                // usando el mismo ID y todos los campos pero con menciones nuevas
                break;
            }
        }
        // reescribir toda la lista con el post modificado
        raf.seek(0);
        raf.setLength(0);
        for (int i = 0; i < lista.size(); i++) {
            Publicacion p = lista.get(i);
            if (p.getId().equals(postId)) {
                escribirRegistroConMenciones(p, nuevoLikes, nuevasMenciones);
            } else {
                escribirRegistro(p);
            }
        }
    }

    // escribe un registro sobreescribiendo menciones y likes
    private void escribirRegistroConMenciones(Publicacion p, int likes,
            String menciones) throws IOException {
        escribirCampo(p.getId(), Publicacion.ID_MAX);
        escribirCampo(p.getUsernameAutor(), Publicacion.USERNAME_MAX);
        escribirCampo(p.getFecha(), Publicacion.FECHA_MAX);
        escribirCampo(p.getHora(), Publicacion.HORA_MAX);
        escribirCampo(p.getContenido(), Publicacion.CONTENIDO_MAX);
        escribirCampo(p.getHashtags(), Publicacion.HASHTAGS_MAX);
        escribirCampo(menciones, Publicacion.MENCIONES_MAX);
        escribirCampo(p.getRutaImagen(), Publicacion.RUTA_MAX);
        escribirCampo(p.getTipoMultimedia(), Publicacion.TIPO_MAX);
        raf.writeInt(likes);
    }

    public ArrayList<Publicacion> buscarPorHashtag(String hashtag) throws IOException {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        for (Publicacion p : leerTodas()) {
            if (p.contieneHashtag(hashtag)) {
                resultados.add(p);
            }
        }
        return resultados;
    }

    // publicaciones donde se menciona un usuario
    public ArrayList<Publicacion> buscarMenciones(String username) throws IOException {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        for (Publicacion p : leerTodas()) {
            if (p.mencionaA(username)) {
                resultados.add(p);
            }
        }
        return resultados;
    }

    public void cerrar() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void escribirRegistro(Publicacion p) throws IOException {
        escribirCampo(p.getId(), Publicacion.ID_MAX);
        escribirCampo(p.getUsernameAutor(), Publicacion.USERNAME_MAX);
        escribirCampo(p.getFecha(), Publicacion.FECHA_MAX);
        escribirCampo(p.getHora(), Publicacion.HORA_MAX);
        escribirCampo(p.getContenido(), Publicacion.CONTENIDO_MAX);
        escribirCampo(p.getHashtags(), Publicacion.HASHTAGS_MAX);
        escribirCampo(p.getMenciones(), Publicacion.MENCIONES_MAX);
        escribirCampo(p.getRutaImagen(), Publicacion.RUTA_MAX);
        escribirCampo(p.getTipoMultimedia(), Publicacion.TIPO_MAX);
        raf.writeInt(p.getLikes());
    }

    private Publicacion leerRegistro() throws IOException {
        String id = leerCampo(Publicacion.ID_MAX).trim();
        String autor = leerCampo(Publicacion.USERNAME_MAX).trim();
        String fecha = leerCampo(Publicacion.FECHA_MAX).trim();
        String hora = leerCampo(Publicacion.HORA_MAX).trim();
        String cont = leerCampo(Publicacion.CONTENIDO_MAX).trim();
        String hash = leerCampo(Publicacion.HASHTAGS_MAX).trim();
        String menc = leerCampo(Publicacion.MENCIONES_MAX).trim();
        String ruta = leerCampo(Publicacion.RUTA_MAX).trim();
        String tipo = leerCampo(Publicacion.TIPO_MAX).trim();
        int likes = raf.readInt();
        return new Publicacion(id, autor, fecha, hora, cont, hash, menc, ruta, tipo, likes);
    }

    private void escribirCampo(String valor, int maxChars) throws IOException {
        String ajustado = ajustar(valor, maxChars);
        for (char c : ajustado.toCharArray()) {
            raf.writeChar(c);
        }
    }

    private String leerCampo(int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxChars; i++) {
            sb.append(raf.readChar());
        }
        return sb.toString();
    }

    private String ajustar(String s, int max) {
        if (s == null) {
            s = "";
        }
        if (s.length() > max) {
            return s.substring(0, max);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < max) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
