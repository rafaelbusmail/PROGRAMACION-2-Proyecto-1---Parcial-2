package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.enums.EstadoCuenta;
import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PantallaLogin extends JPanel implements AppFrame.Refrescable {

    private static final Color AZUL = new Color(0x0095F6);
    private static final Color AZUL_DIS = new Color(0x9ACAF7);
    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color FONDO_CAMPO = new Color(0xFAFAFA);
    private static final Color GRIS_TEXTO = new Color(0x737373);
    private static final Color AZUL_LINK = new Color(0x00376B);
    private static final Color ROJO_ERROR = new Color(0xED4956);

    private JTextField campoUsuario;
    private JPasswordField campoPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private boolean passwordVisible = false;

    public PantallaLogin() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construir();
    }

    @Override
    public void refrescar() {
        limpiar();
    }

    private void construir() {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Color.WHITE);
        contenido.setBorder(BorderFactory.createEmptyBorder(70, 40, 20, 40));

        JLabel lblLogo = new JLabel("Instagram");
        Font fi = Assets.getFuenteInstagram(52f);
        if (fi.getFamily().equals("Serif")) {
            fi = new Font("Georgia", Font.ITALIC, 52);
        }
        lblLogo.setFont(fi);
        lblLogo.setForeground(Color.BLACK);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblError = new JLabel("<html><div style='text-align:center;'>&nbsp;</div></html>");
        lblError.setFont(new Font("Arial", Font.PLAIN, 12));
        lblError.setForeground(ROJO_ERROR);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(310, 36));

        campoUsuario = crearCampoTexto("Username");
        campoPassword = crearCampoPassword("Password");

        btnLogin = crearBoton("Log in", AZUL_DIS);
        btnLogin.setEnabled(false);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelPass = buildPasswordRow();
        panelPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        campoUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        KeyAdapter ka = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarBoton();
            }
        };
        campoUsuario.addKeyListener(ka);
        campoPassword.addKeyListener(ka);
        campoPassword.addActionListener(e -> intentarLogin());
        btnLogin.addActionListener(e -> intentarLogin());

        JLabel lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(new Font("Arial", Font.BOLD, 12));
        lblForgot.setForeground(AZUL_LINK);
        lblForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        contenido.add(lblLogo);
        contenido.add(Box.createVerticalStrut(32));
        contenido.add(campoUsuario);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(panelPass);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(lblError);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnLogin);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(lblForgot);
        contenido.add(Box.createVerticalStrut(28));
        contenido.add(buildSeparadorOR());
        contenido.add(Box.createVerticalGlue());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));
        bottom.add(buildPanelSignup(), BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildPasswordRow() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FONDO_CAMPO);
        wrapper.setMaximumSize(new Dimension(310, 44));
        wrapper.setPreferredSize(new Dimension(310, 44));
        wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));

        campoPassword.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 4));
        campoPassword.setBackground(FONDO_CAMPO);
        campoPassword.setOpaque(true);

        JLabel btnOjo = new JLabel("Show");
        btnOjo.setFont(new Font("Arial", Font.BOLD, 11));
        btnOjo.setForeground(new Color(0x262626));
        btnOjo.setHorizontalAlignment(SwingConstants.CENTER);
        btnOjo.setPreferredSize(new Dimension(44, 44));
        btnOjo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOjo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passwordVisible = !passwordVisible;
                campoPassword.setEchoChar(passwordVisible ? (char) 0 : '●');
                btnOjo.setText(passwordVisible ? "Hide" : "Show");
            }
        });

        campoPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(new Color(0xA8A8A8), 1));
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrapper.setBorder(BorderFactory.createLineBorder(BORDE, 1));
            }
        });

        wrapper.add(campoPassword, BorderLayout.CENTER);
        wrapper.add(btnOjo, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildSeparadorOR() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(310, 20));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        JSeparator izq = new JSeparator();
        izq.setForeground(BORDE);
        JSeparator der = new JSeparator();
        der.setForeground(BORDE);
        JLabel or = new JLabel("  OR  ");
        or.setFont(new Font("Arial", Font.BOLD, 13));
        or.setForeground(GRIS_TEXTO);
        gc.gridx = 0;
        p.add(izq, gc);
        gc.gridx = 1;
        gc.weightx = 0;
        p.add(or, gc);
        gc.gridx = 2;
        gc.weightx = 1;
        p.add(der, gc);
        return p;
    }

    private JPanel buildPanelSignup() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 14));
        p.setBackground(Color.WHITE);
        JLabel t1 = new JLabel("Don't have an account?");
        t1.setFont(new Font("Arial", Font.PLAIN, 14));
        t1.setForeground(GRIS_TEXTO);
        JLabel t2 = new JLabel("Sign up.");
        t2.setFont(new Font("Arial", Font.BOLD, 14));
        t2.setForeground(AZUL);
        t2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_REGISTRO);
            }
        });
        p.add(t1);
        p.add(t2);
        return p;
    }

    private void intentarLogin() {
        String user = campoUsuario.getText().trim();
        String pass = new String(campoPassword.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            mostrarError("Enter your username and password.");
            return;
        }
        try {
            UsuarioStorage st = new UsuarioStorage();
            UsuarioRegistrado u = st.buscarPorUsername(user);
            st.cerrar();

            if (u == null) {
                mostrarError("The username you entered doesn't belong to an account.");
                return;
            }

            // cuenta desactivada: ofrecer reactivar
            if (!u.isActivo()) {
                int resp = JOptionPane.showConfirmDialog(this,
                        "This account is deactivated.\nWould you like to reactivate it?",
                        "Account deactivated",
                        JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    if (!u.getPassword().equals(pass)) {
                        mostrarError("Sorry, your password was incorrect.");
                        return;
                    }
                    u.setEstado(EstadoCuenta.ACTIVO);
                    UsuarioStorage st2 = new UsuarioStorage();
                    st2.actualizar(u);
                    st2.cerrar();
                    limpiar();
                    AppFrame.getInstance().iniciarSesion(u);
                }
                return;
            }

            if (!u.getPassword().equals(pass)) {
                mostrarError("Sorry, your password was incorrect.");
                return;
            }
            limpiar();
            AppFrame.getInstance().iniciarSesion(u);
        } catch (Exception ex) {
            mostrarError("Connection error. Try again.");
        }
    }

    private void mostrarError(String msg) {
        lblError.setText("<html><div style='text-align:center;'>" + msg + "</div></html>");
    }

    private void actualizarBoton() {
        boolean ok = !campoUsuario.getText().trim().isEmpty()
                && campoPassword.getPassword().length > 0;
        btnLogin.setEnabled(ok);
        btnLogin.setBackground(ok ? AZUL : AZUL_DIS);
        btnLogin.repaint();
    }

    private void limpiar() {
        campoUsuario.setText("");
        campoPassword.setText("");
        lblError.setText("<html><div style='text-align:center;'>&nbsp;</div></html>");
        passwordVisible = false;
        campoPassword.setEchoChar('●');
        btnLogin.setEnabled(false);
        btnLogin.setBackground(AZUL_DIS);
        btnLogin.repaint();
    }

    private JTextField crearCampoTexto(String ph) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0xAAAAAA));
                    g.setFont(getFont());
                    g.drawString(ph, 12, getHeight() / 2 + 5);
                }
            }
        };
        f.setBackground(FONDO_CAMPO);
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.setMaximumSize(new Dimension(310, 44));
        f.setPreferredSize(new Dimension(310, 44));
        f.setOpaque(true);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xA8A8A8), 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE, 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
        });
        return f;
    }

    private JPasswordField crearCampoPassword(String ph) {
        JPasswordField f = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    g.setColor(new Color(0xAAAAAA));
                    g.setFont(new Font("Arial", Font.PLAIN, 14));
                    g.drawString(ph, 12, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setForeground(Color.BLACK);
        f.setCaretColor(Color.BLACK);
        f.setEchoChar('●');
        return f;
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
        btn.setMaximumSize(new Dimension(310, 44));
        btn.setPreferredSize(new Dimension(310, 44));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
