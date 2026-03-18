package proyecto.ii.programacion.ii.storage;

import java.io.*;
import java.util.ArrayList;

// cada registro es un username de tamano fijo (60 bytes)
public class FollowStorage {

    private static final int USERNAME_MAX = 30;

    // lee lista de usernames desde un archivo
    public ArrayList<String> leerLista(String rutaArchivo) throws IOException {
        ArrayList<String> lista = new ArrayList<>();
        File f = new File(rutaArchivo);
        if (!f.exists() || f.length() == 0) {
            return lista;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            while (raf.getFilePointer() < raf.length()) {
                lista.add(leerCampo(raf, USERNAME_MAX).trim());
            }
        }
        return lista;
    }

    // agrega username a la lista sin duplicados
    public void agregar(String rutaArchivo, String username) throws IOException {
        if (existe(rutaArchivo, username)) {
            return;
        }
        FileManager.crearArchivo(rutaArchivo);
        try (RandomAccessFile raf = new RandomAccessFile(new File(rutaArchivo), "rw")) {
            raf.seek(raf.length());
            escribirCampo(raf, username, USERNAME_MAX);
        }
    }

    // elimina username de la lista reescribiendo sin el
    public void eliminar(String rutaArchivo, String username) throws IOException {
        ArrayList<String> lista = leerLista(rutaArchivo);
        lista.removeIf(u -> u.equalsIgnoreCase(username));
        try (RandomAccessFile raf = new RandomAccessFile(new File(rutaArchivo), "rw")) {
            raf.setLength(0);
            for (String u : lista) {
                escribirCampo(raf, u, USERNAME_MAX);
            }
        }
    }

    // true si el username esta en la lista
    public boolean existe(String rutaArchivo, String username) throws IOException {
        for (String u : leerLista(rutaArchivo)) {
            if (u.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    // A sigue a B, B follow de A y A follower+
    public void seguir(String usernameA, String usernameB) throws IOException {
        agregar(FileManager.getRutaFollowing(usernameA), usernameB);
        agregar(FileManager.getRutaFollowers(usernameB), usernameA);
    }

    // A deja de seguir a B
    public void dejarDeSeguir(String usernameA, String usernameB) throws IOException {
        eliminar(FileManager.getRutaFollowing(usernameA), usernameB);
        eliminar(FileManager.getRutaFollowers(usernameB), usernameA);
    }

    // true si A sigue a B
    public boolean sigueA(String usernameA, String usernameB) throws IOException {
        return existe(FileManager.getRutaFollowing(usernameA), usernameB);
    }

    public int contarFollowers(String username) throws IOException {
        return leerLista(FileManager.getRutaFollowers(username)).size();
    }

    public int contarFollowing(String username) throws IOException {
        return leerLista(FileManager.getRutaFollowing(username)).size();
    }

    private void escribirCampo(RandomAccessFile raf, String valor, int max)
            throws IOException {
        String s = ajustar(valor, max);
        for (char c : s.toCharArray()) {
            raf.writeChar(c);
        }
    }

    private String leerCampo(RandomAccessFile raf, int max) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
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
