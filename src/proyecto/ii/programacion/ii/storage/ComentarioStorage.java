package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.model.Comentario;
import java.io.*;
import java.util.ArrayList;

public class ComentarioStorage {

    private RandomAccessFile raf;
    private final String ruta;

    public ComentarioStorage(String usernameAutorPost) throws IOException {
        this.ruta = FileManager.getRutaComentarios(usernameAutorPost);
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(new File(ruta), "rw");
    }

    public void agregar(Comentario c) throws IOException {
        raf.seek(raf.length());
        escribir(c);
    }

    // todos los comentarios de una publicacion especifica
    public ArrayList<Comentario> leerPorPost(String postId) throws IOException {
        ArrayList<Comentario> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            Comentario c = leerRegistro();
            if (c != null && c.getPostId().equals(postId)) {
                lista.add(c);
            }
        }
        return lista;
    }

    // total de comentarios en un post
    public int contarPorPost(String postId) throws IOException {
        return leerPorPost(postId).size();
    }

    public void cerrar() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void escribir(Comentario c) throws IOException {
        escribirCampo(c.getId(), Comentario.ID_MAX);
        escribirCampo(c.getPostId(), Comentario.POST_ID_MAX);
        escribirCampo(c.getUsername(), Comentario.USERNAME_MAX);
        escribirCampo(c.getContenido(), Comentario.CONTENIDO_MAX);
        escribirCampo(c.getFecha(), Comentario.FECHA_MAX);
        escribirCampo(c.getHora(), Comentario.HORA_MAX);
    }

    private Comentario leerRegistro() throws IOException {
        if (raf.getFilePointer() + Comentario.RECORD_SIZE > raf.length()) {
            return null;
        }
        String id = leerCampo(Comentario.ID_MAX).trim();
        String postId = leerCampo(Comentario.POST_ID_MAX).trim();
        String username = leerCampo(Comentario.USERNAME_MAX).trim();
        String contenido = leerCampo(Comentario.CONTENIDO_MAX).trim();
        String fecha = leerCampo(Comentario.FECHA_MAX).trim();
        String hora = leerCampo(Comentario.HORA_MAX).trim();
        if (id.isEmpty()) {
            return null;
        }
        return new Comentario(id, postId, username, contenido, fecha, hora);
    }

    private void escribirCampo(String valor, int maxChars) throws IOException {
        if (valor == null) {
            valor = "";
        }
        if (valor.length() > maxChars) {
            valor = valor.substring(0, maxChars);
        }
        StringBuilder sb = new StringBuilder(valor);
        while (sb.length() < maxChars) {
            sb.append(' ');
        }
        for (char ch : sb.toString().toCharArray()) {
            raf.writeChar(ch);
        }
    }

    private String leerCampo(int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxChars; i++) {
            sb.append(raf.readChar());
        }
        return sb.toString();
    }
}
