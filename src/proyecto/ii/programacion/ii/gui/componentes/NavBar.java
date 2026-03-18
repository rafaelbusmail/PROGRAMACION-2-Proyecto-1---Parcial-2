package proyecto.ii.programacion.ii.gui.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NavBar extends JPanel {

    public static final String HOME = "HOME";
    public static final String SEARCH = "SEARCH";
    public static final String ADD = "ADD";
    public static final String INBOX = "INBOX";
    public static final String PROFILE = "PROFILE";

    public interface NavBarListener {

        void onNavClick(String seccion);
    }

    private NavBarListener listener;
    private String seccionActiva = HOME;
    private JLabel badgeInbox;

    // colores insta
    private static final Color COLOR_BORDE = new Color(0xDBDBDB);
    private static final Color COLOR_FONDO = Color.WHITE;

    public NavBar() {
        setLayout(new GridLayout(1, 5));
        setBackground(COLOR_FONDO);
        setPreferredSize(new Dimension(390, 49));
        setMaximumSize(new Dimension(390, 49));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));
        construir();
    }

    public void setListener(NavBarListener l) {
        this.listener = l;
    }

    // refrescar iconos
    public void setSeccionActiva(String seccion) {
        this.seccionActiva = seccion;
        refrescarIconos();
    }

    public void setBadgeInbox(boolean visible) {
        if (badgeInbox != null) {
            badgeInbox.setVisible(visible);
        }
    }

    private void construir() {
        String[] secciones = {HOME, SEARCH, ADD, INBOX, PROFILE};

        for (String sec : secciones) {
            JPanel celda = new JPanel(null);
            celda.setBackground(COLOR_FONDO);
            celda.setPreferredSize(new Dimension(78, 49));

            JLabel icono = new JLabel();
            icono.setHorizontalAlignment(SwingConstants.CENTER);
            icono.setBounds(0, 0, 78, 49);
            icono.setName(sec);

            actualizarIcono(icono, sec, false);

            if (sec.equals(INBOX)) {
                badgeInbox = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0xED4956));
                        g2.fillOval(0, 0, getWidth(), getHeight());
                    }
                };
                badgeInbox.setBounds(46, 10, 8, 8);
                badgeInbox.setVisible(false);
                celda.add(badgeInbox);
            }

            celda.add(icono);

            MouseAdapter click = new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    navegar(sec);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            };
            celda.addMouseListener(click);
            icono.addMouseListener(click);

            add(celda);
        }
    }

    private void navegar(String seccion) {
        seccionActiva = seccion;
        refrescarIconos();
        if (listener != null) {
            listener.onNavClick(seccion);
        }
    }

    private void refrescarIconos() {
        // Recorrer todos los componentes y actualizar iconos
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                JPanel celda = (JPanel) comp;
                for (Component inner : celda.getComponents()) {
                    if (inner instanceof JLabel) {
                        JLabel lbl = (JLabel) inner;
                        String sec = lbl.getName();
                        if (sec != null && !sec.isEmpty()) {
                            actualizarIcono(lbl, sec,
                                    sec.equals(seccionActiva));
                        }
                    }
                }
            }
        }
        repaint();
    }

    private void actualizarIcono(JLabel lbl, String seccion, boolean activo) {
        int size = 26;
        ImageIcon icon = null;

        switch (seccion) {
            case HOME:
                icon = activo
                        ? Assets.getIcon("ic_home_filled.png", size)
                        : Assets.getIcon("ic_home.png", size);
                break;
            case SEARCH:
                icon = Assets.getIcon("ic_search.png", size);
                break;
            case ADD:
                icon = Assets.getIcon("ic_add.png", size);
                break;
            case INBOX:
                icon = activo
                        ? Assets.getIcon("ic_inbox_filled.png", size)
                        : Assets.getIcon("ic_inbox.png", size);
                break;
            case PROFILE:
                icon = activo
                        ? Assets.getIcon("ic_profile_selected.png", size)
                        : Assets.getIcon("ic_profile.png", size);
                break;
        }

        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lbl.setForeground(activo ? Color.BLACK : new Color(0x737373));

        if (icon != null) {
            lbl.setIcon(icon);
            lbl.setText("");
        } else {
            lbl.setIcon(null);
            switch (seccion) {
                case HOME:
                    lbl.setText("⌂");
                    break;
                case SEARCH:
                    lbl.setText("🔍");
                    break;
                case ADD:
                    lbl.setText("＋");
                    break;
                case INBOX:
                    lbl.setText("✉");
                    break;
                case PROFILE:
                    lbl.setText("👤");
                    break;
            }
        }
    }
}
