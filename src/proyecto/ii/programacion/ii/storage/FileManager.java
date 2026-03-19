package proyecto.ii.programacion.ii.storage;

import java.io.*;

/*
 * INSTA_RAIZ/
 *   users.ins
 *   stickers_globales/
 *   USERNAME/
 *     insta.ins
 *     followers.ins
 *     following.ins
 *     inbox.ins
 *     stickers.ins
 *     imagenes/
 *     folders_personales/
 *     stickers_personales/
 */
public class FileManager {

    public static final String RAIZ = "INSTA_RAIZ";
    public static final String USERS_FILE = RAIZ + "/users.ins";
    public static final String STICKERS_GLOBALES = RAIZ + "/stickers_globales";

    // Crear toda la estructura raíz si no existe
    public static void inicializarEstructura() throws IOException {
        crearCarpeta(RAIZ);
        crearCarpeta(STICKERS_GLOBALES);
        crearArchivo(USERS_FILE);
        // stickers_globales.ins: biblioteca compartida de stickers para todos los usuarios
        crearArchivo(STICKERS_GLOBALES + "/stickers_globales.ins");
    }

    // crear carpeta de usuario con todos sus archivos y subcarpetas
    public static void crearEstructuraUsuario(String username) throws IOException {
        String base = RAIZ + "/" + username;
        crearCarpeta(base);
        crearCarpeta(base + "/imagenes");
        crearCarpeta(base + "/folders_personales");
        crearCarpeta(base + "/stickers_personales");
        crearArchivo(base + "/insta.ins");
        crearArchivo(base + "/followers.ins");
        crearArchivo(base + "/following.ins");
        crearArchivo(base + "/inbox.ins");
        crearArchivo(base + "/stickers.ins");
        crearArchivo(base + "/comentarios.ins");
        crearArchivo(base + "/pending_followers.ins");
    }

    // helpers
    public static void crearCarpeta(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public static void crearArchivo(String ruta) throws IOException {
        File f = new File(ruta);
        if (!f.exists()) {
            // Asegura que el directorio padre exista
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
    }

    public static boolean existeEstructura() {
        return new File(RAIZ).exists() && new File(USERS_FILE).exists();
    }

    // Rutas de archivos por usuario
    public static String getRutaInsta(String username) {
        return RAIZ + "/" + username + "/insta.ins";
    }

    public static String getRutaFollowers(String username) {
        return RAIZ + "/" + username + "/followers.ins";
    }

    public static String getRutaFollowing(String username) {
        return RAIZ + "/" + username + "/following.ins";
    }

    public static String getRutaInbox(String username) {
        return RAIZ + "/" + username + "/inbox.ins";
    }

    public static String getRutaStickers(String username) {
        return RAIZ + "/" + username + "/stickers.ins";
    }

    // comentarios de las publicaciones del usuario
    public static String getRutaComentarios(String username) {
        return RAIZ + "/" + username + "/comentarios.ins";
    }

    // solicitudes de follow pendientes para cuentas privadas
    public static String getRutaPendingFollowers(String username) {
        return RAIZ + "/" + username + "/pending_followers.ins";
    }

    public static String getRutaImagenes(String username) {
        return RAIZ + "/" + username + "/imagenes";
    }

    public static String getRutaStickersPersonales(String username) {
        return RAIZ + "/" + username + "/stickers_personales";
    }

    // stickers compartidos disponibles para todos los usuarios
    public static String getRutaStickersGlobalesIns() {
        return STICKERS_GLOBALES + "/stickers_globales.ins";
    }

    // renombra la carpeta del usuario cuando se cambia el username
    // retorna true si el renombrado fue exitoso o no era necesario
    public static boolean renombrarCarpetaUsuario(String usernameAnterior,
            String usernameNuevo) {
        File carpetaVieja = new File(RAIZ + "/" + usernameAnterior);
        File carpetaNueva = new File(RAIZ + "/" + usernameNuevo);
        if (!carpetaVieja.exists()) {
            return true;  // nada que renombrar
        }
        if (carpetaNueva.exists()) {
            return false;   // destino ya ocupado
        }
        return carpetaVieja.renameTo(carpetaNueva);
    }
}
