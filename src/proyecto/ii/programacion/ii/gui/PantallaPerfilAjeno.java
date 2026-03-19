package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.model.Comentario;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.ComentarioStorage;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.MensajeStorage;
import proyecto.ii.programacion.ii.storage.PublicacionStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import proyecto.ii.programacion.ii.storage.FileManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PantallaPerfilAjeno extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color GRIS = new Color(0x737373);
    private static final Color NEGRO = Color.BLACK;
    private static final Color AZUL = new Color(0x0095F6);
    private static final Color AZUL_DIS = new Color(0xEFEFEF);

    public PantallaPerfilAjeno() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    @Override
    public void refrescar() {
        removeAll();
        String username = AppFrame.getInstance().getUsernameViendoPerfil();
        if (username == null) {
            return;
        }

        try {
            UsuarioStorage st = new UsuarioStorage();
            UsuarioRegistrado u = st.buscarPorUsername(username);
            st.cerrar();
            if (u == null || !u.isActivo()) {
                return;
            }
            construir(u);
        } catch (IOException e) {
            add(new JLabel("Error loading profile."), BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private void construir(UsuarioRegistrado u) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        FollowStorage fs = new FollowStorage();

        boolean yoSigo;
        int numFollowers, numFollowing;
        ArrayList<Publicacion> posts;
        try {
            yoSigo = fs.sigueA(sesion.getUsername(), u.getUsername());
            numFollowers = fs.contarFollowers(u.getUsername());
            numFollowing = fs.contarFollowing(u.getUsername());
            PublicacionStorage ps = new PublicacionStorage(u.getUsername());
            posts = ps.leerTodas();
            ps.cerrar();
        } catch (IOException e) {
            yoSigo = false;
            numFollowers = 0;
            numFollowing = 0;
            posts = new ArrayList<>();
        }

        // capturar como final para uso en lambdas
        final boolean yoSigoFinal = yoSigo;
        final boolean amigos = yoSigoFinal && esAmigo(sesion, u.getUsername());
        // puede ver posts si: publica, o si yo la sigo (me aceptaron)
        final boolean puedeVerPosts = u.isPublico() || yoSigoFinal;

        // topBar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        topBar.setPreferredSize(new Dimension(390, 46));

        JLabel btnBack = new JLabel("‹  @" + u.getUsername());
        btnBack.setFont(new Font("Arial", Font.BOLD, 16));
        btnBack.setForeground(NEGRO);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // vuelve de donde se vino
                AppFrame.getInstance().mostrarPantalla(
                        AppFrame.getInstance().getPantallaAnteriorPerfil());
            }
        });
        topBar.add(btnBack, BorderLayout.WEST);

        // scrollable content
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Color.WHITE);

        // Header avatar + stats
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        header.setBackground(Color.WHITE);
        header.setMaximumSize(new Dimension(390, 110));

        // avatar clickeable para ver foto ampliada (como en Instagram real)
        FotoCircular avatarHeader = new FotoCircular(u.getRutaFotoPerfil(), 86);
        avatarHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatarHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarFotoAmpliada(u.getRutaFotoPerfil(), u.getUsername());
            }
        });
        header.add(avatarHeader);

        JPanel stats = new JPanel(new GridLayout(1, 3, 0, 0));
        stats.setBackground(Color.WHITE);
        stats.setPreferredSize(new Dimension(240, 60));
        stats.add(buildStat(String.valueOf(posts.size()), "Posts", null));
        stats.add(buildStat(String.valueOf(numFollowers), "Followers",
                () -> verListaAjeno(u.getUsername(), false)));
        stats.add(buildStat(String.valueOf(numFollowing), "Following",
                () -> verListaAjeno(u.getUsername(), true)));
        header.add(stats);

        // Nombre
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setMaximumSize(new Dimension(390, 28));
        JLabel lblNombre = new JLabel(u.getNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(lblNombre);
        if (!u.isPublico()) {
            ImageIcon lock = Assets.getIcon("ic_lock.png", 13);
            infoPanel.add(lock != null ? new JLabel(lock) : new JLabel("🔒"));
        }

        // datos completos genero, edad, fecha registro, estado, tipo cuenta
        JPanel datosPanel = new JPanel();
        datosPanel.setLayout(new BoxLayout(datosPanel, BoxLayout.Y_AXIS));
        datosPanel.setBackground(Color.WHITE);
        datosPanel.setBorder(BorderFactory.createEmptyBorder(2, 16, 4, 16));
        datosPanel.setMaximumSize(new Dimension(390, 80));

        String generoStr = u.getGenero() == proyecto.ii.programacion.ii.enums.Genero.M
                ? "Male" : "Female";
        String tipoStr = u.isPublico() ? "Public" : "Private";
        String estadoStr = u.isActivo() ? "Active" : "Inactive";
        int edad = u.getEdad();

        JLabel lblDatos1 = new JLabel(generoStr
                + (edad > 0 ? "  ·  " + edad + " years old" : "")
                + "  ·  " + tipoStr);
        lblDatos1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDatos1.setForeground(GRIS);

        JLabel lblDatos2 = new JLabel("Joined " + u.getFechaRegistro()
                + "  ·  " + estadoStr);
        lblDatos2.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDatos2.setForeground(GRIS);

        datosPanel.add(lblDatos1);
        datosPanel.add(Box.createVerticalStrut(2));
        datosPanel.add(lblDatos2);

        // verificar si ya tenemos solicitud pendiente
        boolean tieneSolicitudPendiente = false;
        try {
            tieneSolicitudPendiente = new FollowStorage().existe(
                    FileManager.getRutaPendingFollowers(u.getUsername()),
                    sesion.getUsername());
        } catch (IOException ignored) {
        }

        final boolean[] siguiendo = {yoSigoFinal};
        final boolean[] pendiente = {tieneSolicitudPendiente};
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btnPanel.setMaximumSize(new Dimension(390, 44));

        String textoFollow = yoSigoFinal ? "Following"
                : (tieneSolicitudPendiente ? "Requested" : "Follow");
        JButton btnFollow = buildBoton(textoFollow,
                yoSigoFinal ? AZUL_DIS : AZUL,
                yoSigoFinal ? NEGRO : Color.WHITE);
        JButton btnMessage = buildBoton("Message", AZUL_DIS, NEGRO);

        btnFollow.addActionListener(e -> {
            try {
                if (siguiendo[0]) {
                    fs.dejarDeSeguir(sesion.getUsername(), u.getUsername());
                    siguiendo[0] = false;
                    btnFollow.setText("Follow");
                    btnFollow.setBackground(AZUL);
                    btnFollow.setForeground(Color.WHITE);
                } else if (pendiente[0]) {
                    fs.eliminar(FileManager.getRutaPendingFollowers(u.getUsername()),
                            sesion.getUsername());
                    pendiente[0] = false;
                    btnFollow.setText("Follow");
                    btnFollow.setBackground(AZUL);
                    btnFollow.setForeground(Color.WHITE);
                } else if (!u.isPublico()) {
                    FileManager.crearArchivo(
                            FileManager.getRutaPendingFollowers(u.getUsername()));
                    fs.agregar(FileManager.getRutaPendingFollowers(u.getUsername()),
                            sesion.getUsername());
                    pendiente[0] = true;
                    btnFollow.setText("Requested");
                    btnFollow.setBackground(AZUL_DIS);
                    btnFollow.setForeground(NEGRO);
                    JOptionPane.showMessageDialog(this,
                            "Follow request sent. The user must approve it.");
                } else {
                    fs.seguir(sesion.getUsername(), u.getUsername());
                    siguiendo[0] = true;
                    btnFollow.setText("Following");
                    btnFollow.setBackground(AZUL_DIS);
                    btnFollow.setForeground(NEGRO);
                }
                btnFollow.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error. Try again.");
            }
        });

        btnMessage.addActionListener(e -> {
            // re-leer desde disco para evitar estado desactualizado
            try {
                FollowStorage fsCheck = new FollowStorage();
                boolean yoSigoAhora = fsCheck.sigueA(sesion.getUsername(), u.getUsername());
                UsuarioStorage usCheck = new UsuarioStorage();
                UsuarioRegistrado uActual = usCheck.buscarPorUsername(u.getUsername());
                usCheck.cerrar();

                if (uActual == null || !uActual.isActivo()) {
                    JOptionPane.showMessageDialog(this, "This account is no longer available.");
                    return;
                }

                // reglamento sec 11.1: permitido si:
                // a) cuenta publica
                // b) yo sigo al privado (me aceptaron)
                // c) el otro ya me envio un mensaje primero (conversacion existente)
                boolean puedeMsg = uActual.isPublico() || yoSigoAhora;

                if (!puedeMsg) {
                    // verificar si ya existe conversacion iniciada por el otro
                    try {
                        MensajeStorage ms = new MensajeStorage(sesion.getUsername());
                        boolean existeConv = !ms.leerConversacion(u.getUsername()).isEmpty();
                        ms.cerrar();
                        puedeMsg = existeConv;
                    } catch (IOException ignored) {
                    }
                }

                if (!puedeMsg) {
                    JOptionPane.showMessageDialog(this,
                            "This account is private. Follow them to send a message.");
                    return;
                }
                AppFrame.getInstance().abrirChat(u.getUsername());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error. Try again.");
            }
        });

        btnPanel.add(btnFollow);
        btnPanel.add(btnMessage);

        // Tab bar
        JPanel tabBar = new JPanel(new GridLayout(1, 1));
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));
        tabBar.setMaximumSize(new Dimension(390, 44));
        JPanel tabGrid = new JPanel(new BorderLayout());
        tabGrid.setBackground(Color.WHITE);
        tabGrid.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, NEGRO));
        ImageIcon gridIc = Assets.getIcon("ic_grid.png", 22);
        JLabel lblGrid = gridIc != null ? new JLabel(gridIc) : new JLabel("⊞");
        lblGrid.setHorizontalAlignment(SwingConstants.CENTER);
        tabGrid.add(lblGrid, BorderLayout.CENTER);
        tabBar.add(tabGrid);

        // Grid de posts o mensaje de privacidad
        JPanel gridPanel;
        if (!puedeVerPosts) {
            gridPanel = new JPanel(new BorderLayout());
            gridPanel.setBackground(Color.WHITE);
            gridPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
            JLabel lblPriv = new JLabel(
                    "<html><div style='text-align:center;color:#737373;'>"
                    + "🔒<br><br><b>This account is private.</b><br>"
                    + "Follow to see their photos.</div></html>");
            lblPriv.setFont(new Font("Arial", Font.PLAIN, 14));
            lblPriv.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.add(lblPriv, BorderLayout.CENTER);
        } else {
            gridPanel = buildGrid(posts);
        }

        contenido.add(header);
        contenido.add(infoPanel);
        contenido.add(datosPanel);
        contenido.add(btnPanel);
        contenido.add(tabBar);
        contenido.add(gridPanel);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildGrid(ArrayList<Publicacion> posts) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        if (posts.isEmpty()) {
            JLabel l = new JLabel("<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br>No posts yet.</div></html>");
            l.setHorizontalAlignment(SwingConstants.CENTER);
            wrapper.add(l, BorderLayout.CENTER);
            return wrapper;
        }

        final int COLS = 3, GAP = 2;
        final int TAM = (390 - GAP * (COLS - 1)) / COLS;
        int filas = (int) Math.ceil((double) posts.size() / COLS);

        JPanel grid = new JPanel(new GridLayout(filas, COLS, GAP, GAP));
        grid.setBackground(new Color(0xDBDBDB));
        grid.setPreferredSize(new Dimension(390, filas * (TAM + GAP)));

        for (Publicacion p : posts) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(new Color(0xEFEFEF));
            cell.setPreferredSize(new Dimension(TAM, TAM));
            cell.setMinimumSize(new Dimension(TAM, TAM));
            cell.add(new JLabel(Assets.getPostImagen(p.getRutaImagen(), TAM, TAM)),
                    BorderLayout.CENTER);
            // agregar click para ver el post en grande
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    verPost(p);
                }
            });
            grid.add(cell);
        }
        int resto = posts.size() % COLS;
        if (resto != 0) {
            for (int i = 0; i < COLS - resto; i++) {
                JPanel v = new JPanel();
                v.setBackground(new Color(0xEFEFEF));
                v.setPreferredSize(new Dimension(TAM, TAM));
                grid.add(v);
            }
        }
        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    // dialogo de post clickeable - igual que PantallaPerfil
    private void verPost(Publicacion p) {
        UsuarioRegistrado sesionPost = AppFrame.getInstance().getSesion();

        JDialog dlg = new JDialog(AppFrame.getInstance(), true);
        dlg.setUndecorated(true);
        dlg.setSize(370, 560);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        // opacacion: glasspane semitransparente mientras el post esta visible
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        AppFrame.getInstance().setGlassPane(glass);
        glass.setVisible(true);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                glass.setVisible(false);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDE, 1));

        JLabel img = new JLabel(Assets.getPostImagen(p.getRutaImagen(), 370, 370));
        img.setPreferredSize(new Dimension(370, 370));
        img.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(img, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBackground(Color.WHITE);

        // fila de like y comentario
        final boolean[] liked = {sesionPost != null && p.yaDioLike(sesionPost.getUsername())};
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        acciones.setBackground(Color.WHITE);

        JLabel btnLikePost = new JLabel();
        ImageIcon icHeart = liked[0]
                ? Assets.getIcon("ic_heart_filled.png", 22)
                : Assets.getIcon("ic_heart.png", 22);
        if (icHeart != null) {
            btnLikePost.setIcon(icHeart);
        }
        btnLikePost.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblLikesPost = new JLabel(p.getLikes() + " likes");
        lblLikesPost.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel btnCommentPost = new JLabel();
        ImageIcon icComment = Assets.getIcon("ic_comment.png", 22);
        if (icComment != null) {
            btnCommentPost.setIcon(icComment);
        }
        btnCommentPost.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLikePost.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (sesionPost == null) {
                    return;
                }
                if (liked[0]) {
                    p.decrementarLikes();
                    p.quitarLike(sesionPost.getUsername());
                    liked[0] = false;
                    ImageIcon ic = Assets.getIcon("ic_heart.png", 22);
                    if (ic != null) {
                        btnLikePost.setIcon(ic);
                    }
                } else {
                    if (!p.agregarLike(sesionPost.getUsername())) {
                        return;
                    }
                    p.incrementarLikes();
                    liked[0] = true;
                    ImageIcon ic = Assets.getIcon("ic_heart_filled.png", 22);
                    if (ic != null) {
                        btnLikePost.setIcon(ic);
                    }
                }
                lblLikesPost.setText(p.getLikes() + " likes");
                try {
                    PublicacionStorage ps = new PublicacionStorage(p.getUsernameAutor());
                    ps.actualizarLikesYMenciones(p.getId(), p.getLikes(), p.getMenciones());
                    ps.cerrar();
                } catch (IOException ignored) {
                }
            }
        });

        btnCommentPost.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dlg.dispose();
                mostrarComentariosPost(p);
            }
        });

        acciones.add(btnLikePost);
        acciones.add(lblLikesPost);
        acciones.add(btnCommentPost);

        JPanel infoPanel = new JPanel(new BorderLayout(8, 0));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));
        JLabel info = new JLabel(
                "<html><div style='width:290px;word-wrap:break-word;'><b>@"
                + p.getUsernameAutor() + "</b>  " + esc(p.getContenido())
                + "<br><small style='color:#737373;'>" + p.getFecha()
                + "</small></div></html>");
        info.setFont(new Font("Arial", Font.PLAIN, 13));
        JLabel x = new JLabel("x");
        x.setFont(new Font("Arial", Font.BOLD, 18));
        x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        x.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dlg.dispose();
            }
        });
        infoPanel.add(info, BorderLayout.CENTER);
        infoPanel.add(x, BorderLayout.EAST);

        south.add(acciones);
        south.add(infoPanel);
        panel.add(south, BorderLayout.SOUTH);

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        panel.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });

        dlg.add(panel);
        dlg.setVisible(true);
    }

    // dialogo de comentarios para posts del perfil ajeno
    private void mostrarComentariosPost(Publicacion p) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();

        // verificar si puede comentar: publico o yo sigo al autor
        if (sesion != null) {
            try {
                UsuarioStorage usTmp = new UsuarioStorage();
                UsuarioRegistrado autor = usTmp.buscarPorUsername(p.getUsernameAutor());
                usTmp.cerrar();
                if (autor != null && !autor.isPublico()) {
                    FollowStorage fsTmp = new FollowStorage();
                    boolean yoSigo = fsTmp.sigueA(sesion.getUsername(), p.getUsernameAutor());
                    if (!yoSigo) {
                        JOptionPane.showMessageDialog(AppFrame.getInstance(),
                                "This account is private. Follow to comment.");
                        return;
                    }
                }
            } catch (IOException ignored) {
            }
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(), "Comments", true);
        dlg.setSize(390, 500);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        lista.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        try {
            ComentarioStorage cs = new ComentarioStorage(p.getUsernameAutor());
            ArrayList<Comentario> comentarios = cs.leerPorPost(p.getId());
            cs.cerrar();
            if (comentarios.isEmpty()) {
                JLabel vacio = new JLabel(
                        "<html><div style='text-align:center;color:#737373;'>"
                        + "<br><br>No comments yet. Be the first!</div></html>");
                vacio.setFont(new Font("Arial", Font.PLAIN, 13));
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                lista.add(vacio);
            } else {
                for (Comentario c : comentarios) {
                    JPanel row = new JPanel(new BorderLayout());
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    JLabel lbl = new JLabel(
                            "<html><div style='width:330px;word-wrap:break-word;'>"
                            + "<b>@" + c.getUsername() + "</b>  " + esc(c.getContenido())
                            + "<br><small style='color:#737373;'>"
                            + c.getFecha() + " " + c.getHora()
                            + "</small></div></html>");
                    lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                    row.add(lbl, BorderLayout.CENTER);
                    // row clickeable navega al perfil del comentarista
                    final String uComentUser = c.getUsername();
                    row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    lbl.setForeground(Color.BLACK);
                    row.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            UsuarioRegistrado sesionActual = AppFrame.getInstance().getSesion();
                            if (sesionActual != null && sesionActual.getUsername()
                                    .equalsIgnoreCase(uComentUser)) {
                                AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
                            } else {
                                AppFrame.getInstance().verPerfilAjeno(uComentUser);
                            }
                        }
                    });
                    lista.add(row);
                    JPanel sep = new JPanel();
                    sep.setBackground(new Color(0xF0F0F0));
                    sep.setMaximumSize(new Dimension(390, 1));
                    lista.add(sep);
                }
            }
        } catch (IOException ignored) {
        }

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        main.add(scroll, BorderLayout.CENTER);

        // barra de input para nuevo comentario si tiene permiso
        if (sesion != null) {
            JPanel inputRow = new JPanel(new BorderLayout(8, 0));
            inputRow.setBackground(Color.WHITE);
            inputRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDBDBDB)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));

            JTextField campo = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty() && !isFocusOwner()) {
                        g.setColor(new Color(0x8E8E8E));
                        g.setFont(getFont());
                        g.drawString("Add a comment...", 4, getHeight() / 2 + 5);
                    }
                }
            };
            campo.setFont(new Font("Arial", Font.PLAIN, 13));
            campo.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

            JLabel btnPost = new JLabel("Post");
            btnPost.setFont(new Font("Arial", Font.BOLD, 13));
            btnPost.setForeground(new Color(0x0095F6));
            btnPost.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            final UsuarioRegistrado sesionFinal = sesion;
            Runnable enviar = new Runnable() {
                @Override
                public void run() {
                    String texto = campo.getText().trim();
                    if (texto.isEmpty()) {
                        return;
                    }
                    if (texto.length() > 200) {
                        JOptionPane.showMessageDialog(dlg,
                                "Comment cannot exceed 200 characters.");
                        return;
                    }
                    String ts = String.valueOf(System.currentTimeMillis());
                    String tsCorto = ts.substring(Math.max(0, ts.length() - 8));
                    String uPart = sesionFinal.getUsername().substring(
                            0, Math.min(8, sesionFinal.getUsername().length()));
                    String id = uPart + "_c" + tsCorto;
                    String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                    String hora = new SimpleDateFormat("HH:mm").format(new Date());
                    try {
                        ComentarioStorage cs = new ComentarioStorage(p.getUsernameAutor());
                        cs.agregar(new proyecto.ii.programacion.ii.model.Comentario(
                                id, p.getId(), sesionFinal.getUsername(), texto, fecha, hora));
                        cs.cerrar();
                        dlg.dispose();
                        mostrarComentariosPost(p);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dlg, "Error posting comment.");
                    }
                }
            };

            campo.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enviar.run();
                }
            });
            btnPost.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    enviar.run();
                }
            });

            inputRow.add(campo, BorderLayout.CENTER);
            inputRow.add(btnPost, BorderLayout.EAST);
            main.add(inputRow, BorderLayout.SOUTH);
        }

        dlg.add(main);
        dlg.setVisible(true);
    }

    // escapa html para labels
    // muestra la foto de perfil ampliada con fondo opaco - cierra al clickear fuera
    private void mostrarFotoAmpliada(String rutaFoto, String username) {
        JDialog dlg = new JDialog(AppFrame.getInstance(), false);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        // foto ampliada circular de 260px
        FotoCircular fotoGrande = new FotoCircular(rutaFoto, 260);

        JPanel contenedor = new JPanel(new GridBagLayout());
        contenedor.setBackground(new Color(0, 0, 0, 0));
        contenedor.setOpaque(false);
        contenedor.add(fotoGrande);
        dlg.add(contenedor);
        dlg.setSize(300, 300);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        // glasspane oscuro sobre AppFrame
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        AppFrame.getInstance().setGlassPane(glass);
        glass.setVisible(true);

        // cerrar al clickear fuera del circulo (en el glasspane o fuera del dialogo)
        glass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                glass.setVisible(false);
                dlg.dispose();
            }
        });
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                glass.setVisible(false);
            }
        });

        dlg.setVisible(true);
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JPanel buildStat(String num, String label, Runnable onClick) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        if (onClick != null) {
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        JLabel n = new JLabel(num);
        n.setFont(new Font("Arial", Font.BOLD, 15));
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setForeground(GRIS);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalGlue());
        p.add(n);
        p.add(l);
        p.add(Box.createVerticalGlue());
        if (onClick != null) {
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    onClick.run();
                }
            });
        }
        return p;
    }

    // lista de followers/following del perfil ajeno
    // si cuenta privada y no eres follower, no se muestra la lista
    private void verListaAjeno(String username, boolean siguiendo) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();

        // verificar si hay permiso para ver la lista
        try {
            UsuarioStorage usTmp = new UsuarioStorage();
            UsuarioRegistrado objetivo = usTmp.buscarPorUsername(username);
            usTmp.cerrar();

            if (objetivo != null && !objetivo.isPublico()) {
                FollowStorage fsTmp = new FollowStorage();
                boolean yoSigo = sesion != null
                        && fsTmp.sigueA(sesion.getUsername(), username);
                if (!yoSigo) {
                    JOptionPane.showMessageDialog(AppFrame.getInstance(),
                            "This account is private.\nFollow them to see their "
                            + (siguiendo ? "following." : "followers."));
                    return;
                }
            }
        } catch (IOException ignored) {
        }

        ArrayList<String> lista = new ArrayList<>();
        try {
            FollowStorage fs = new FollowStorage();
            lista = siguiendo
                    ? fs.leerLista(FileManager.getRutaFollowing(username))
                    : fs.leerLista(FileManager.getRutaFollowers(username));
        } catch (IOException ignored) {
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(),
                siguiendo ? "Following" : "Followers", true);
        dlg.setSize(300, 400);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        if (lista.isEmpty()) {
            JLabel empty = new JLabel("<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br>None yet.</div></html>");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(empty);
        } else {
            try {
                UsuarioStorage us = new UsuarioStorage();
                for (String uname : lista) {
                    UsuarioRegistrado otro = us.buscarPorUsername(uname);
                    if (otro == null) {
                        continue;
                    }
                    JPanel fila = new JPanel(new BorderLayout(8, 0));
                    fila.setBackground(Color.WHITE);
                    fila.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                    JLabel lbl = new JLabel("@" + uname + "  " + otro.getNombreCompleto());
                    lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                    fila.add(lbl, BorderLayout.CENTER);
                    fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    fila.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            dlg.dispose();
                            AppFrame.getInstance().verPerfilAjeno(uname);
                        }
                    });
                    panel.add(fila);
                }
                us.cerrar();
            } catch (IOException ignored) {
            }
        }
        dlg.add(new JScrollPane(panel));
        dlg.setVisible(true);
    }

    private JButton buildBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
        btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(150, 32));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private boolean esAmigo(UsuarioRegistrado sesion, String username) {
        try {
            FollowStorage fs = new FollowStorage();
            return fs.sigueA(sesion.getUsername(), username)
                    && fs.sigueA(username, sesion.getUsername());
        } catch (IOException e) {
            return false;
        }
    }
}
