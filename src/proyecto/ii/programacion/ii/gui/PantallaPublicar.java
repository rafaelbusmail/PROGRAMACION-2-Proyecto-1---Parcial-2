package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.FileManager;
import proyecto.ii.programacion.ii.storage.PublicacionStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PantallaPublicar extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color AZUL = new Color(0x0095F6);
    private static final Color GRIS = new Color(0x737373);

    private JLabel lblImagenPreview;
    private JTextArea areaContenido;
    private JTextField campoHashtags;
    private JTextField campoMenciones;
    private String rutaImagenSeleccionada = "";

    public PantallaPublicar() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construir();
    }

    @Override
    public void refrescar() {
        limpiar();
    }

    private void construir() {
        //tOpBar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        topBar.setPreferredSize(new Dimension(390, 46));

        JLabel lblTitulo = new JLabel("New post");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(Color.BLACK);

        JLabel btnShare = new JLabel("Share");
        btnShare.setFont(new Font("Arial", Font.BOLD, 14));
        btnShare.setForeground(AZUL);
        btnShare.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // mouseClicked a mouseReleased para consistencia
        btnShare.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                publicar();
            }
        });

        topBar.add(lblTitulo, BorderLayout.WEST);
        topBar.add(btnShare, BorderLayout.EAST);

        // panel principal
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Preview de imagen + boton seleccionar
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(new Color(0xFAFAFA));
        imgPanel.setMaximumSize(new Dimension(390, 300));
        imgPanel.setPreferredSize(new Dimension(390, 300));
        imgPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));

        lblImagenPreview = new JLabel();
        lblImagenPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagenPreview.setVerticalAlignment(SwingConstants.CENTER);
        mostrarPlaceholderImagen();

        lblImagenPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblImagenPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarImagen();
            }
        });
        imgPanel.add(lblImagenPreview, BorderLayout.CENTER);

        // Boton select photo
        JPanel selPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selPanel.setBackground(Color.WHITE);
        selPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        selPanel.setMaximumSize(new Dimension(390, 46));

        JButton btnSelectImg = new JButton("Select from gallery");
        btnSelectImg.setFont(new Font("Arial", Font.BOLD, 13));
        btnSelectImg.setForeground(AZUL);
        btnSelectImg.setBackground(Color.WHITE);
        btnSelectImg.setBorderPainted(false);
        btnSelectImg.setFocusPainted(false);
        btnSelectImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelectImg.addActionListener(e -> seleccionarImagen());
        selPanel.add(btnSelectImg);

        // Caption
        JPanel captionPanel = new JPanel(new BorderLayout(12, 0));
        captionPanel.setBackground(Color.WHITE);
        captionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        captionPanel.setMaximumSize(new Dimension(390, 90));

        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        String ruta = sesion != null ? sesion.getRutaFotoPerfil() : "";
        captionPanel.add(new proyecto.ii.programacion.ii.gui.componentes.FotoCircular(ruta, 36), BorderLayout.WEST);

        areaContenido = new JTextArea(3, 20);
        areaContenido.setFont(new Font("Arial", Font.PLAIN, 14));
        areaContenido.setLineWrap(true);
        areaContenido.setWrapStyleWord(true);
        areaContenido.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        areaContenido.setBackground(Color.WHITE);

        // Placeholder para textarea
        areaContenido.setText("Write a caption...");
        areaContenido.setForeground(GRIS);
        areaContenido.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (areaContenido.getText().equals("Write a caption...")) {
                    areaContenido.setText("");
                    areaContenido.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (areaContenido.getText().trim().isEmpty()) {
                    areaContenido.setText("Write a caption...");
                    areaContenido.setForeground(GRIS);
                }
            }
        });

        // Contador de caracteres
        JLabel lblCount = new JLabel("0/220");
        lblCount.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCount.setForeground(GRIS);
        areaContenido.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            void update() {
                int len = areaContenido.getText().equals("Write a caption...")
                        ? 0 : areaContenido.getText().length();
                lblCount.setText(len + "/220");
                lblCount.setForeground(len > 220 ? new Color(0xED4956) : GRIS);
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        });

        JPanel captionRight = new JPanel(new BorderLayout());
        captionRight.setBackground(Color.WHITE);
        captionRight.add(areaContenido, BorderLayout.CENTER);
        captionRight.add(lblCount, BorderLayout.SOUTH);
        captionPanel.add(captionRight, BorderLayout.CENTER);

        // Hashtags
        campoHashtags = crearCampoExtra("Add hashtags  (e.g. #java #code)");
        JPanel hashPanel = wrapCampo(campoHashtags);

        // Menciones
        campoMenciones = crearCampoExtra("Tag people  (e.g. @user1 @user2)");
        JPanel mencPanel = wrapCampo(campoMenciones);

        body.add(imgPanel);
        body.add(selPanel);
        body.add(captionPanel);
        body.add(hashPanel);
        body.add(mencPanel);
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void seleccionarImagen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select image");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (*.jpg, *.png)", "jpg", "jpeg", "png", "webp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            rutaImagenSeleccionada = fc.getSelectedFile().getAbsolutePath();
            ImageIcon preview = Assets.getPostImagen(rutaImagenSeleccionada, 390, 300);
            lblImagenPreview.setIcon(preview);
            lblImagenPreview.setText("");
        }
    }

    private void publicar() {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        String contenido = areaContenido.getText().trim();
        if (contenido.equals("Write a caption...")) {
            contenido = "";
        }

        if (rutaImagenSeleccionada.isEmpty() && contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Add a photo or write something to share.");
            return;
        }
        if (contenido.length() > 220) {
            JOptionPane.showMessageDialog(this,
                    "Caption cannot exceed 220 characters.");
            return;
        }

        String hashtags = campoHashtags.getText().trim();
        String menciones = campoMenciones.getText().trim();

        if (hashtags.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Hashtags cannot exceed 100 characters (" + hashtags.length() + "/100).");
            return;
        }
        if (menciones.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Mentions cannot exceed 100 characters (" + menciones.length() + "/100).");
            return;
        }
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date());
        String ts = String.valueOf(System.currentTimeMillis());
        String tsCorto = ts.substring(Math.max(0, ts.length() - 8));
        String uPart = sesion.getUsername().substring(0, Math.min(8, sesion.getUsername().length()));
        String id = uPart + "_" + tsCorto;
        String tipo = rutaImagenSeleccionada.isEmpty() ? "TEXT" : "IMAGE";

        // copiar imagen a INSTA_RAIZ/username/imagenes/ y guardar ruta relativa
        String rutaParaGuardar = rutaImagenSeleccionada;
        if (!rutaImagenSeleccionada.isEmpty()) {
            try {
                File origen = new File(rutaImagenSeleccionada);
                String carpetaImagenes = FileManager.getRutaImagenes(sesion.getUsername());
                FileManager.crearCarpeta(carpetaImagenes);
                // nombre del archivo: id del post + extension original
                String ext = rutaImagenSeleccionada.contains(".")
                        ? rutaImagenSeleccionada.substring(rutaImagenSeleccionada.lastIndexOf("."))
                        : ".png";
                File destino = new File(carpetaImagenes + File.separator + id + ext);
                Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // guardar ruta relativa desde el directorio de trabajo
                rutaParaGuardar = destino.getPath();
            } catch (Exception ex) {
                // si no se puede copiar, usar la ruta original como fallback
                rutaParaGuardar = rutaImagenSeleccionada;
            }
        }

        Publicacion nueva = new Publicacion(id, sesion.getUsername(),
                fecha, hora, contenido, hashtags, menciones,
                rutaParaGuardar, tipo, 0);

        try {
            PublicacionStorage ps = new PublicacionStorage(sesion.getUsername());
            ps.agregar(nueva);
            ps.cerrar();

            JOptionPane.showMessageDialog(this, "Posted successfully!");
            limpiar();
            AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error posting. Try again.");
        }
    }

    private void limpiar() {
        rutaImagenSeleccionada = "";
        mostrarPlaceholderImagen();
        areaContenido.setText("Write a caption...");
        areaContenido.setForeground(new Color(0x737373));
        campoHashtags.setText("");
        campoMenciones.setText("");
    }

    private void mostrarPlaceholderImagen() {
        lblImagenPreview.setIcon(null);
        lblImagenPreview.setText("<html><div style='text-align:center;color:#737373;'>"
                + "📷<br><br>Tap to select a photo</div></html>");
        lblImagenPreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
    }

    private JTextField crearCampoExtra(String placeholder) {
        JTextField campo = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(GRIS);
                    g.setFont(getFont());
                    g.drawString(placeholder, 12, getHeight() / 2 + 4);
                }
            }
        };
        campo.setFont(new Font("Arial", Font.PLAIN, 13));
        campo.setForeground(Color.BLACK);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createEmptyBorder());
        return campo;
    }

    private JPanel wrapCampo(JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setMaximumSize(new Dimension(390, 46));
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

}
