package proyecto.ii.programacion.ii.storage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// servidor de notificaciones en tiempo real para mensajeria
// escucha en localhost:9090 y retrasmite a clientes conectados
public class ChatServer {

    public static final int PORT = 9090;
    private static ChatServer instancia;
    private ServerSocket serverSocket;
    private final Map<String, List<PrintWriter>> clientes = new ConcurrentHashMap<>();
    private boolean corriendo = false;

    public static ChatServer getInstance() {
        if (instancia == null) {
            instancia = new ChatServer();
        }
        return instancia;
    }

    // en ese caso esta instancia usara ChatClient para conectarse al servidor existente
    public void iniciar() {
        if (corriendo) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                corriendo = true;
                while (corriendo) {
                    try {
                        Socket cliente = serverSocket.accept();
                        new Thread(() -> manejarCliente(cliente)).start();
                    } catch (IOException ignored) {
                    }
                }
            } catch (IOException e) {
                // puerto ocupado: otra instancia ya es el servidor, no hacer nada
                corriendo = false;
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void manejarCliente(Socket socket) {
        String username = null;
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            username = in.readLine();
            if (username == null) {
                return;
            }
            clientes.computeIfAbsent(username, k -> new ArrayList<>()).add(out);
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("NOTIFY:")) {
                    notificar(line.substring(7));
                } else if (line.startsWith("NOTIFY_READ:")) {
                    // notificar al emisor original que sus mensajes fueron leidos
                    notificarLeido(line.substring(12));
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (username != null) {
                List<PrintWriter> lista = clientes.get(username);
                if (lista != null) {
                    lista.clear();
                }
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void notificar(String receptor) {
        List<PrintWriter> lista = clientes.get(receptor);
        if (lista == null) {
            return;
        }
        for (PrintWriter pw : new ArrayList<>(lista)) {
            try {
                pw.println("NEW_MESSAGE");
            } catch (Exception ignored) {
            }
        }
    }

    // notifica al emisor original que el receptor leyo sus mensajes
    public void notificarLeido(String emisorOriginal) {
        List<PrintWriter> lista = clientes.get(emisorOriginal);
        if (lista == null) {
            return;
        }
        for (PrintWriter pw : new ArrayList<>(lista)) {
            try {
                pw.println("MESSAGE_READ");
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isCorriendo() {
        return corriendo;
    }
}
