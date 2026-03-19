package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.model.Sticker;
import java.io.*;
import java.util.ArrayList;

/*
 *   nombre    : 50 chars * 2 = 100 bytes
 *   rutaImagen: 300 chars * 2 = 600 bytes
 *   TOTAL: 700 bytes
 */
public class StickerStorage {

    private RandomAccessFile raf;
    private final String ruta;

    // constructor por username usa la ruta de stickers del usuario
    public StickerStorage(String username) throws IOException {
        this.ruta = FileManager.getRutaStickers(username);
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(new File(ruta), "rw");
    }

    // constructor por ruta directa para el archivo de stickers globales
    public StickerStorage(File archivoDirecto) throws IOException {
        this.ruta = archivoDirecto.getPath();
        FileManager.crearArchivo(ruta);
        raf = new RandomAccessFile(archivoDirecto, "rw");
    }

    public void agregar(Sticker s) throws IOException {
        //No duplicados por nombre
        for (Sticker existente : leerTodos()) {
            if (existente.getNombre().equalsIgnoreCase(s.getNombre())) {
                return;
            }
        }
        raf.seek(raf.length());
        escribirRegistro(s);
    }

    public ArrayList<Sticker> leerTodos() throws IOException {
        ArrayList<Sticker> lista = new ArrayList<>();
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            lista.add(leerRegistro());
        }
        return lista;
    }

    public void cerrar() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void escribirRegistro(Sticker s) throws IOException {
        escribirCampo(s.getNombre(), Sticker.NOMBRE_MAX);
        escribirCampo(s.getRutaImagen(), Sticker.RUTA_MAX);
    }

    private Sticker leerRegistro() throws IOException {
        String nombre = leerCampo(Sticker.NOMBRE_MAX).trim();
        String ruta = leerCampo(Sticker.RUTA_MAX).trim();
        return new Sticker(nombre, ruta);
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
