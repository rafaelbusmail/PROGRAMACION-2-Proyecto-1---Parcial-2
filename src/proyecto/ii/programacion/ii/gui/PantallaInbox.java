package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.MensajeStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class PantallaInbox extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color GRIS = new Color(0x737373);
    private static final Color ROJO = new Color(0xED4956);

    public PantallaInbox() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    @Override
    public void refrescar() {
        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done() {
                removeAll();
                construir();
                revalidate();
                repaint();
            }
        }.execute();
    }

    private void construir() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        topBar.setPreferredSize(new Dimension(390, 46));

        JLabel lblTitulo = new JLabel("@" + sesion.getUsername());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(Color.BLACK);

        JLabel lblNuevo = new JLabel();
        ImageIcon iconEdit = Assets.getIcon("ic_add.png", 22);
        if (iconEdit != null) {
            lblNuevo.setIcon(iconEdit);
        } else {
            lblNuevo.setText("+");
            lblNuevo.setFont(new Font("Arial", Font.BOLD, 22));
        }
        lblNuevo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblNuevo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                nuevoMensaje();
            }
        });

        topBar.add(lblTitulo, BorderLayout.WEST);
        topBar.add(lblNuevo, BorderLayout.EAST);

        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Color.WHITE);

        try {
            MensajeStorage ms = new MensajeStorage(sesion.getUsername());
            ArrayList<String> convs = ms.leerConversacionesUnicas();
            ms.cerrar();

            UsuarioStorage us = new UsuarioStorage();

            if (convs.isEmpty()) {
                JLabel vacio = new JLabel(
                        "<html><div style='text-align:center;color:#737373;'>"
                        + "<br><br><br>No messages yet.<br>Tap + to start a conversation.</div></html>");
                vacio.setFont(new Font("Arial", Font.PLAIN, 14));
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                listaPanel.add(vacio);
            } else {
                for (String uname : convs) {
                    UsuarioRegistrado otro = us.buscarPorUsername(uname);
                    if (otro == null) {
                        continue;
                    }
                    MensajeStorage ms2 = new MensajeStorage(sesion.getUsername());
                    ArrayList<proyecto.ii.programacion.ii.model.Mensaje> conv
                            = ms2.leerConversacion(uname);
                    long noLeidos = conv.stream()
                            .filter(m -> m.getReceptor().equalsIgnoreCase(sesion.getUsername())
                            && m.esNoLeido())
                            .count();
                    // ultimo mensaje de la conversacion para indicador sent/seen
                    proyecto.ii.programacion.ii.model.Mensaje ultimo
                            = conv.isEmpty() ? null : conv.get(conv.size() - 1);
                    ms2.cerrar();
                    listaPanel.add(buildFila(otro, (int) noLeidos, ultimo,
                            sesion.getUsername()));
                }
            }
            us.cerrar();

            // actualizar badge navbar
            MensajeStorage msTotal = new MensajeStorage(sesion.getUsername());
            AppFrame.getInstance().setBadgeInbox(msTotal.contarNoLeidos() > 0);
            msTotal.cerrar();

        } catch (IOException e) {
            listaPanel.add(new JLabel("Error loading messages."));
        }

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildFila(UsuarioRegistrado otro, int noLeidos,
            proyecto.ii.programacion.ii.model.Mensaje ultimo, String miUsername) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(Color.WHITE);
        fila.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        fila.setMaximumSize(new Dimension(390, 70));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        FotoCircular foto = new FotoCircular(otro.getRutaFotoPerfil(), 50);

        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(Color.WHITE);

        JPanel nombres = new JPanel();
        nombres.setLayout(new BoxLayout(nombres, BoxLayout.Y_AXIS));
        nombres.setBackground(Color.WHITE);

        JLabel lblUser = new JLabel("@" + otro.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblNombre = new JLabel(otro.getNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNombre.setForeground(GRIS);

        nombres.add(lblUser);
        nombres.add(lblNombre);

        // indicador sent/seen basado en el ultimo mensaje
        if (ultimo != null && noLeidos == 0) {
            boolean yoEnvie = ultimo.getEmisor().equalsIgnoreCase(miUsername);
            if (yoEnvie) {
                // yo envie el ultimo mensaje - mostrar si fue visto o solo enviado
                String estado = ultimo.esNoLeido() ? "Sent" : "Seen";
                JLabel lblEstado = new JLabel(estado);
                lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
                lblEstado.setForeground(new Color(0x737373));
                nombres.add(lblEstado);
            }
        }

        // si hay mensajes no leidos, mostrar texto descriptivo debajo del nombre
        if (noLeidos > 0) {
            // 1 new message, 2 new messages, 3 new messages, 4 new messages, 4+ new messages
            String textoMsgs = noLeidos > 4
                    ? "4+ new messages"
                    : noLeidos + (noLeidos == 1 ? " new message" : " new messages");
            JLabel lblNuevos = new JLabel(textoMsgs);
            lblNuevos.setFont(new Font("Arial", Font.BOLD, 11));
            lblNuevos.setForeground(ROJO);
            nombres.add(lblNuevos);

            // "9+" usa fuente mas chica para caber, pero el circulo no cambia de tamaño
            String badgeTexto = noLeidos > 9 ? "9+" : String.valueOf(noLeidos);
            JLabel badge = new JLabel(badgeTexto) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ROJO);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            // tamaño fijo siempre - circulo perfecto
            badge.setFont(new Font("Arial", Font.BOLD, noLeidos > 9 ? 9 : 11));
            badge.setForeground(Color.WHITE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setVerticalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(22, 22));
            badge.setMinimumSize(new Dimension(22, 22));
            badge.setMaximumSize(new Dimension(22, 22));
            info.add(badge, BorderLayout.EAST);
        }

        info.add(nombres, BorderLayout.CENTER);

        fila.add(foto, BorderLayout.WEST);
        fila.add(info, BorderLayout.CENTER);

        MouseAdapter click = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                AppFrame.getInstance().abrirChat(otro.getUsername());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                fila.setBackground(new Color(0xFAFAFA));
                info.setBackground(new Color(0xFAFAFA));
                nombres.setBackground(new Color(0xFAFAFA));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fila.setBackground(Color.WHITE);
                info.setBackground(Color.WHITE);
                nombres.setBackground(Color.WHITE);
            }
        };
        fila.addMouseListener(click);
        foto.addMouseListener(click);
        return fila;
    }

    private void nuevoMensaje() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        String dest = JOptionPane.showInputDialog(this,
                "Enter username to message:", "New message", JOptionPane.PLAIN_MESSAGE);
        if (dest == null || dest.trim().isEmpty()) {
            return;
        }
        dest = dest.trim().toLowerCase().replace("@", "");

        if (sesion.getUsername().equalsIgnoreCase(dest)) {
            JOptionPane.showMessageDialog(this, "You can't message yourself.");
            return;
        }
        try {
            UsuarioStorage us = new UsuarioStorage();
            UsuarioRegistrado otro = us.buscarPorUsername(dest);
            us.cerrar();
            if (otro == null || !otro.isActivo()) {
                JOptionPane.showMessageDialog(this, "User not found.");
                return;
            }
            boolean puedeMsg = otro.isPublico();
            if (!puedeMsg) {
                FollowStorage fs = new FollowStorage();
                puedeMsg = fs.sigueA(sesion.getUsername(), dest)
                        && fs.sigueA(dest, sesion.getUsername());
            }
            if (!puedeMsg) {
                JOptionPane.showMessageDialog(this,
                        "This account is private. You need to follow each other to message.");
                return;
            }
            AppFrame.getInstance().abrirChat(dest);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error. Try again.");
        }
    }
}
