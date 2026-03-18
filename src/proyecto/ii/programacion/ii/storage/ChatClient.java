package proyecto.ii.programacion.ii.storage;

import java.io.*;
import java.net.*;

// cliente socket para recibir y enviar notificaciones de mensajes en tiempo real
public class ChatClient {

    private static final String HOST = "localhost";

    private Socket socket;
    private PrintWriter out;
    private String username;
    private Runnable onNuevoMensaje;
    private Runnable onMensajeLeido;
    private boolean conectado = false;
    private boolean activo = true;

    public ChatClient(String username, Runnable onNuevoMensaje) {
        this.username = username;
        this.onNuevoMensaje = onNuevoMensaje;
    }

    public void setOnMensajeLeido(Runnable callback) {
        this.onMensajeLeido = callback;
    }

    // conectar en hilo daemon; reintenta cada 2s si falla
    public void conectar() {
        Thread t = new Thread(() -> {
            while (activo && !conectado) {
                try {
                    socket = new Socket(HOST, ChatServer.PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    out.println(username);
                    conectado = true;
                    String line;
                    while ((line = in.readLine()) != null) {
                        if ("NEW_MESSAGE".equals(line) && onNuevoMensaje != null) {
                            javax.swing.SwingUtilities.invokeLater(onNuevoMensaje);
                        } else if ("MESSAGE_READ".equals(line) && onMensajeLeido != null) {
                            // el receptor leyo nuestros mensajes - actualizar inbox
                            javax.swing.SwingUtilities.invokeLater(onMensajeLeido);
                        }
                    }
                } catch (IOException e) {
                    conectado = false;
                    // esperar 2s y reintentar
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void notificar(String receptor) {
        if (out != null && conectado) {
            try {
                out.println("NOTIFY:" + receptor);
            } catch (Exception ignored) {
                conectado = false;
            }
        }
    }

    // notifica al emisor original que leimos sus mensajes
    public void notificarLeido(String emisorOriginal) {
        if (out != null && conectado) {
            try {
                out.println("NOTIFY_READ:" + emisorOriginal);
            } catch (Exception ignored) {
                conectado = false;
            }
        }
    }

    public void desconectar() {
        activo = false;
        conectado = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isConectado() {
        return conectado;
    }
}
