package proyecto.ii.programacion.ii.gui.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

// carga de todos los assets: iconos, avatares, posts, fuente, stickers
public class Assets {

    private static Font fuenteInstagram = null;

    // iconos desde icons/
    public static ImageIcon getIcon(String nombre, int size) {
        return scaled(fromPath("icons/" + nombre), size, size);
    }

    // avatar circular soporta classpath y ruta absoluta del disco
    public static ImageIcon getAvatarCircular(String ruta, int d) {
        try {
            BufferedImage src = load(ruta);
            if (src == null) {
                src = load("avatars/default_avatar.png");
            }
            if (src == null) {
                return placeholderAvatar(d);
            }
            return new ImageIcon(cropCircle(src, d));
        } catch (Exception e) {
            return placeholderAvatar(d);
        }
    }

    // imagen de post rellena el ancho exacto, crop centrado vertical (como instagram)
    // no distorsiona: corta el exceso en vez de letterbox con fondo negro
    public static ImageIcon getPostImagen(String ruta, int ancho, int alto) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return placeholderPost(ancho, alto);
        }
        try {
            BufferedImage src = load(ruta);
            if (src == null) {
                return placeholderPost(ancho, alto);
            }

            // escalar para que el ancho encaje exacto, luego crop centrado en alto
            double escala = (double) ancho / src.getWidth();
            int scaledH = (int) (src.getHeight() * escala);
            int scaledW = ancho;

            // si queda mas chico en alto, escalar por alto
            if (scaledH < alto) {
                escala = (double) alto / src.getHeight();
                scaledW = (int) (src.getWidth() * escala);
                scaledH = alto;
            }

            BufferedImage scaled = new BufferedImage(scaledW, scaledH,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, scaledW, scaledH);
            g2.drawImage(src, 0, 0, scaledW, scaledH, null);
            g2.dispose();

            // crop centrado
            int x = Math.max(0, (scaledW - ancho) / 2);
            int y = Math.max(0, (scaledH - alto) / 2);
            int cropW = Math.min(ancho, scaledW - x);
            int cropH = Math.min(alto, scaledH - y);
            BufferedImage cropped = scaled.getSubimage(x, y, cropW, cropH);

            // si el crop es mas chico que el target, centrar en fondo blanco
            if (cropW < ancho || cropH < alto) {
                BufferedImage result = new BufferedImage(ancho, alto,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g3 = result.createGraphics();
                g3.setColor(Color.WHITE);
                g3.fillRect(0, 0, ancho, alto);
                g3.drawImage(cropped, (ancho - cropW) / 2, (alto - cropH) / 2,
                        null);
                g3.dispose();
                return new ImageIcon(result);
            }

            return new ImageIcon(cropped);
        } catch (Exception e) {
            return placeholderPost(ancho, alto);
        }
    }

    // imagen para feed con altura proporcional al aspect ratio
    // cuadrada=390x390, vertical=390x487, horizontal=390x204 (PDF sec 4.1)
    public static ImageIcon getPostImagenFeed(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return placeholderPost(390, 300);
        }
        try {
            BufferedImage src = load(ruta);
            if (src == null) {
                return placeholderPost(390, 300);
            }
            int dW = 390;
            int dH = (int) (src.getHeight() * (390.0 / src.getWidth()));
            dH = Math.max(204, Math.min(487, dH));
            BufferedImage result = new BufferedImage(dW, dH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = result.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, dW, dH);
            int scaledH = (int) (src.getHeight() * (390.0 / src.getWidth()));
            g2.drawImage(src, 0, (dH - scaledH) / 2, dW, scaledH, null);
            g2.dispose();
            return new ImageIcon(result);
        } catch (Exception e) {
            return placeholderPost(390, 300);
        }
    }

    // sticker classpath o disco
    public static ImageIcon getSticker(String ruta, int size) {
        return scaled(load_safe(ruta), size, size);
    }

    // fuente billabong, fallback georgia italic
    public static Font getFuenteInstagram(float size) {
        if (fuenteInstagram != null) {
            return fuenteInstagram.deriveFont(size);
        }
        try {
            InputStream is = Assets.class.getClassLoader()
                    .getResourceAsStream("fonts/Billabong.ttf");
            if (is != null) {
                fuenteInstagram = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .registerFont(fuenteInstagram);
                return fuenteInstagram.deriveFont(size);
            }
        } catch (Exception ignored) {
        }
        return new Font("Serif", Font.ITALIC, (int) size);
    }

    // carga imagen desde ruta absoluta del disco, classpath, o relativa
    // BufferedInputStream es necesario porque ImageIO necesita mark/reset
    // para detectar el formato — sin el, JPEG desde classpath retorna null
    // tambien intenta extensiones alternativas si la ruta original falla (.png -> .jpg)
    public static BufferedImage load(String ruta) throws IOException {
        if (ruta == null || ruta.trim().isEmpty()) {
            return null;
        }

        // intento 1: archivo absoluto en el disco
        File f = new File(ruta);
        if (f.isAbsolute() && f.exists()) {
            BufferedImage img = leerArchivo(f);
            if (img != null) {
                return img;
            }
        }

        // intento 2: classpath con BufferedInputStream
        // usa leerStream() que fuerza jpeg reader si ImageIO.read devuelve null
        InputStream raw = Assets.class.getClassLoader().getResourceAsStream(ruta);
        if (raw != null) {
            BufferedImage img = leerStream(new java.io.BufferedInputStream(raw));
            if (img != null) {
                return img;
            }
        }

        // intento 3: ruta relativa como archivo del disco
        if (f.exists()) {
            BufferedImage img = leerArchivo(f);
            if (img != null) {
                return img;
            }
        }

        // intento 4: si termina en .png probar .jpg (archivo renombrado)
        if (ruta.toLowerCase().endsWith(".png")) {
            String rutaJpg = ruta.substring(0, ruta.length() - 4) + ".jpg";
            InputStream rawJpg = Assets.class.getClassLoader().getResourceAsStream(rutaJpg);
            if (rawJpg != null) {
                BufferedImage img = leerStream(new java.io.BufferedInputStream(rawJpg));
                if (img != null) {
                    return img;
                }
            }
            File fJpg = new File(rutaJpg);
            if (fJpg.exists()) {
                BufferedImage img = leerArchivo(fJpg);
                if (img != null) {
                    return img;
                }
            }
        }

        System.err.println("Assets.load() no pudo cargar: " + ruta);
        return null;
    }

    // lee un archivo forzando jpeg reader si ImageIO.read normal falla
    // esto resuelve JPEGs renombrados a .png que confunden al PNG reader
    private static BufferedImage leerArchivo(File f) throws IOException {
        BufferedImage img = ImageIO.read(f);
        if (img != null) {
            return img;
        }
        // fallback: leer como stream con forzado de jpeg
        try (FileInputStream fis = new FileInputStream(f)) {
            return leerStream(new java.io.BufferedInputStream(fis));
        }
    }

    // lee un stream intentando primero ImageIO.read normal
    // si retorna null (JPEG renombrado a .png), fuerza el jpeg image reader
    private static BufferedImage leerStream(java.io.BufferedInputStream bis) throws IOException {
        bis.mark(64);
        BufferedImage img = ImageIO.read(bis);
        if (img != null) {
            return img;
        }

        // ImageIO.read devolvio null - intentar forzar jpeg reader
        // puede pasar cuando el archivo es JPEG con extension .png
        try {
            bis.reset();
        } catch (IOException e) {
            return null;
        }
        javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(bis);
        if (iis == null) {
            return null;
        }

        // buscar el jpeg reader explicitamente
        java.util.Iterator<javax.imageio.ImageReader> readers
                = ImageIO.getImageReadersByFormatName("jpeg");
        if (!readers.hasNext()) {
            return null;
        }

        javax.imageio.ImageReader reader = readers.next();
        try {
            reader.setInput(iis, true, true);
            return reader.read(0);
        } catch (Exception e) {
            return null;
        } finally {
            reader.dispose();
        }
    }

    // load sin throws para uso interno
    private static BufferedImage load_safe(String ruta) {
        try {
            return load(ruta);
        } catch (Exception e) {
            return null;
        }
    }

    // recorta en circulo con antialiasing
    public static BufferedImage cropCircle(BufferedImage src, int d) {
        BufferedImage out = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, d, d));
        int s = Math.min(src.getWidth(), src.getHeight());
        int sx = (src.getWidth() - s) / 2, sy = (src.getHeight() - s) / 2;
        g2.drawImage(src.getSubimage(sx, sy, s, s), 0, 0, d, d, null);
        g2.dispose();
        return out;
    }

    private static ImageIcon scaled(BufferedImage src, int w, int h) {
        if (src == null) {
            return null;
        }
        return new ImageIcon(src.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private static BufferedImage fromPath(String ruta) {
        try {
            return load(ruta);
        } catch (Exception e) {
            return null;
        }
    }

    private static ImageIcon placeholderAvatar(int d) {
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xDBDBDB));
        g2.fillOval(0, 0, d, d);
        g2.setColor(new Color(0x8E8E8E));
        int margin = d / 4;
        g2.fillOval(d / 2 - d / 8, d / 4 - d / 10, d / 4, d / 4); // cabeza
        g2.fillArc(margin, d / 2, d - 2 * margin, d / 2, 0, 180); // cuerpo
        g2.dispose();
        return new ImageIcon(img);
    }

    private static ImageIcon placeholderPost(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(0xEFEFEF));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(0xC7C7C7));
        int s = Math.min(w, h) / 4;
        g2.fillRect(w / 2 - s, h / 2 - s, s * 2, s * 2); // placeholder cuadrado
        g2.dispose();
        return new ImageIcon(img);
    }
}
