package proyecto.ii.programacion.ii.gui.componentes;

import proyecto.ii.programacion.ii.enums.TipoMensaje;
import proyecto.ii.programacion.ii.model.Mensaje;
import javax.swing.*;
import java.awt.*;


public class TarjetaMensaje extends JPanel {

    private static final Color AZUL_DM = new Color(0x3797EF);
    private static final Color GRIS_DM = new Color(0xEFEFEF);

    public TarjetaMensaje(Mensaje m, String usernameSession) {
        boolean soyEmisor = m.getEmisor().equalsIgnoreCase(usernameSession);

        setLayout(new FlowLayout(soyEmisor ? FlowLayout.RIGHT : FlowLayout.LEFT,
                12, 2)); // vgap reducido a 2 para menos espacio entre burbujas
        setBackground(Color.WHITE);
        setMaximumSize(new Dimension(390, Integer.MAX_VALUE));
        setOpaque(false);

        if (m.getTipo() == TipoMensaje.STICKER) {
            buildSticker(m.getContenido());
        } else {
            buildTexto(m.getContenido(), m.getHora(), soyEmisor);
        }
    }

    private void buildTexto(String contenido, String hora, boolean soyEmisor) {
        JPanel burbuja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(soyEmisor ? AZUL_DM : GRIS_DM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        burbuja.setOpaque(false);
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblTexto = new JLabel(
                "<html><div style='max-width:220px;'>" + contenido + "</div></html>");
        lblTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTexto.setForeground(soyEmisor ? Color.WHITE : Color.BLACK);

        JLabel lblHora = new JLabel(hora);
        lblHora.setFont(new Font("Arial", Font.PLAIN, 10));
        lblHora.setForeground(soyEmisor
                ? new Color(255, 255, 255, 180) : new Color(0x737373));
        lblHora.setAlignmentX(soyEmisor
                ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        burbuja.add(lblTexto);
        burbuja.add(Box.createVerticalStrut(2));
        burbuja.add(lblHora);
        add(burbuja);
    }

    private void buildSticker(String rutaSticker) {
        ImageIcon icon = Assets.getSticker(rutaSticker, 80);
        JLabel lbl = icon != null ? new JLabel(icon) : new JLabel("🎭");
        if (icon == null) {
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }
        add(lbl);
    }
}
