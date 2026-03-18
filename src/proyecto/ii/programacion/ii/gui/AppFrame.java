package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.enums.ModoVista;
import proyecto.ii.programacion.ii.gui.componentes.NavBar;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.DataSeeder;
import proyecto.ii.programacion.ii.storage.FileManager;
import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {

    // singleton
    private static AppFrame instancia;

    public static AppFrame getInstance() {
        if (instancia == null) {
            instancia = new AppFrame();
        }
        return instancia;
    }

    // modo visual segun reglamento seccion 3 - cambiar aqui para alternar
    public static final ModoVista MODO = ModoVista.MOBILE;

    // dimensiones derivadas del modo (no hardcodeadas)
    public static final int ANCHO = MODO.getAncho();
    public static final int ALTO = MODO.getAlto();

    // nombres de pantallas
    public static final String PANTALLA_LOGIN = "LOGIN";
    public static final String PANTALLA_REGISTRO = "REGISTRO";
    public static final String PANTALLA_FEED = "FEED";
    public static final String PANTALLA_PERFIL = "PERFIL";
    public static final String PANTALLA_PERFIL_AJENO = "PERFIL_AJENO";
    public static final String PANTALLA_BUSCAR = "BUSCAR";
    public static final String PANTALLA_INBOX = "INBOX";
    public static final String PANTALLA_CHAT = "CHAT";
    public static final String PANTALLA_PUBLICAR = "PUBLICAR";

    // estado de sesion
    private UsuarioRegistrado usuarioSesion;
    // username del perfil ajeno que se esta viendo
    private String usernameViendoPerfil;
    // pantalla desde donde se navego al perfil ajeno para el boton back
    private String pantallaAnteriorPerfil = PANTALLA_BUSCAR;

    // componentes
    private CardLayout cardLayout;
    private JPanel panelPantallas;
    private NavBar navBar;

    // singleton (constructor privado)
    private AppFrame() {
        setTitle("Instagram");
        setSize(ANCHO, ALTO);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // panel de pantallas con el CardLayout
        cardLayout = new CardLayout();
        panelPantallas = new JPanel(cardLayout);
        panelPantallas.setBackground(Color.WHITE);

        // NavBar
        navBar = new NavBar();
        navBar.setListener(seccion -> manejarNav(seccion));

        add(panelPantallas, BorderLayout.CENTER);
        add(navBar, BorderLayout.SOUTH);

        // Centrar en pantalla
        setLocationRelativeTo(null);
    }

    // inicializacion
    public void iniciar() {
        try {
            // iniciar servidor de notificaciones en tiempo real
            proyecto.ii.programacion.ii.storage.ChatServer.getInstance().iniciar();
            if (!FileManager.existeEstructura()) {
                FileManager.inicializarEstructura();
                DataSeeder.sembrar();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error initializing data: " + e.getMessage());
        }

        // Registrar todas las pantallas
        registrarPantallas();

        // Ocultar navbar hasta hacer login
        navBar.setVisible(false);

        // Mostrar login
        mostrarPantalla(PANTALLA_LOGIN);
        setVisible(true);
    }

    // registro de pantallas
    private void registrarPantallas() {
        JPanel login = new PantallaLogin();
        login.setName(PANTALLA_LOGIN);
        panelPantallas.add(login, PANTALLA_LOGIN);

        JPanel registro = new PantallaRegistro();
        registro.setName(PANTALLA_REGISTRO);
        panelPantallas.add(registro, PANTALLA_REGISTRO);
    }

    // registra las pantallas que necesitan sesion activa
    public void registrarPantallasConSesion() {
        JPanel feed = new PantallaFeed();
        feed.setName(PANTALLA_FEED);
        JPanel perfil = new PantallaPerfil();
        perfil.setName(PANTALLA_PERFIL);
        JPanel ajeno = new PantallaPerfilAjeno();
        ajeno.setName(PANTALLA_PERFIL_AJENO);
        JPanel buscar = new PantallaBuscar();
        buscar.setName(PANTALLA_BUSCAR);
        JPanel inbox = new PantallaInbox();
        inbox.setName(PANTALLA_INBOX);
        JPanel chat = new PantallaChat();
        chat.setName(PANTALLA_CHAT);
        JPanel publicar = new PantallaPublicar();
        publicar.setName(PANTALLA_PUBLICAR);

        panelPantallas.add(feed, PANTALLA_FEED);
        panelPantallas.add(perfil, PANTALLA_PERFIL);
        panelPantallas.add(ajeno, PANTALLA_PERFIL_AJENO);
        panelPantallas.add(buscar, PANTALLA_BUSCAR);
        panelPantallas.add(inbox, PANTALLA_INBOX);
        panelPantallas.add(chat, PANTALLA_CHAT);
        panelPantallas.add(publicar, PANTALLA_PUBLICAR);
    }

    // navegamos entre las pantallas
    public void mostrarPantalla(String nombre) {
        cardLayout.show(panelPantallas, nombre);

        // Actualizar navbar activo
        switch (nombre) {
            case PANTALLA_FEED:
                navBar.setSeccionActiva(NavBar.HOME);
                break;
            case PANTALLA_BUSCAR:
                navBar.setSeccionActiva(NavBar.SEARCH);
                break;
            case PANTALLA_PUBLICAR:
                navBar.setSeccionActiva(NavBar.ADD);
                break;
            case PANTALLA_INBOX:
            case PANTALLA_CHAT:
                navBar.setSeccionActiva(NavBar.INBOX);
                break;
            case PANTALLA_PERFIL:
                navBar.setSeccionActiva(NavBar.PROFILE);
                break;
        }

        // Refrescar la pantalla si implementa Refrescable
        Component actual = getPantallaActual(nombre);
        if (actual instanceof Refrescable) {
            ((Refrescable) actual).refrescar();
        }
    }

    // handler del navbar
    private void manejarNav(String seccion) {
        switch (seccion) {
            case NavBar.HOME:
                mostrarPantalla(PANTALLA_FEED);
                break;
            case NavBar.SEARCH:
                mostrarPantalla(PANTALLA_BUSCAR);
                break;
            case NavBar.ADD:
                mostrarPantalla(PANTALLA_PUBLICAR);
                break;
            case NavBar.INBOX:
                mostrarPantalla(PANTALLA_INBOX);
                break;
            case NavBar.PROFILE:
                mostrarPantalla(PANTALLA_PERFIL);
                break;
        }
    }

    // sesion
    public void iniciarSesion(UsuarioRegistrado u) {
        this.usuarioSesion = u;
        // conectar cliente socket para recibir notificaciones en tiempo real
        chatClient = new proyecto.ii.programacion.ii.storage.ChatClient(
                u.getUsername(), () -> {
            navBar.setBadgeInbox(true);
            // si el chat esta visible, recargar mensajes al instante
            Component actual = getPantallaActual(PANTALLA_CHAT);
            if (actual instanceof PantallaChat && actual.isVisible()) {
                ((PantallaChat) actual).refrescarMensajes();
            }
            // si inbox esta visible, refrescarlo
            Component inboxComp = getPantallaActual(PANTALLA_INBOX);
            if (inboxComp instanceof PantallaInbox && inboxComp.isVisible()) {
                ((Refrescable) inboxComp).refrescar();
            }
        });
        // cuando el receptor lee nuestros mensajes, refrescar inbox para mostrar "Seen"
        chatClient.setOnMensajeLeido(() -> {
            Component inboxComp = getPantallaActual(PANTALLA_INBOX);
            if (inboxComp instanceof PantallaInbox && inboxComp.isVisible()) {
                ((Refrescable) inboxComp).refrescar();
            }
        });
        chatClient.conectar();
        navBar.setVisible(true);
        registrarPantallasConSesion();
        mostrarPantalla(PANTALLA_FEED);
    }

    public void cerrarSesion() {
        if (chatClient != null) {
            chatClient.desconectar();
            chatClient = null;
        }
        this.usuarioSesion = null;
        this.usernameChat = null;
        this.usernameViendoPerfil = null;
        this.pantallaAnteriorPerfil = PANTALLA_BUSCAR;
        navBar.setVisible(false);
        navBar.setBadgeInbox(false);
        panelPantallas.removeAll();
        registrarPantallas();
        mostrarPantalla(PANTALLA_LOGIN);
    }

    public UsuarioRegistrado getSesion() {
        return usuarioSesion;
    }

    // cliente socket para notificaciones en tiempo real
    private proyecto.ii.programacion.ii.storage.ChatClient chatClient;

    public proyecto.ii.programacion.ii.storage.ChatClient getChatClient() {
        return chatClient;
    }

    // perfil ajeno
    public void verPerfilAjeno(String username) {
        // guardar desde donde venimos para que el back funcione correctamente
        for (Component c : panelPantallas.getComponents()) {
            if (c.isVisible()) {
                if (c instanceof PantallaFeed) {
                    pantallaAnteriorPerfil = PANTALLA_FEED;
                } else if (c instanceof PantallaBuscar) {
                    pantallaAnteriorPerfil = PANTALLA_BUSCAR;
                } else if (c instanceof PantallaPerfil) {
                    pantallaAnteriorPerfil = PANTALLA_PERFIL;
                } else {
                    pantallaAnteriorPerfil = PANTALLA_BUSCAR;
                }
                break;
            }
        }
        this.usernameViendoPerfil = username;
        mostrarPantalla(PANTALLA_PERFIL_AJENO);
    }

    public String getPantallaAnteriorPerfil() {
        return pantallaAnteriorPerfil;
    }

    public String getUsernameViendoPerfil() {
        return usernameViendoPerfil;
    }

    // chat
    private String usernameChat;
    private String pantallaAnteriorChat = PANTALLA_INBOX;

    public void abrirChat(String username) {
        this.usernameChat = username;
        // recordar desde que pantalla se abrio el chat para el boton back
        for (Component c : panelPantallas.getComponents()) {
            if (c.isVisible()) {
                // si estaba en perfil ajeno, el back vuelve ahi
                if (c instanceof PantallaPerfilAjeno) {
                    pantallaAnteriorChat = PANTALLA_PERFIL_AJENO;
                } else {
                    pantallaAnteriorChat = PANTALLA_INBOX;
                }
                break;
            }
        }
        mostrarPantalla(PANTALLA_CHAT);
    }

    public String getUsernameChat() {
        return usernameChat;
    }

    public String getPantallaAnteriorChat() {
        return pantallaAnteriorChat;
    }

    // badge de inbox
    public void setBadgeInbox(boolean visible) {
        navBar.setBadgeInbox(visible);
    }

    // helper para cardlayout
    private Component getPantallaActual(String nombre) {
        for (Component c : panelPantallas.getComponents()) {
            if (c.isVisible()) {
                return c;
            }
        }
        return null;
    }

    // retorna la pantalla con ese nombre (visible o no)
    public Component getPantallaPorNombre(String nombre) {
        for (Component c : panelPantallas.getComponents()) {
            if (nombre.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    // Las pantallas que necesitan actualizar datos al mostrarse la implementan
    public interface Refrescable {

        void refrescar();
    }
}
