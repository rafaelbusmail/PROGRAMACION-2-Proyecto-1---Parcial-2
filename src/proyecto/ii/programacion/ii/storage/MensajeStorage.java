package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.enums.EstadoMensaje;
import proyecto.ii.programacion.ii.enums.TipoMensaje;
import proyecto.ii.programacion.ii.model.Mensaje;
import java.io.*;
import java.util.ArrayList;

/*
 *   id        : 36 chars * 2 = 72  bytes
 *   emisor    : 30 chars * 2 = 60  bytes
 *   receptor  : 30 chars * 2 = 60  bytes
 *   fecha     : 10 chars * 2 = 20  bytes
 *   hora      :  5 chars * 2 = 10  bytes
 *   contenido : 300 chars* 2 = 600 bytes
 *   tipo      :  7 chars * 2 = 14  bytes  ("TEXTO" o "STICKER")
 *   estado    :  9 chars * 2 = 18  bytes  ("LEIDO" o "NO_LEIDO")
 *   TOTAL: 854 bytes
 */
public class MensajeStorage {

    private RandomAccessFile raf;
    private final String ruta;
    private final String username; //dueño

    public MensajeStorage(String username) throws IOException {
        this.username = username;
        this.ruta = FileManager.getRutaInbox(username);
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(new File(ruta), "rw");
    }

    public void guardar(Mensaje m) throws IOException {
        raf.seek(raf.length());
        escribirRegistro(m);
    }

    public ArrayList<Mensaje> leerTodos() throws IOException {
        ArrayList<Mensaje> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            Mensaje m = leerRegistro();
            if (m != null) {
                lista.add(m);
            }
        }
        return lista;
    }

    public ArrayList<Mensaje> leerConversacion(String otroUsername) throws IOException {
        ArrayList<Mensaje> conv = new ArrayList<>();
        for (Mensaje m : leerTodos()) {
            if (m.getEmisor().equalsIgnoreCase(otroUsername)
                    || m.getReceptor().equalsIgnoreCase(otroUsername)) {
                conv.add(m);
            }
        }
        return conv;
    }

    public ArrayList<String> leerConversacionesUnicas() throws IOException {
        ArrayList<String> convs = new ArrayList<>();
        for (Mensaje m : leerTodos()) {
            String otro = m.getEmisor().equalsIgnoreCase(username)
                    ? m.getReceptor() : m.getEmisor();
            if (!convs.contains(otro)) {
                convs.add(otro);
            }
        }
        return convs;
    }

    public int contarNoLeidos() throws IOException {
        int count = 0;
        for (Mensaje m : leerTodos()) {
            if (m.getReceptor().equalsIgnoreCase(username) && m.esNoLeido()) {
                count++;
            }
        }
        return count;
    }

    public void marcarLeidos(String otroUsername) throws IOException {
        ArrayList<Mensaje> todos = leerTodos();
        boolean cambio = false;
        for (Mensaje m : todos) {
            if (m.getEmisor().equalsIgnoreCase(otroUsername)
                    && m.getReceptor().equalsIgnoreCase(username)
                    && m.esNoLeido()) {
                m.setEstado(EstadoMensaje.LEIDO);
                cambio = true;
            }
        }
        if (cambio) {
            reescribir(todos);
            // para que cuando el emisor abra su inbox vea que fue leido
            try {
                MensajeStorage msEmisor = new MensajeStorage(otroUsername);
                ArrayList<Mensaje> todosEmisor = msEmisor.leerTodos();
                boolean cambioEmisor = false;
                for (Mensaje m : todosEmisor) {
                    if (m.getEmisor().equalsIgnoreCase(otroUsername)
                            && m.getReceptor().equalsIgnoreCase(username)
                            && m.esNoLeido()) {
                        m.setEstado(EstadoMensaje.LEIDO);
                        cambioEmisor = true;
                    }
                }
                if (cambioEmisor) {
                    msEmisor.reescribir(todosEmisor);
                }
                msEmisor.cerrar();
            } catch (IOException ignored) {
            }
        }
    }

    public void eliminarConversacion(String otroUsername) throws IOException {
        ArrayList<Mensaje> todos = leerTodos();
        todos.removeIf(m
                -> m.getEmisor().equalsIgnoreCase(otroUsername)
                || m.getReceptor().equalsIgnoreCase(otroUsername)
        );
        reescribir(todos);
    }

    // package-private para que marcarLeidos pueda usarla en la instancia del emisor
    void reescribir(ArrayList<Mensaje> lista) throws IOException {
        raf.seek(0);
        raf.setLength(0);
        for (Mensaje m : lista) {
            escribirRegistro(m);
        }
    }

    public void cerrar() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void escribirRegistro(Mensaje m) throws IOException {
        escribirCampo(m.getId(), Mensaje.ID_MAX);
        escribirCampo(m.getEmisor(), Mensaje.USERNAME_MAX);
        escribirCampo(m.getReceptor(), Mensaje.USERNAME_MAX);
        escribirCampo(m.getFecha(), Mensaje.FECHA_MAX);
        escribirCampo(m.getHora(), Mensaje.HORA_MAX);
        escribirCampo(m.getContenido(), Mensaje.CONTENIDO_MAX);
        escribirCampo(m.getTipo().name(), Mensaje.TIPO_MAX);
        escribirCampo(m.getEstado().name(), Mensaje.ESTADO_MAX);
    }

    private Mensaje leerRegistro() throws IOException {
        String id = leerCampo(Mensaje.ID_MAX).trim();
        String emisor = leerCampo(Mensaje.USERNAME_MAX).trim();
        String receptor = leerCampo(Mensaje.USERNAME_MAX).trim();
        String fecha = leerCampo(Mensaje.FECHA_MAX).trim();
        String hora = leerCampo(Mensaje.HORA_MAX).trim();
        String cont = leerCampo(Mensaje.CONTENIDO_MAX).trim();
        String tipoStr = leerCampo(Mensaje.TIPO_MAX).trim();
        String estStr = leerCampo(Mensaje.ESTADO_MAX).trim();

        TipoMensaje tipo = tipoStr.equals("STICKER")
                ? TipoMensaje.STICKER : TipoMensaje.TEXTO;
        EstadoMensaje estado = estStr.equals("LEIDO")
                ? EstadoMensaje.LEIDO : EstadoMensaje.NO_LEIDO;

        return new Mensaje(id, emisor, receptor, fecha, hora, cont, tipo, estado);
    }

    //enviar mensaje
    public static void enviar(Mensaje m) throws IOException {
        //guardar inbox emisor
        MensajeStorage msEmisor = new MensajeStorage(m.getEmisor());
        msEmisor.guardar(m);
        msEmisor.cerrar();

        //guardar inbox receptor
        if (!m.getEmisor().equalsIgnoreCase(m.getReceptor())) {
            MensajeStorage msReceptor = new MensajeStorage(m.getReceptor());
            msReceptor.guardar(m);
            msReceptor.cerrar();
        }
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
