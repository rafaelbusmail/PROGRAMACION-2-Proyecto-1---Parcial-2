package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.enums.*;
import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.model.Sticker;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.FileManager;
import proyecto.ii.programacion.ii.storage.StickerStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PantallaRegistro extends JPanel implements AppFrame.Refrescable {

    private static final Color AZUL = new Color(0x0095F6);
    private static final Color AZUL_DIS = new Color(0x9ACAF7);
    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color FONDO_CAMPO = new Color(0xFAFAFA);
    private static final Color GRIS_TEXTO = new Color(0x737373);
    private static final Color ROJO = new Color(0xED4956);
    private static final Color VERDE = new Color(0x2ECC71);

    private JTextField campoNombre;
    private JTextField campoUsername;
    private JPasswordField campoPassword;
    private JPasswordField campoPasswordConfirm;
    private JTextField campoEdad;
    private JComboBox<String> comboGenero;
    private JComboBox<String> comboTipo;
    private JButton btnRegistrar;
    private JLabel lblFeedback;
    private JLabel lblFotoPreview;
    private String rutaFoto = "avatars/default_avatar.png";
    private boolean passVisible = false;

    public PantallaRegistro() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construir();
    }

    @Override
    public void refrescar() {
        limpiar();
    }

    private void construir() {

        campoNombre = crearCampo("Full name");
        campoUsername = crearCampo("Username");
        campoPassword = crearPasswordField();  // se crea aquí, antes de usarse
        campoPasswordConfirm = crearPasswordField();  // confirmacion de contraseña
        campoEdad = crearCampo("Age");
        comboGenero = crearCombo(new String[]{"Male", "Female"});
        comboTipo = crearCombo(new String[]{"Public", "Private"});

        // main panel
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(28, 40, 8, 40));

        // Logo
        JLabel lblLogo = new JLabel("Instagram");
        Font fi = Assets.getFuenteInstagram(48f);
        if (fi.getFamily().equals("Serif")) {
            fi = new Font("Georgia", Font.ITALIC, 48);
        }
        lblLogo.setFont(fi);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel lblTag = new JLabel(
                "<html><div style='text-align:center;width:280px;'>Sign up to see photos and videos from your friends.</div></html>");
        lblTag.setFont(new Font("Arial", Font.BOLD, 13));
        lblTag.setForeground(GRIS_TEXTO);
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Foto
        lblFotoPreview = new JLabel();
        lblFotoPreview.setPreferredSize(new Dimension(60, 60));
        lblFotoPreview.setMaximumSize(new Dimension(60, 60));
        lblFotoPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        actualizarFoto();

        JButton btnFoto = new JButton("Add profile photo");
        btnFoto.setFont(new Font("Arial", Font.PLAIN, 12));
        btnFoto.setForeground(AZUL);
        btnFoto.setBackground(Color.WHITE);
        btnFoto.setBorderPainted(false);
        btnFoto.setFocusPainted(false);
        btnFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFoto.addActionListener(e -> seleccionarFoto());

        // Feedback en tiempo real
        lblFeedback = new JLabel(" ");
        lblFeedback.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFeedback.setForeground(ROJO);
        lblFeedback.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFeedback.setHorizontalAlignment(SwingConstants.CENTER);
        lblFeedback.setMaximumSize(new Dimension(310, 30));

        // Botón Sign up
        btnRegistrar = crearBoton("Sign up", AZUL_DIS);
        btnRegistrar.setEnabled(false);
        btnRegistrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistrar.addActionListener(e -> intentarRegistro());

        // Panel password con Show/Hide
        JPanel panelPass = buildPanelPassword();
        panelPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Alinear campos al centro
        campoNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        campoUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        campoEdad.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Labels de sección
        JLabel lblGenero = labelSeccion("Gender");
        JLabel lblTipo = labelSeccion("Account type");
        lblGenero.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboGenero.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboTipo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Términos
        JLabel lblTerm = new JLabel(
                "<html><div style='text-align:center;color:#737373;font-size:10px;'>"
                + "By signing up, you agree to our Terms and Privacy Policy.</div></html>");
        lblTerm.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTerm.setMaximumSize(new Dimension(310, 30));

        // ensamblar
        form.add(lblLogo);
        form.add(Box.createVerticalStrut(4));
        form.add(lblTag);
        form.add(Box.createVerticalStrut(8));
        form.add(lblFotoPreview);
        form.add(Box.createVerticalStrut(2));
        form.add(btnFoto);
        form.add(Box.createVerticalStrut(10));
        form.add(campoNombre);
        form.add(Box.createVerticalStrut(6));
        form.add(campoUsername);
        form.add(Box.createVerticalStrut(6));
        form.add(panelPass);       //campo password visible aquí
        form.add(Box.createVerticalStrut(6));
        form.add(buildPanelPasswordConfirm());  // confirmacion de contraseña
        form.add(Box.createVerticalStrut(6));
        form.add(campoEdad);
        form.add(Box.createVerticalStrut(4));
        form.add(lblGenero);
        form.add(Box.createVerticalStrut(2));
        form.add(comboGenero);
        form.add(Box.createVerticalStrut(4));
        form.add(lblTipo);
        form.add(Box.createVerticalStrut(2));
        form.add(comboTipo);
        form.add(Box.createVerticalStrut(6));
        form.add(lblFeedback);
        form.add(Box.createVerticalStrut(4));
        form.add(btnRegistrar);
        form.add(Box.createVerticalStrut(6));
        form.add(lblTerm);

        // DocumentListener detecta cambios incluso sin teclas (paste, etc.)
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarFeedback();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarFeedback();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarFeedback();
            }
        };
        campoNombre.getDocument().addDocumentListener(dl);
        campoUsername.getDocument().addDocumentListener(dl);
        campoPassword.getDocument().addDocumentListener(dl);
        campoPasswordConfirm.getDocument().addDocumentListener(dl);
        campoEdad.getDocument().addDocumentListener(dl);

        //panel inferior
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));

        JPanel pLogin = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 12));
        pLogin.setBackground(Color.WHITE);
        JLabel ya = new JLabel("Already have an account?");
        ya.setFont(new Font("Arial", Font.PLAIN, 13));
        ya.setForeground(GRIS_TEXTO);
        JLabel lnLogin = new JLabel("Log in.");
        lnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        lnLogin.setForeground(AZUL);
        lnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_LOGIN);
            }
        });
        pLogin.add(ya);
        pLogin.add(lnLogin);
        bottom.add(pLogin, BorderLayout.CENTER);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildPanelPassword() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FONDO_CAMPO);
        wrapper.setMaximumSize(new Dimension(310, 40));
        wrapper.setPreferredSize(new Dimension(310, 40));
        wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));

        // campoPassword ya fue creado en construir()
        campoPassword.setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 4));
        campoPassword.setBackground(FONDO_CAMPO);
        campoPassword.setOpaque(true);

        JLabel btnOjo = new JLabel("Show");
        btnOjo.setFont(new Font("Arial", Font.BOLD, 11));
        btnOjo.setForeground(new Color(0x262626));
        btnOjo.setHorizontalAlignment(SwingConstants.CENTER);
        btnOjo.setPreferredSize(new Dimension(44, 40));
        btnOjo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOjo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passVisible = !passVisible;
                campoPassword.setEchoChar(passVisible ? (char) 0 : '●');
                btnOjo.setText(passVisible ? "Hide" : "Show");
            }
        });

        wrapper.add(campoPassword, BorderLayout.CENTER);
        wrapper.add(btnOjo, BorderLayout.EAST);

        wrapper.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(
                        new Color(0xA8A8A8), 1));
            }
        });
        campoPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(
                        new Color(0xA8A8A8), 1));
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));
            }
        });

        return wrapper;
    }

    // panel de confirmacion de contraseña con mismo estilo que buildPanelPassword
    private JPanel buildPanelPasswordConfirm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FONDO_CAMPO);
        wrapper.setMaximumSize(new Dimension(310, 40));
        wrapper.setPreferredSize(new Dimension(310, 40));
        wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        campoPasswordConfirm.setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 4));
        campoPasswordConfirm.setBackground(FONDO_CAMPO);
        campoPasswordConfirm.setOpaque(true);

        // placeholder visual
        campoPasswordConfirm.putClientProperty("placeholder", "Confirm password");

        JLabel btnOjo2 = new JLabel("Show");
        btnOjo2.setFont(new Font("Arial", Font.BOLD, 11));
        btnOjo2.setForeground(new Color(0x262626));
        btnOjo2.setHorizontalAlignment(SwingConstants.CENTER);
        btnOjo2.setPreferredSize(new Dimension(44, 40));
        btnOjo2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final boolean[] visible2 = {false};
        btnOjo2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                visible2[0] = !visible2[0];
                campoPasswordConfirm.setEchoChar(visible2[0] ? (char) 0 : '\u25CF');
                btnOjo2.setText(visible2[0] ? "Hide" : "Show");
            }
        });

        wrapper.add(campoPasswordConfirm, BorderLayout.CENTER);
        wrapper.add(btnOjo2, BorderLayout.EAST);

        campoPasswordConfirm.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(
                        new Color(0xA8A8A8), 1));
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));
            }
        });

        return wrapper;
    }

    private void actualizarFeedback() {
        String nombre = campoNombre.getText().trim();
        String username = campoUsername.getText().trim();
        String pass = new String(campoPassword.getPassword());
        String confirm = new String(campoPasswordConfirm.getPassword());
        String edadStr = campoEdad.getText().trim();

        String msg = null;
        Color color = ROJO;

        if (!nombre.isEmpty() && nombre.length() < 2) {
            msg = "Full name must be at least 2 characters.";
        } else if (!username.isEmpty()) {
            if (username.length() < 3) {
                msg = "Username: at least 3 characters required ("
                        + username.length() + "/3).";
            } else if (!username.matches("[a-zA-Z0-9._]+")) {
                msg = "Username: only letters, numbers, . and _ allowed.";
            } else {
                // chequear username existente en tiempo real con SwingWorker
                final String uCheck = username.toLowerCase();
                new javax.swing.SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        UsuarioStorage st = new UsuarioStorage();
                        boolean existe = st.existeUsername(uCheck);
                        st.cerrar();
                        return existe;
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                lblFeedback.setForeground(ROJO);
                                lblFeedback.setText(
                                        "<html><div style='text-align:center;width:290px;'>"
                                        + "This username isn't available. Try another."
                                        + "</div></html>");
                                btnRegistrar.setEnabled(false);
                                btnRegistrar.setBackground(AZUL_DIS);
                                btnRegistrar.repaint();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }.execute();
            }
        }

        if (msg == null && !pass.isEmpty()) {
            if (pass.length() < 6) {
                msg = "Password: at least 6 characters (" + pass.length() + "/6).";
            } else if (!pass.matches(".*[A-Z].*")) {
                msg = "Password: needs at least one uppercase letter (A-Z).";
            } else if (!pass.matches(".*[a-z].*")) {
                msg = "Password: needs at least one lowercase letter (a-z).";
            } else if (!pass.matches(".*[0-9].*")) {
                msg = "Password: needs at least one number (0-9).";
            } else if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
                msg = "Password: needs at least one special character (!@#$...).";
            } else {
                // contraseña fuerte - ahora mostrar estado de confirmacion
                if (confirm.isEmpty()) {
                    msg = "Please confirm your password.";
                } else if (!pass.equals(confirm)) {
                    msg = "Passwords do not match.";
                } else {
                    msg = "Passwords match";
                    color = VERDE;
                }
            }
        }

        if (msg == null && !edadStr.isEmpty()) {
            try {
                int edad = Integer.parseInt(edadStr);
                if (edad < 13) {
                    msg = "You must be at least 13 years old to sign up.";
                } else if (edad > 120) {
                    msg = "Enter a valid age.";
                }
            } catch (NumberFormatException ex) {
                msg = "Age must be a number.";
            }
        }

        if (msg == null || msg.isEmpty()) {
            lblFeedback.setText(" ");
        } else {
            lblFeedback.setForeground(color);
            lblFeedback.setText("<html><div style='text-align:center;width:290px;'>"
                    + msg + "</div></html>");
        }

        boolean ok = nombre.length() >= 2
                && username.length() >= 3
                && username.matches("[a-zA-Z0-9._]+")
                && pass.length() >= 6
                && pass.matches(".*[A-Z].*")
                && pass.matches(".*[a-z].*")
                && pass.matches(".*[0-9].*")
                && pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")
                && pass.equals(confirm)
                && !edadStr.isEmpty()
                && edadStr.matches("[0-9]+");

        btnRegistrar.setEnabled(ok);
        btnRegistrar.setBackground(ok ? AZUL : AZUL_DIS);
        btnRegistrar.repaint();
    }

    //logica de registro
    private void intentarRegistro() {
        String nombre = campoNombre.getText().trim();
        String username = campoUsername.getText().trim().toLowerCase();
        String pass = new String(campoPassword.getPassword());
        String edadStr = campoEdad.getText().trim();

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
            if (edad < 13 || edad > 120) {
                setFeedbackError("Enter a valid age (13+).");
                return;
            }
        } catch (NumberFormatException ex) {
            setFeedbackError("Enter a valid age.");
            return;
        }

        Genero genero = comboGenero.getSelectedIndex() == 0 ? Genero.M : Genero.F;
        TipoCuenta tipo = comboTipo.getSelectedIndex() == 0
                ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;
        try {
            UsuarioStorage st = new UsuarioStorage();
            if (st.existeUsername(username)) {
                st.cerrar();
                setFeedbackError("This username isn't available. Try another.");
                return;
            }
            String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            // copiar foto de perfil a INSTA_RAIZ/username/imagenes/ si es ruta absoluta
            String rutaFotoFinal = rutaFoto;
            FileManager.crearEstructuraUsuario(username);
            if (!rutaFoto.startsWith("avatars/") && new File(rutaFoto).isAbsolute()) {
                try {
                    String ext = rutaFoto.contains(".")
                            ? rutaFoto.substring(rutaFoto.lastIndexOf(".")) : ".png";
                    String carpeta = FileManager.getRutaImagenes(username);
                    FileManager.crearCarpeta(carpeta);
                    File destino = new File(carpeta + File.separator + "profile" + ext);
                    java.nio.file.Files.copy(new File(rutaFoto).toPath(), destino.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    rutaFotoFinal = destino.getPath();
                } catch (Exception ignored) {
                    rutaFotoFinal = rutaFoto;
                }
            }

            UsuarioRegistrado u = new UsuarioRegistrado(
                    username, pass, nombre, genero, edad,
                    fecha, EstadoCuenta.ACTIVO, tipo, rutaFotoFinal);
            st.agregar(u);
            st.cerrar();
            sembrarStickers(username);
            autoFollowDeSeedAccounts(username);
            limpiar();
            AppFrame.getInstance().iniciarSesion(u);
        } catch (Exception ex) {
            setFeedbackError("Error creating account: " + ex.getMessage());
        }
    }

    private void autoFollowDeSeedAccounts(String nuevoUsername) {
        String[] seeds = {"rafael", "fcbarcelona", "primos_unitedfc"};
        try {
            proyecto.ii.programacion.ii.storage.FollowStorage fs
                    = new proyecto.ii.programacion.ii.storage.FollowStorage();
            proyecto.ii.programacion.ii.storage.UsuarioStorage us
                    = new proyecto.ii.programacion.ii.storage.UsuarioStorage();
            for (String seed : seeds) {
                if (us.existeUsername(seed)
                        && !seed.equalsIgnoreCase(nuevoUsername)) {
                    // seeds siguen al nuevo usuario
                    fs.seguir(seed, nuevoUsername);
                    // el nuevo usuario sigue a las seeds
                    fs.seguir(nuevoUsername, seed);
                }
            }
            us.cerrar();
        } catch (Exception ignored) {
        }
    }

    private void sembrarStickers(String username) {
        String[] n = {"Feliz", "Triste", "Corazon", "Risa", "Aplauso"};
        String[] r = {"stickers/sticker_feliz.png", "stickers/sticker_triste.png",
            "stickers/sticker_corazon.png", "stickers/sticker_risa.png",
            "stickers/sticker_apluso.png"};
        try {
            StickerStorage ss = new StickerStorage(username);
            for (int i = 0; i < n.length; i++) {
                ss.agregar(new Sticker(n[i], r[i]));
            }
            ss.cerrar();
        } catch (Exception ignored) {
        }
    }

    private void seleccionarFoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // guardar la ruta absoluta temporalmente - se copiara al registrarse
            rutaFoto = fc.getSelectedFile().getAbsolutePath();
            actualizarFoto();
        }
    }

    private void actualizarFoto() {
        ImageIcon icon = Assets.getAvatarCircular(rutaFoto, 60);
        if (icon != null) {
            lblFotoPreview.setIcon(icon);
            lblFotoPreview.setText("");
        } else {
            lblFotoPreview.setIcon(null);
            lblFotoPreview.setText("👤");
            lblFotoPreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private void setFeedbackError(String m) {
        lblFeedback.setForeground(ROJO);
        lblFeedback.setText("<html><div style='text-align:center;width:290px;'>"
                + m + "</div></html>");
    }

    private void limpiar() {
        campoNombre.setText("");
        campoUsername.setText("");
        campoPassword.setText("");
        campoPasswordConfirm.setText("");
        campoEdad.setText("");
        passVisible = false;
        campoPassword.setEchoChar('●');
        rutaFoto = "avatars/default_avatar.png";
        actualizarFoto();
        lblFeedback.setText(" ");
        btnRegistrar.setEnabled(false);
        btnRegistrar.setBackground(AZUL_DIS);
        btnRegistrar.repaint();
    }

    //helpers ui
    private JPasswordField crearPasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    g.setColor(new Color(0xAAAAAA));
                    g.setFont(new Font("Arial", Font.PLAIN, 13));
                    g.drawString("Password", 10, getHeight() / 2 + 4);
                }
            }
        };
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setEchoChar('●');
        return f;
    }

    private JTextField crearCampo(String ph) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0xAAAAAA));
                    g.setFont(getFont());
                    g.drawString(ph, 10, getHeight() / 2 + 4);
                }
            }
        };
        f.setBackground(FONDO_CAMPO);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setPreferredSize(new Dimension(310, 40));
        f.setMaximumSize(new Dimension(310, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.setOpaque(true);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xA8A8A8), 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        });
        return f;
    }

    private JComboBox<String> crearCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(FONDO_CAMPO);
        cb.setFont(new Font("Arial", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(310, 36));
        cb.setMaximumSize(new Dimension(310, 36));
        cb.setBorder(BorderFactory.createLineBorder(BORDE, 1));
        return cb;
    }

    private JLabel labelSeccion(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        l.setForeground(GRIS_TEXTO);
        l.setMaximumSize(new Dimension(310, 16));
        return l;
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                FontMetrics fm = g2.getFontMetrics(getFont());
                g2.setColor(getForeground());
                g2.setFont(getFont());
                String t = getText();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(310, 44));
        btn.setMaximumSize(new Dimension(310, 44));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
