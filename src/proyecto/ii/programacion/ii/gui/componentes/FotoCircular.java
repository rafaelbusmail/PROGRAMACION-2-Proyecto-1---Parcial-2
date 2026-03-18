package proyecto.ii.programacion.ii.gui.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class FotoCircular extends JComponent {

    private BufferedImage imagen;
    private final int diametro;
    private Color colorBorde;
    private int grosorBorde;

    public FotoCircular(String rutaRecurso, int diametro) {
        this.diametro = diametro;
        this.colorBorde = null;
        this.grosorBorde = 0;
        setPreferredSize(new Dimension(diametro, diametro));
        setMinimumSize(new Dimension(diametro, diametro));
        setMaximumSize(new Dimension(diametro, diametro));
        setOpaque(false);
        cargarImagen(rutaRecurso);
    }

    public FotoCircular(String rutaRecurso, int diametro, Color borde, int grosor) {
        this(rutaRecurso, diametro);
        this.colorBorde = borde;
        this.grosorBorde = grosor;
    }

    public void setImagen(String rutaRecurso) {
        cargarImagen(rutaRecurso);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int offset = grosorBorde > 0 ? grosorBorde + 2 : 0;
        int size = diametro - offset * 2;

        if (colorBorde != null && grosorBorde > 0) {
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(grosorBorde));
            g2.drawOval(grosorBorde / 2, grosorBorde / 2,
                    diametro - grosorBorde, diametro - grosorBorde);
        }

        if (imagen != null) {
            g2.setClip(new Ellipse2D.Float(offset, offset, size, size));
            g2.drawImage(imagen, offset, offset, size, size, null);
        } else {
            g2.setColor(new Color(0xDBDBDB));
            g2.fillOval(offset, offset, size, size);
            g2.setColor(new Color(0x737373));
            g2.setFont(new Font("Arial", Font.PLAIN, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            String t = "?";
            g2.drawString(t,
                    offset + (size - fm.stringWidth(t)) / 2,
                    offset + (size + fm.getAscent()) / 2 - 2);
        }
        g2.dispose();
    }

    private void cargarImagen(String rutaRecurso) {
        if (rutaRecurso == null || rutaRecurso.trim().isEmpty()) {
            imagen = null;
            return;
        }
        try {
            imagen = Assets.load(rutaRecurso);
            if (imagen != null) {
                // recortar en cuadrado centrado para el circulo
                int s = Math.min(imagen.getWidth(), imagen.getHeight());
                int sx = (imagen.getWidth() - s) / 2;
                int sy = (imagen.getHeight() - s) / 2;
                imagen = imagen.getSubimage(sx, sy, s, s);
            }
        } catch (Exception e) {
            imagen = null;
        }
    }
}
