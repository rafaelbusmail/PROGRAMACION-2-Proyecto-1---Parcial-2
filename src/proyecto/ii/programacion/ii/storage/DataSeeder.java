package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.enums.*;
import proyecto.ii.programacion.ii.model.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// sembramos datos iniciales en el primer arranque
// cuentas: rafael, fcbarcelona, primos_unitedfc
public class DataSeeder {

    private static final String[] STICKER_NOMBRES = {
        "Feliz", "Triste", "Corazon", "Risa", "Aplauso"
    };
    private static final String[] STICKER_RUTAS = {
        "stickers/sticker_feliz.png",
        "stickers/sticker_triste.png",
        "stickers/sticker_corazon.png",
        "stickers/sticker_risa.png",
        "stickers/sticker_apluso.png"
    };

    public static void sembrar() throws IOException {
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date());

        UsuarioStorage usuStorage = new UsuarioStorage();
        FollowStorage fs = new FollowStorage();

        UsuarioRegistrado u1 = new UsuarioRegistrado(
                "rafael", "1234", "Rafael Medina",
                Genero.M, 20, fecha, EstadoCuenta.ACTIVO,
                TipoCuenta.PUBLICA, "avatars/avatar_user1.png");

        UsuarioRegistrado u2 = new UsuarioRegistrado(
                "fcbarcelona", "1234", "FC Barcelona",
                Genero.M, 0, fecha, EstadoCuenta.ACTIVO,
                TipoCuenta.PUBLICA, "avatars/avatar_user3.png");

        UsuarioRegistrado u3 = new UsuarioRegistrado(
                "primos_unitedfc", "1234", "Primos United FC",
                Genero.M, 0, fecha, EstadoCuenta.ACTIVO,
                TipoCuenta.PUBLICA, "avatars/avatar_user2.png");

        if (!usuStorage.existeUsername("rafael")) {
            usuStorage.agregar(u1);
        }
        if (!usuStorage.existeUsername("fcbarcelona")) {
            usuStorage.agregar(u2);
        }
        if (!usuStorage.existeUsername("primos_unitedfc")) {
            usuStorage.agregar(u3);
        }
        usuStorage.cerrar();

        FileManager.crearEstructuraUsuario("rafael");
        FileManager.crearEstructuraUsuario("fcbarcelona");
        FileManager.crearEstructuraUsuario("primos_unitedfc");

        // post de boston (rafael)
        PublicacionStorage ps1 = new PublicacionStorage("rafael");
        if (ps1.leerTodas().isEmpty()) {
            ps1.agregar(new Publicacion(
                    "rafael_s001", "rafael", fecha, hora,
                    "Exploring Boston! Great city",
                    "#boston #travel #usa", "",
                    "posts/boston.png", "IMAGE", 0));
        }
        ps1.cerrar();

        // post Barca 5-2 (fcbarcelona)
        // el trofeo se escribe como \uD83C\uDFC6 en el .java (Java compile-time escape)
        PublicacionStorage ps2 = new PublicacionStorage("fcbarcelona");
        if (ps2.leerTodas().isEmpty()) {
            ps2.agregar(new Publicacion(
                    "fcbarca_s001", "fcbarcelona", fecha, hora,
                    "VICTORIA! 5-2 \uD83C\uDFC6 Raphinha hattrick!",
                    "#FCBarcelona #LaLiga #Barca", "",
                    "posts/post_u3_1.png", "IMAGE", 15420));
        }
        ps2.cerrar();

        // post Gran Final (primos_unitedfc)
        PublicacionStorage ps3 = new PublicacionStorage("primos_unitedfc");
        if (ps3.leerTodas().isEmpty()) {
            ps3.agregar(new Publicacion(
                    "primos_s001", "primos_unitedfc", fecha, hora,
                    "Unete a ver la GRAN FINAL de la UNITEC CUP el 19 de marzo a las 6:00 PM",
                    "#UnitecCup #GranFinal #PrimosUnited", "",
                    "posts/post_u2_1.png", "IMAGE", 0));
        }
        ps3.cerrar();

        sembrarStickers("rafael");
        sembrarStickers("fcbarcelona");
        sembrarStickers("primos_unitedfc");

        // usar ListaSimple<String[]> para construir los pares de follow entre seeds
        // y recorrer con Nodo<String[]> directamente para mostrar el uso de ambas estructuras
        ListaSimple<String[]> relaciones = new ListaSimple<>();
        relaciones.agregar(new String[]{"rafael", "fcbarcelona"});
        relaciones.agregar(new String[]{"rafael", "primos_unitedfc"});
        relaciones.agregar(new String[]{"fcbarcelona", "rafael"});
        relaciones.agregar(new String[]{"fcbarcelona", "primos_unitedfc"});
        relaciones.agregar(new String[]{"primos_unitedfc", "rafael"});
        relaciones.agregar(new String[]{"primos_unitedfc", "fcbarcelona"});

        // recorrer la lista enlazada usando Nodo directamente
        Nodo<String[]> nodo = relaciones.getCabeza();
        while (nodo != null) {
            fs.seguir(nodo.dato[0], nodo.dato[1]);
            nodo = nodo.siguiente;
        }
    }

    private static void sembrarStickers(String username) throws IOException {
        StickerStorage ss = new StickerStorage(username);
        if (ss.leerTodos().isEmpty()) {
            for (int i = 0; i < STICKER_NOMBRES.length; i++) {
                ss.agregar(new Sticker(STICKER_NOMBRES[i], STICKER_RUTAS[i]));
            }
        }
        ss.cerrar();
    }

    // siembra la biblioteca global de stickers compartida por todos los usuarios
    // se llama una sola vez al inicializar la aplicacion
    public static void sembrarStickersGlobales() throws IOException {
        File archivoGlobal = new File(FileManager.getRutaStickersGlobalesIns());
        StickerStorage sg = new StickerStorage(archivoGlobal);
        if (sg.leerTodos().isEmpty()) {
            for (int i = 0; i < STICKER_NOMBRES.length; i++) {
                sg.agregar(new Sticker(STICKER_NOMBRES[i], STICKER_RUTAS[i]));
            }
        }
        sg.cerrar();
    }
}
