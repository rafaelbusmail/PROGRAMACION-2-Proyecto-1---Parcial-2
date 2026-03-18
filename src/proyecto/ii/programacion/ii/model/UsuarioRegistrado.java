package proyecto.ii.programacion.ii.model;

import proyecto.ii.programacion.ii.enums.*;
import proyecto.ii.programacion.ii.interfaces.Mensajeable;
import proyecto.ii.programacion.ii.interfaces.Publicable;
import java.util.ArrayList;

// Clase concreta
public class UsuarioRegistrado extends Usuario implements Publicable, Mensajeable {

    // Campos RandomAccessFile
    public static final int USERNAME_MAX = 30;
    public static final int PASSWORD_MAX = 30;
    public static final int NOMBRE_MAX = 60;
    public static final int FECHA_MAX = 10;
    public static final int FOTO_MAX = 300;

    // int edad (4) + char genero (2) + char tipoCuenta (2) + char estado (2) = 10 bytes
    public static final int RECORD_SIZE
            = (USERNAME_MAX + PASSWORD_MAX + NOMBRE_MAX + FECHA_MAX + FOTO_MAX) * 2 + 10;

    private ArrayList<Publicacion> publicaciones;
    private ArrayList<String> followers;    // usernames que me siguen
    private ArrayList<String> following;    // usernames que sigo

    public UsuarioRegistrado(String username, String password, String nombreCompleto,
            Genero genero, int edad, String fechaRegistro,
            EstadoCuenta estado, TipoCuenta tipoCuenta,
            String rutaFotoPerfil) {
        super(username, password, nombreCompleto, genero, edad,
                fechaRegistro, estado, tipoCuenta, rutaFotoPerfil);
        this.publicaciones = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    @Override
    public String getResumen() {
        return "@" + username + " · " + publicaciones.size() + " posts · "
                + followers.size() + " followers · " + following.size() + " following";
    }

    @Override
    public void publicar(Publicacion p) {
        publicaciones.add(0, p); // más reciente primero
    }

    @Override
    public void eliminarPost(String postId) {
        publicaciones.removeIf(p -> p.getId().equals(postId));
    }

    @Override
    public ArrayList<Publicacion> getMisPublicaciones() {
        return publicaciones;
    }

    @Override
    public void enviarMensaje(Mensaje m) {
    }

    @Override
    public ArrayList<Mensaje> getMensajes(String otroUsername) {

        return new ArrayList<>();

    }

    // Followers / Following 
    public void agregarFollower(String username) {
        if (!followers.contains(username)) {
            followers.add(username);
        }
    }

    public void quitarFollower(String username) {
        followers.remove(username);
    }

    public void agregarFollowing(String username) {
        if (!following.contains(username)) {
            following.add(username);
        }
    }

    public void quitarFollowing(String username) {
        following.remove(username);
    }

    public boolean sigueA(String username) {
        return following.contains(username);
    }

    public boolean esSeguridoPor(String username) {
        return followers.contains(username);
    }

    // Amistad = seguimiento mutuo
    public boolean esAmigoDe(String username) {
        return sigueA(username) && esSeguridoPor(username);
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public int getNumFollowers() {
        return followers.size();
    }

    public int getNumFollowing() {
        return following.size();
    }

    public int getNumPosts() {
        return publicaciones.size();
    }

    public void setFollowers(ArrayList<String> f) {
        this.followers = f;
    }

    public void setFollowing(ArrayList<String> f) {
        this.following = f;
    }

    public void setPublicaciones(ArrayList<Publicacion> p) {
        this.publicaciones = p;
    }
}
