package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.enums.EstadoMensaje;
import proyecto.ii.programacion.ii.enums.TipoMensaje;
import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.gui.componentes.TarjetaMensaje;
import proyecto.ii.programacion.ii.model.Mensaje;
import proyecto.ii.programacion.ii.model.Sticker;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.MensajeStorage;
import proyecto.ii.programacion.ii.storage.StickerStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PantallaChat extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color AZUL = new Color(0x0095F6);
    private static final Color FONDO_IN = new Color(0xEFEFEF);
    private static final Color ROJO = new Color(0xED4956);

    private JPanel mensajesPanel;
    private JScrollPane scrollMensajes;
    private JTextField campoTexto;
    private JLabel btnEnviar;
    private JPanel inputBar;
    private UsuarioRegistrado otroUsuario;
    private Timer autoRefresh;

    public PantallaChat() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    public void refrescarMensajes() {
        if (otroUsuario != null) {
            cargarMensajes();
        }
    }

    @Override
    public void refrescar() {
        if (autoRefresh != null) {
            autoRefresh.stop();
        }
        removeAll();
        String username = AppFrame.getInstance().getUsernameChat();
        if (username == null) {
            return;
        }
        try {
            UsuarioStorage us = new UsuarioStorage();
            otroUsuario = us.buscarPorUsername(username);
            us.cerrar();
        } catch (IOException e) {
            otroUsuario = null;
        }
        if (otroUsuario == null) {
            return;
        }
        construir();
        cargarMensajes();
        revalidate();
        repaint();

        autoRefresh = new Timer(3000, e -> {
            if (isShowing()) {
                verificarEstadoYCargar();
            }
        });
        autoRefresh.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (autoRefresh != null) {
            autoRefresh.stop();
        }
    }

    private void construir() {
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        topBar.setPreferredSize(new Dimension(390, 56));

        JLabel btnBack = new JLabel("‹");
        btnBack.setFont(new Font("Arial", Font.BOLD, 24));
        btnBack.setForeground(Color.BLACK);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (autoRefresh != null) {
                    autoRefresh.stop();
                }
                String anterior = AppFrame.getInstance().getPantallaAnteriorChat();
                AppFrame.getInstance().mostrarPantalla(anterior);
            }
        });

        JPanel userInfo = new JPanel(new BorderLayout(8, 0));
        userInfo.setBackground(Color.WHITE);
        FotoCircular foto = new FotoCircular(otroUsuario.getRutaFotoPerfil(), 36);
        JLabel lblUser = new JLabel("@" + otroUsuario.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        userInfo.add(foto, BorderLayout.WEST);
        userInfo.add(lblUser, BorderLayout.CENTER);

        JLabel btnMenu = new JLabel();
        ImageIcon iconMenu = Assets.getIcon("ic_back.png", 20);
        btnMenu.setText("...");
        btnMenu.setFont(new Font("Arial", Font.BOLD, 18));
        btnMenu.setForeground(Color.BLACK);
        btnMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem del = new JMenuItem("🗑  Delete conversation");
                del.setForeground(ROJO);
                del.addActionListener(ae -> {
                    if (JOptionPane.showConfirmDialog(PantallaChat.this,
                            "Delete this conversation?\nThis only removes it from your inbox.",
                            "Delete",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            if (autoRefresh != null) {
                                autoRefresh.stop();
                            }
                            UsuarioRegistrado s = AppFrame.getInstance().getSesion();
                            // cada usuario borra solo su propio lado (como instagram real)
                            MensajeStorage ms = new MensajeStorage(s.getUsername());
                            ms.eliminarConversacion(otroUsuario.getUsername());
                            ms.cerrar();
                            AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_INBOX);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(PantallaChat.this,
                                    "Error deleting conversation.");
                        }
                    }
                });
                menu.add(del);
                menu.show(btnMenu, 0, btnMenu.getHeight());
            }
        });

        topBar.add(btnBack, BorderLayout.WEST);
        topBar.add(userInfo, BorderLayout.CENTER);
        topBar.add(btnMenu, BorderLayout.EAST);

        mensajesPanel = new JPanel();
        mensajesPanel.setLayout(new BoxLayout(mensajesPanel, BoxLayout.Y_AXIS));
        mensajesPanel.setBackground(Color.WHITE);
        mensajesPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        scrollMensajes = new JScrollPane(mensajesPanel);
        scrollMensajes.setBorder(BorderFactory.createEmptyBorder());
        scrollMensajes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(16);
        scrollMensajes.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        inputBar = buildInputBar();

        add(topBar, BorderLayout.NORTH);
        add(scrollMensajes, BorderLayout.CENTER);
        add(inputBar, BorderLayout.SOUTH);
    }

    private JPanel buildInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        bar.setPreferredSize(new Dimension(390, 56));

        JLabel btnSticker = new JLabel("😊");
        btnSticker.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        btnSticker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSticker.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarStickers();
            }
        });

        campoTexto = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0x8E8E8E));
                    g.setFont(getFont());
                    g.drawString("Message...", 12, getHeight() / 2 + 5);
                }
            }
        };
        campoTexto.setBackground(FONDO_IN);
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        campoTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FONDO_IN, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        campoTexto.addActionListener(e -> enviarTexto());

        btnEnviar = new JLabel();
        ImageIcon sendIc = Assets.getIcon("ic_send_msg.png", 24);
        if (sendIc != null) {
            btnEnviar.setIcon(sendIc);
        } else {
            btnEnviar.setText("➤");
            btnEnviar.setFont(new Font("Arial", Font.BOLD, 18));
        }
        btnEnviar.setForeground(AZUL);
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                enviarTexto();
            }
        });

        bar.add(btnSticker, BorderLayout.WEST);
        bar.add(campoTexto, BorderLayout.CENTER);
        bar.add(btnEnviar, BorderLayout.EAST);
        return bar;
    }

    // recarga mensajes y verifica si aun se puede chatear
    // si el otro desactivo su cuenta, cambio a privado, o nos quito de followers -> bloquear
    private void verificarEstadoYCargar() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null || otroUsuario == null) {
            return;
        }
        try {
            UsuarioStorage us = new UsuarioStorage();
            UsuarioRegistrado actualizado = us.buscarPorUsername(otroUsuario.getUsername());
            us.cerrar();

            if (actualizado == null || !actualizado.isActivo()) {
                bloquearChat("This account has been deactivated.");
                return;
            }

            if (!actualizado.isPublico()) {
                FollowStorage fs = new FollowStorage();
                boolean sigueYo = fs.sigueA(sesion.getUsername(), actualizado.getUsername());
                if (!sigueYo) {
                    bloquearChat("You can no longer message this private account.");
                    return;
                }
            }

            otroUsuario = actualizado;
            desbloquearChat();
        } catch (IOException ignored) {
        }
        cargarMensajes();
    }

    private void bloquearChat(String motivo) {
        campoTexto.setEnabled(false);
        campoTexto.setText("");
        btnEnviar.setEnabled(false);
        inputBar.setBackground(new Color(0xFAFAFA));
        JLabel lblBlocked = new JLabel(
                "<html><div style='text-align:center;color:#737373;"
                + "font-size:11px;width:320px;word-wrap:break-word;'>"
                + motivo + "</div></html>");
        lblBlocked.setHorizontalAlignment(SwingConstants.CENTER);
        // solo agregar el label si no esta ya
        if (mensajesPanel.getComponentCount() == 0
                || !(mensajesPanel.getComponent(
                        mensajesPanel.getComponentCount() - 1) instanceof JLabel
                && ((JLabel) mensajesPanel.getComponent(
                        mensajesPanel.getComponentCount() - 1)).getText().contains(motivo))) {
            mensajesPanel.add(lblBlocked);
            mensajesPanel.revalidate();
            mensajesPanel.repaint();
        }
    }

    private void desbloquearChat() {
        campoTexto.setEnabled(true);
        btnEnviar.setEnabled(true);
        inputBar.setBackground(Color.WHITE);
    }

    private void cargarMensajes() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null || otroUsuario == null) {
            return;
        }

        JScrollBar bar = scrollMensajes.getVerticalScrollBar();
        boolean estaAlFinal = bar.getValue() >= bar.getMaximum() - bar.getVisibleAmount() - 50;

        mensajesPanel.removeAll();
        try {
            MensajeStorage ms = new MensajeStorage(sesion.getUsername());
            ArrayList<Mensaje> mensajes = ms.leerConversacion(otroUsuario.getUsername());
            ms.marcarLeidos(otroUsuario.getUsername());
            ms.cerrar();

            // notificar al emisor que sus mensajes fueron leidos para que vea "Seen"
            proyecto.ii.programacion.ii.storage.ChatClient client
                    = AppFrame.getInstance().getChatClient();
            if (client != null) {
                client.notificarLeido(otroUsuario.getUsername());
            }

            if (mensajes.isEmpty()) {
                JLabel vacio = new JLabel(
                        "<html><div style='text-align:center;color:#737373;'>"
                        + "<br><br>No messages yet. Say hi! 👋</div></html>");
                vacio.setFont(new Font("Arial", Font.PLAIN, 13));
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                mensajesPanel.add(vacio);
            } else {
                for (Mensaje m : mensajes) {
                    mensajesPanel.add(new TarjetaMensaje(m, sesion.getUsername()));
                }
            }
        } catch (IOException e) {
            mensajesPanel.add(new JLabel("Error loading messages."));
        }

        mensajesPanel.revalidate();
        mensajesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            if (estaAlFinal) {
                bar.setValue(bar.getMaximum());
            }
        });
    }

    private void enviarTexto() {
        String texto = campoTexto.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        if (texto.length() > 300) {
            JOptionPane.showMessageDialog(this, "Message cannot exceed 300 characters.");
            return;
        }
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        // verificar permiso antes de enviar
        try {
            UsuarioStorage us = new UsuarioStorage();
            UsuarioRegistrado actualizado = us.buscarPorUsername(otroUsuario.getUsername());
            us.cerrar();
            if (actualizado == null || !actualizado.isActivo()) {
                JOptionPane.showMessageDialog(this, "This account is no longer available.");
                return;
            }
            if (!actualizado.isPublico()) {
                FollowStorage fs = new FollowStorage();
                // consistente con verificarEstadoYCargar solo requiere que yo siga
                boolean sigueYo = fs.sigueA(sesion.getUsername(), actualizado.getUsername());
                if (!sigueYo) {
                    JOptionPane.showMessageDialog(this,
                            "You can no longer message this private account.");
                    bloquearChat("You can no longer message this private account.");
                    return;
                }
            }
        } catch (IOException ignored) {
        }

        String ts8 = String.valueOf(System.currentTimeMillis());
        ts8 = ts8.substring(Math.max(0, ts8.length() - 8));
        String uPartm = sesion.getUsername().substring(0,
                Math.min(8, sesion.getUsername().length()));
        String id = uPartm + "_" + ts8;

        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date());

        Mensaje m = new Mensaje(id, sesion.getUsername(),
                otroUsuario.getUsername(), fecha, hora,
                texto, TipoMensaje.TEXTO, EstadoMensaje.NO_LEIDO);
        try {
            MensajeStorage.enviar(m);
            proyecto.ii.programacion.ii.storage.ChatClient client
                    = AppFrame.getInstance().getChatClient();
            if (client != null) {
                client.notificar(otroUsuario.getUsername());
            }
            campoTexto.setText("");
            cargarMensajes();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error sending message.");
        }
    }

    private void mostrarStickers() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }
        try {
            StickerStorage ss = new StickerStorage(sesion.getUsername());
            ArrayList<Sticker> stickers = ss.leerTodos();
            ss.cerrar();
            if (stickers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No stickers available.");
                return;
            }
            JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
            grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            grid.setBackground(Color.WHITE);

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Stickers", true);
            dlg.setSize(310, 220);
            dlg.setLocationRelativeTo(this);

            for (Sticker s : stickers) {
                ImageIcon ic = Assets.getSticker(s.getRutaImagen(), 70);
                JLabel lbl = ic != null ? new JLabel(ic) : new JLabel(s.getNombre());
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        dlg.dispose();
                        enviarSticker(s);
                    }
                });
                grid.add(lbl);
            }
            dlg.add(new JScrollPane(grid));
            dlg.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading stickers.");
        }
    }

    private void enviarSticker(Sticker s) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        String ts8s = String.valueOf(System.currentTimeMillis());
        ts8s = ts8s.substring(Math.max(0, ts8s.length() - 8));
        String uParts = sesion.getUsername().substring(0,
                Math.min(8, sesion.getUsername().length()));
        String id = uParts + "_s_" + ts8s;

        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date());

        Mensaje m = new Mensaje(id, sesion.getUsername(),
                otroUsuario.getUsername(), fecha, hora,
                s.getRutaImagen(), TipoMensaje.STICKER, EstadoMensaje.NO_LEIDO);
        try {
            MensajeStorage.enviar(m);
            proyecto.ii.programacion.ii.storage.ChatClient client
                    = AppFrame.getInstance().getChatClient();
            if (client != null) {
                client.notificar(otroUsuario.getUsername());
            }
            cargarMensajes();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error sending sticker.");
        }
    }
}
