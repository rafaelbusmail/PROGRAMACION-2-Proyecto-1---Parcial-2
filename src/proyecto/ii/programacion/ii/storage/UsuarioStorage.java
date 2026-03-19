package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.enums.*;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import java.io.*;
import java.util.ArrayList;

/*
 *   username      : 30 chars * 2 = 60 bytes
 *   password      : 30 chars * 2 = 60 bytes
 *   nombreCompleto: 60 chars * 2 = 120 bytes
 *   fechaRegistro : 10 chars * 2 = 20 bytes
 *   fotoPerfil    : 300 chars * 2 = 600 bytes
 *   edad          : int           = 4 bytes
 *   genero        : char          = 2 bytes  ('M' o 'F')
 *   tipoCuenta    : char          = 2 bytes  ('U' = publica, 'R' = privada)
 *   estado        : char          = 2 bytes  ('A' = activo, 'I' = inactivo)
 *   TOTAL: 870 bytes
 */
public class UsuarioStorage {

    private RandomAccessFile raf;
    private final String ruta;

    public UsuarioStorage() throws IOException {
        this.ruta = FileManager.USERS_FILE;
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(new File(ruta), "rw");
    }

    // ── Agregar usuario al final ──────────────────────────────────────
    public void agregar(UsuarioRegistrado u) throws IOException {
        raf.seek(raf.length());
        escribirRegistro(u);
    }

    //Leer usuarios 
    public ArrayList<UsuarioRegistrado> leerTodos() throws IOException {
        ArrayList<UsuarioRegistrado> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            UsuarioRegistrado u = leerRegistro();
            if (u != null) {
                lista.add(u);
            }
        }
        return lista;
    }

    //Buscar
    public UsuarioRegistrado buscarPorUsername(String username) throws IOException {
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();
            UsuarioRegistrado u = leerRegistro();
            if (u != null && u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    //Verificar
    public boolean existeUsername(String username) throws IOException {
        return buscarPorUsername(username) != null;
    }

    //update
    public void actualizar(UsuarioRegistrado u) throws IOException {
        raf.seek(0);
        int indice = 0;
        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();
            UsuarioRegistrado leido = leerRegistro();
            if (leido != null && leido.getUsername().equalsIgnoreCase(u.getUsername())) {
                raf.seek(pos);
                escribirRegistro(u);
                return;
            }
            indice++;
        }
    }

    // cuando el username cambia, buscar por el username anterior y sobreescribir
    public void actualizarConUsernameAnterior(String usernameAnterior,
            UsuarioRegistrado u) throws IOException {
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();
            UsuarioRegistrado leido = leerRegistro();
            if (leido != null && leido.getUsername().equalsIgnoreCase(usernameAnterior)) {
                raf.seek(pos);
                escribirRegistro(u);
                return;
            }
        }
    }

    //
    public ArrayList<UsuarioRegistrado> buscarParcial(String query) throws IOException {
        ArrayList<UsuarioRegistrado> resultados = new ArrayList<>();
        String q = query.toLowerCase();
        for (UsuarioRegistrado u : leerTodos()) {
            if (!u.isActivo()) {
                continue; // no mostrar cuentas desactivadas
            }
            if (u.getUsername().toLowerCase().contains(q)
                    || u.getNombreCompleto().toLowerCase().contains(q)) {
                resultados.add(u);
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

    //registeo fijo
    private void escribirRegistro(UsuarioRegistrado u) throws IOException {
        escribirCampo(u.getUsername(), UsuarioRegistrado.USERNAME_MAX);
        escribirCampo(u.getPassword(), UsuarioRegistrado.PASSWORD_MAX);
        escribirCampo(u.getNombreCompleto(), UsuarioRegistrado.NOMBRE_MAX);
        escribirCampo(u.getFechaRegistro(), UsuarioRegistrado.FECHA_MAX);
        escribirCampo(u.getRutaFotoPerfil(), UsuarioRegistrado.FOTO_MAX);
        raf.writeInt(u.getEdad());
        raf.writeChar(u.getGenero() == Genero.M ? 'M' : 'F');
        raf.writeChar(u.getTipoCuenta() == TipoCuenta.PUBLICA ? 'U' : 'R');
        raf.writeChar(u.getEstado() == EstadoCuenta.ACTIVO ? 'A' : 'I');
    }

    private UsuarioRegistrado leerRegistro() throws IOException {
        String username = leerCampo(UsuarioRegistrado.USERNAME_MAX).trim();
        String password = leerCampo(UsuarioRegistrado.PASSWORD_MAX).trim();
        String nombre = leerCampo(UsuarioRegistrado.NOMBRE_MAX).trim();
        String fecha = leerCampo(UsuarioRegistrado.FECHA_MAX).trim();
        String foto = leerCampo(UsuarioRegistrado.FOTO_MAX).trim();
        int edad = raf.readInt();
        char genChar = raf.readChar();
        char tipChar = raf.readChar();
        char estChar = raf.readChar();

        Genero genero = genChar == 'M' ? Genero.M : Genero.F;
        TipoCuenta tipo = tipChar == 'U' ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;
        EstadoCuenta estado = estChar == 'A' ? EstadoCuenta.ACTIVO : EstadoCuenta.INACTIVO;

        return new UsuarioRegistrado(username, password, nombre,
                genero, edad, fecha, estado, tipo, foto);
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

    /**
     * Propaga el cambio de username a TODOS los archivos del sistema. Debe
     * llamarse DESPUES de renombrar la carpeta y actualizar users.ins.
     * Actualiza: - followers.ins de cada usuario (si el viejo aparece como
     * seguidor) - following.ins de cada usuario (si el viejo aparece como
     * seguido) - inbox.ins de cada usuario (emisor o receptor con el username
     * viejo) - comentarios.ins de cada usuario (comentarios escritos por el
     * viejo)
     */
    public static void propagarCambioUsername(String usernameViejo,
            String usernameNuevo) throws IOException {
        UsuarioStorage usAll = new UsuarioStorage();
        java.util.ArrayList<UsuarioRegistrado> todos = usAll.leerTodos();
        usAll.cerrar();

        FollowStorage fs = new FollowStorage();

        for (UsuarioRegistrado u : todos) {
            String un = u.getUsername();

            // --- followers.ins: reemplazar usernameViejo por usernameNuevo ---
            String rutaFollowers = FileManager.getRutaFollowers(un);
            java.util.ArrayList<String> followers = fs.leerLista(rutaFollowers);
            boolean cambioF = false;
            for (int i = 0; i < followers.size(); i++) {
                if (followers.get(i).equalsIgnoreCase(usernameViejo)) {
                    followers.set(i, usernameNuevo);
                    cambioF = true;
                }
            }
            if (cambioF) {
                reescribirLista(rutaFollowers, followers);
            }

            // --- following.ins ---
            String rutaFollowing = FileManager.getRutaFollowing(un);
            java.util.ArrayList<String> following = fs.leerLista(rutaFollowing);
            boolean cambioFw = false;
            for (int i = 0; i < following.size(); i++) {
                if (following.get(i).equalsIgnoreCase(usernameViejo)) {
                    following.set(i, usernameNuevo);
                    cambioFw = true;
                }
            }
            if (cambioFw) {
                reescribirLista(rutaFollowing, following);
            }

            // --- inbox.ins: reemplazar emisor o receptor ---
            try {
                MensajeStorage ms = new MensajeStorage(un);
                java.util.ArrayList<proyecto.ii.programacion.ii.model.Mensaje> mensajes
                        = ms.leerTodos();
                boolean cambioM = false;
                for (proyecto.ii.programacion.ii.model.Mensaje m : mensajes) {
                    if (m.getEmisor().equalsIgnoreCase(usernameViejo)) {
                        m.setEmisor(usernameNuevo);
                        cambioM = true;
                    }
                    if (m.getReceptor().equalsIgnoreCase(usernameViejo)) {
                        m.setReceptor(usernameNuevo);
                        cambioM = true;
                    }
                }
                if (cambioM) {
                    ms.reescribir(mensajes);
                }
                ms.cerrar();
            } catch (IOException ignored) {
            }

            // --- comentarios.ins: reemplazar username del comentarista ---
            try {
                ComentarioStorage cs = new ComentarioStorage(un);
                java.util.ArrayList<proyecto.ii.programacion.ii.model.Comentario> comms
                        = cs.leerPorPost(null); // null = todos
                boolean cambioC = false;
                for (proyecto.ii.programacion.ii.model.Comentario c : comms) {
                    if (c.getUsername().equalsIgnoreCase(usernameViejo)) {
                        c.setUsername(usernameNuevo);
                        cambioC = true;
                    }
                }
                if (cambioC) {
                    cs.reescribir(comms);
                }
                cs.cerrar();
            } catch (IOException ignored) {
            }
        }

        // --- insta.ins del usuario (ya en carpeta renombrada): actualizar usernameAutor y rutaImagen ---
        try {
            PublicacionStorage ps = new PublicacionStorage(usernameNuevo);
            java.util.ArrayList<proyecto.ii.programacion.ii.model.Publicacion> posts
                    = ps.leerTodas();
            boolean cambioP = false;
            for (proyecto.ii.programacion.ii.model.Publicacion p : posts) {
                if (p.getUsernameAutor().equalsIgnoreCase(usernameViejo)) {
                    p.setUsernameAutor(usernameNuevo);
                    cambioP = true;
                }
                // actualizar ruta de imagen con ambos separadores posibles (/ y \)
                String rutaImg = p.getRutaImagen();
                if (rutaImg != null) {
                    String nueva = rutaImg;
                    if (nueva.contains("/" + usernameViejo + "/")) {
                        nueva = nueva.replace("/" + usernameViejo + "/",
                                "/" + usernameNuevo + "/");
                    } else if (nueva.contains("\\" + usernameViejo + "\\")) {
                        nueva = nueva.replace("\\" + usernameViejo + "\\",
                                "\\" + usernameNuevo + "\\");
                    }
                    if (!nueva.equals(rutaImg)) {
                        p.setRutaImagen(nueva);
                        cambioP = true;
                    }
                }
            }
            if (cambioP) {
                ps.reescribirTodas(posts);
            }
            ps.cerrar();
        } catch (IOException ignored) {
        }
    }

    // reescribe un archivo de follows con la lista actualizada
    private static void reescribirLista(String ruta,
            java.util.ArrayList<String> lista) throws IOException {
        try (java.io.RandomAccessFile raf
                = new java.io.RandomAccessFile(new java.io.File(ruta), "rw")) {
            raf.setLength(0);
            for (String u : lista) {
                String ajust = u.length() > 30 ? u.substring(0, 30) : u;
                while (ajust.length() < 30) {
                    ajust += " ";
                }
                for (char c : ajust.toCharArray()) {
                    raf.writeChar(c);
                }
            }
        }
    }

}
