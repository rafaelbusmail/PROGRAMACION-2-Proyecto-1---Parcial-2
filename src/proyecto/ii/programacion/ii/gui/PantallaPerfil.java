package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.ComentarioStorage;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.PublicacionStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class PantallaPerfil extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color GRIS = new Color(0x737373);
    private static final Color NEGRO = Color.BLACK;
    private static final Color FONDO_BTN = new Color(0xEFEFEF);

    public PantallaPerfil() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    @Override
    public void refrescar() {
        removeAll();
        JLabel cargando = new JLabel("Loading...");
        cargando.setFont(new Font("Arial", Font.PLAIN, 13));
        cargando.setForeground(new Color(0x737373));
        cargando.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.add(cargando, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        revalidate();
        repaint();

        UsuarioRegistrado u = AppFrame.getInstance().getSesion();
        if (u == null) {
            return;
        }

        // carga de datos fuera del EDT para evitar lag
        new javax.swing.SwingWorker<int[], Void>() {
            ArrayList<Publicacion> posts = new ArrayList<>();

            @Override
            protected int[] doInBackground() throws Exception {
                PublicacionStorage ps = new PublicacionStorage(u.getUsername());
                posts = ps.leerTodas();
                ps.cerrar();
                FollowStorage fs = new FollowStorage();
                int followers = fs.contarFollowers(u.getUsername());
                int following = fs.contarFollowing(u.getUsername());
                return new int[]{followers, following};
            }

            @Override
            protected void done() {
                try {
                    int[] counts = get();
                    removeAll();
                    construir(u, posts, counts[0], counts[1]);
                    revalidate();
                    repaint();
                } catch (Exception e) {
                    System.err.println("error en SwingWorker perfil: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void construir(UsuarioRegistrado u, ArrayList<Publicacion> posts,
            int followers, int following) {

        add(buildTopBar(u), BorderLayout.NORTH);

        // contenido con ancho fijo 390 para que el grid siempre quede centrado
        JPanel contenido = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(390, super.getPreferredSize().height);
            }
        };
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Color.WHITE);

        final int nPosts = posts.size(), nFollowers = followers, nFollowing = following;
        contenido.add(buildHeader(u, nPosts, nFollowers, nFollowing));
        contenido.add(buildInfo(u));
        contenido.add(buildBotones(u));
        contenido.add(buildTabBar());
        contenido.add(buildGrid(posts));

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        // forzar ancho del viewport igual al contenido
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildTopBar(UsuarioRegistrado u) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        bar.setPreferredSize(new Dimension(390, 46));

        JLabel lblUser = new JLabel("@" + u.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.setBackground(Color.WHITE);
        izq.add(lblUser);

        // si privado y hay solicitudes pendientes, mostrar notificacion
        if (!u.isPublico()) {
            try {
                FollowStorage fs = new FollowStorage();
                int pendientes = fs.leerLista(
                        proyecto.ii.programacion.ii.storage.FileManager
                                .getRutaPendingFollowers(u.getUsername())).size();
                if (pendientes > 0) {
                    JLabel notif = new JLabel(pendientes + " follow request"
                            + (pendientes > 1 ? "s" : ""));
                    notif.setFont(new Font("Arial", Font.PLAIN, 12));
                    notif.setForeground(new Color(0x0095F6));
                    notif.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    notif.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            mostrarSolicitudes(u);
                        }
                    });
                    izq.add(notif);
                }
            } catch (IOException ignored) {
            }
        }

        JLabel lblMenu = new JLabel("≡");
        lblMenu.setFont(new Font("Arial", Font.BOLD, 22));
        lblMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenu(lblMenu, u);
            }
        });

        bar.add(izq, BorderLayout.WEST);
        bar.add(lblMenu, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildHeader(UsuarioRegistrado u, int nPosts,
            int nFollowers, int nFollowing) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(390, 110));

        // avatar clickeable para cambiar foto de perfil
        FotoCircular avatar = new FotoCircular(u.getRutaFotoPerfil(), 86);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.setToolTipText("Tap to change profile photo");
        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cambiarFotoPerfil(u);
            }
        });
        p.add(avatar);

        JPanel stats = new JPanel(new GridLayout(1, 3, 0, 0));
        stats.setBackground(Color.WHITE);
        stats.setPreferredSize(new Dimension(240, 60));
        stats.add(statPanel(String.valueOf(nPosts), "Posts", null));
        // followers y following son clickeables para ver lista
        stats.add(statPanel(String.valueOf(nFollowers), "Followers",
                () -> verLista(u.getUsername(), false)));
        stats.add(statPanel(String.valueOf(nFollowing), "Following",
                () -> verLista(u.getUsername(), true)));
        p.add(stats);
        return p;
    }

    private JPanel buildInfo(UsuarioRegistrado u) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(390, 30));
        JLabel nombre = new JLabel(u.getNombreCompleto());
        nombre.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(nombre);
        if (!u.isPublico()) {
            ImageIcon lock = Assets.getIcon("ic_lock.png", 14);
            p.add(lock != null ? new JLabel(lock) : new JLabel("🔒"));
        }
        return p;
    }

    private JPanel buildBotones(UsuarioRegistrado u) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        p.setMaximumSize(new Dimension(390, 44));
        JButton edit = btn("Edit profile");
        edit.addActionListener(e -> editarPerfil(u));
        p.add(edit);
        p.add(btn("Share profile"));
        return p;
    }

    private JPanel buildTabBar() {
        JPanel tab = new JPanel(new GridLayout(1, 1));
        tab.setBackground(Color.WHITE);
        tab.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));
        tab.setMaximumSize(new Dimension(390, 44));
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, NEGRO));
        ImageIcon ic = Assets.getIcon("ic_grid.png", 22);
        JLabel lbl = ic != null ? new JLabel(ic) : new JLabel("⊞");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(lbl, BorderLayout.CENTER);
        tab.add(cell);
        return tab;
    }

    // grid 3 columnas centrado con celdas cuadradas
    private JPanel buildGrid(ArrayList<Publicacion> posts) {
        // wrapper con ancho fijo 390 y alineacion LEFT para que no flote
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));
        wrapper.setPreferredSize(new Dimension(390,
                posts.isEmpty() ? 100 : (int) Math.ceil(posts.size() / 3.0) * 130));

        if (posts.isEmpty()) {
            JLabel l = new JLabel(
                    "<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br>No posts yet.</div></html>");
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.PLAIN, 14));
            wrapper.add(l, BorderLayout.CENTER);
            return wrapper;
        }

        final int COLS = 3, GAP = 2;
        final int TAM = (390 - GAP * (COLS - 1)) / COLS; // 128px
        int filas = (int) Math.ceil((double) posts.size() / COLS);

        JPanel grid = new JPanel(new GridLayout(filas, COLS, GAP, GAP));
        grid.setBackground(new Color(0xDBDBDB));
        grid.setPreferredSize(new Dimension(390, filas * (TAM + GAP)));

        for (Publicacion p : posts) {
            JPanel cell = buildCelda(p, TAM);
            grid.add(cell);
        }
        // rellenar celdas vacias en ultima fila
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

    private JPanel buildCelda(Publicacion p, int tam) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new Color(0xEFEFEF));
        cell.setPreferredSize(new Dimension(tam, tam));
        cell.setMinimumSize(new Dimension(tam, tam));
        JLabel img = new JLabel(Assets.getPostImagen(p.getRutaImagen(), tam, tam));
        img.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(img, BorderLayout.CENTER);
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // mouseReleased para mayor responsividad 
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                verPost(p);
            }
        });
        return cell;
    }

    // dialogo de post cerrable con X, Escape y click fuera
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

    // dialogo de comentarios invocable desde verPost
    private void mostrarComentariosPost(Publicacion p) {
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
            java.util.ArrayList<proyecto.ii.programacion.ii.model.Comentario> comentarios
                    = cs.leerPorPost(p.getId());
            cs.cerrar();
            if (comentarios.isEmpty()) {
                JLabel vacio = new JLabel(
                        "<html><div style='text-align:center;color:#737373;'>"
                        + "<br><br>No comments yet.</div></html>");
                vacio.setFont(new Font("Arial", Font.PLAIN, 13));
                vacio.setHorizontalAlignment(SwingConstants.CENTER);
                vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                lista.add(vacio);
            } else {
                for (proyecto.ii.programacion.ii.model.Comentario c : comentarios) {
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
                    //row clickeable navega al perfil del comentarista
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

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDBDBDB)));
        JButton btnCerrar = new JButton("Close");
        btnCerrar.addActionListener(e -> dlg.dispose());
        bar.add(btnCerrar);
        main.add(bar, BorderLayout.SOUTH);
        dlg.add(main);
        dlg.setVisible(true);
    }

    // stat panel clickeable para ver lista de followers/following
    private JPanel statPanel(String num, String label, Runnable onClick) {
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

    // muestra lista de followers o following en un dialogo
    // si es followers permite eliminar a alguien con consecuencias inmediatas
    private void verLista(String username, boolean siguiendo) {
        ArrayList<String> lista = new ArrayList<>();
        try {
            FollowStorage fs = new FollowStorage();
            lista = siguiendo
                    ? fs.leerLista(proyecto.ii.programacion.ii.storage.FileManager.getRutaFollowing(username))
                    : fs.leerLista(proyecto.ii.programacion.ii.storage.FileManager.getRutaFollowers(username));
        } catch (IOException ignored) {
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(),
                siguiendo ? "Following" : "Followers", true);
        dlg.setSize(300, 420);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        if (lista.isEmpty()) {
            JLabel l = new JLabel("<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br>None yet.</div></html>");
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(l);
        } else {
            try {
                UsuarioStorage us = new UsuarioStorage();
                for (String uname : new ArrayList<>(lista)) {
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
                    lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    lbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            dlg.dispose();
                            AppFrame.getInstance().verPerfilAjeno(uname);
                        }
                    });
                    fila.add(lbl, BorderLayout.CENTER);

                    // boton remove en followers, boton unfollow en following
                    if (!siguiendo) {
                        JLabel btnRemove = new JLabel("Remove");
                        btnRemove.setFont(new Font("Arial", Font.BOLD, 11));
                        btnRemove.setForeground(new Color(0xED4956));
                        btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        btnRemove.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
                                String msg = "Remove @" + uname + " from your followers?";
                                if (sesion != null && !sesion.isPublico()) {
                                    msg += "\nThey will no longer be able to see your private posts.";
                                }
                                if (JOptionPane.showConfirmDialog(dlg, msg,
                                        "Remove follower",
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                    try {
                                        FollowStorage fs = new FollowStorage();
                                        fs.dejarDeSeguir(uname, username);
                                        fila.setVisible(false);
                                        panel.revalidate();
                                        panel.repaint();
                                        refrescar();
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(dlg,
                                                "Error removing follower.");
                                    }
                                }
                            }
                        });
                        fila.add(btnRemove, BorderLayout.EAST);
                    } else {
                        // boton Unfollow en la lista de following
                        JLabel btnUnfollow = new JLabel("Unfollow");
                        btnUnfollow.setFont(new Font("Arial", Font.BOLD, 11));
                        btnUnfollow.setForeground(new Color(0xED4956));
                        btnUnfollow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        btnUnfollow.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (JOptionPane.showConfirmDialog(dlg,
                                        "Unfollow @" + uname + "?",
                                        "Unfollow",
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                    try {
                                        // yo (username) dejo de seguir a uname
                                        FollowStorage fs = new FollowStorage();
                                        fs.dejarDeSeguir(username, uname);
                                        fila.setVisible(false);
                                        panel.revalidate();
                                        panel.repaint();
                                        refrescar();
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(dlg,
                                                "Error unfollowing.");
                                    }
                                }
                            }
                        });
                        fila.add(btnUnfollow, BorderLayout.EAST);
                    }

                    panel.add(fila);
                }
                us.cerrar();
            } catch (IOException ignored) {
            }
        }

        JScrollPane spLista = new JScrollPane(panel);
        spLista.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dlg.add(spLista);
        dlg.setVisible(true);
    }

    private JButton btn(String texto) {
        JButton b = new JButton(texto) {
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
        b.setBackground(FONDO_BTN);
        b.setForeground(NEGRO);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(150, 32));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void mostrarMenu(JLabel origen, UsuarioRegistrado u) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem priv = new JMenuItem(
                u.isPublico() ? "Switch to Private" : "Switch to Public");
        priv.addActionListener(e -> {
            try {
                u.setTipoCuenta(u.isPublico()
                        ? proyecto.ii.programacion.ii.enums.TipoCuenta.PRIVADA
                        : proyecto.ii.programacion.ii.enums.TipoCuenta.PUBLICA);
                UsuarioStorage st = new UsuarioStorage();
                st.actualizar(u);
                st.cerrar();
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating account.");
            }
        });

        // importar stickers personales (PDF sec 12)
        JMenuItem impSticker = new JMenuItem("Import sticker");
        impSticker.addActionListener(e -> importarSticker(u));

        JMenuItem menciones = new JMenuItem("Posts where I'm mentioned");
        menciones.addActionListener(e -> verMenciones(u));

        JMenuItem desact = new JMenuItem("Deactivate account");
        desact.setForeground(new Color(0xED4956));
        desact.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Deactivate your account?",
                    "Deactivate", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    u.setEstado(proyecto.ii.programacion.ii.enums.EstadoCuenta.INACTIVO);
                    UsuarioStorage st = new UsuarioStorage();
                    st.actualizar(u);
                    st.cerrar();
                    AppFrame.getInstance().cerrarSesion();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deactivating.");
                }
            }
        });

        JMenuItem logout = new JMenuItem("Log out");
        logout.addActionListener(e -> AppFrame.getInstance().cerrarSesion());

        menu.add(priv);
        menu.addSeparator();
        menu.add(impSticker);
        menu.addSeparator();
        menu.add(menciones);
        menu.addSeparator();
        menu.add(desact);
        menu.addSeparator();
        menu.add(logout);
        menu.show(origen, 0, origen.getHeight());
    }

    // importar sticker: copia el archivo a stickers_personales/ y lo registra en stickers.ins
    private void importarSticker(UsuarioRegistrado u) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select sticker image");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File origen = fc.getSelectedFile();
        String nombre = origen.getName().replaceFirst("[.][^.]+$", ""); // sin extension

        try {
            // crear carpeta stickers_personales si no existe
            String carpeta = proyecto.ii.programacion.ii.storage.FileManager
                    .getRutaStickersPersonales(u.getUsername());
            new File(carpeta).mkdirs();

            // copiar archivo
            File destino = new File(carpeta + File.separator + origen.getName());
            java.nio.file.Files.copy(origen.toPath(), destino.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // registrar en stickers.ins
            proyecto.ii.programacion.ii.storage.StickerStorage ss
                    = new proyecto.ii.programacion.ii.storage.StickerStorage(u.getUsername());
            ss.agregar(new proyecto.ii.programacion.ii.model.Sticker(
                    nombre, destino.getAbsolutePath()));
            ss.cerrar();

            JOptionPane.showMessageDialog(this,
                    "Sticker \"" + nombre + "\" imported successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error importing sticker: " + ex.getMessage());
        }
    }

    // cambia la foto de perfil - se llama al tocar el avatar
    private void cambiarFotoPerfil(UsuarioRegistrado u) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select profile photo");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File origen = fc.getSelectedFile();
        String rutaFinal = origen.getAbsolutePath();

        // copiar la foto al directorio imagenes/ del usuario
        try {
            String ext = origen.getName().contains(".")
                    ? origen.getName().substring(origen.getName().lastIndexOf(".")) : ".png";
            String carpeta = proyecto.ii.programacion.ii.storage.FileManager
                    .getRutaImagenes(u.getUsername());
            proyecto.ii.programacion.ii.storage.FileManager.crearCarpeta(carpeta);
            File destino = new File(carpeta + File.separator + "profile" + ext);
            java.nio.file.Files.copy(origen.toPath(), destino.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            rutaFinal = destino.getPath();
        } catch (Exception ignored) {
            // fallback a ruta absoluta si la copia falla
        }

        u.setRutaFotoPerfil(rutaFinal);
        try {
            UsuarioStorage st = new UsuarioStorage();
            st.actualizar(u);
            st.cerrar();
            refrescar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving photo.");
        }
    }

    private void editarPerfil(UsuarioRegistrado u) {
        JDialog dlg = new JDialog(AppFrame.getInstance(), "Edit Profile", true);
        dlg.setSize(420, 460);
        dlg.setLocationRelativeTo(AppFrame.getInstance());
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new java.awt.Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfNombre = new JTextField(u.getNombreCompleto(), 20);
        JTextField tfUsername = new JTextField(u.getUsername(), 20);
        JPasswordField tfPass = new JPasswordField(20);
        JPasswordField tfConfirm = new JPasswordField(20);
        final boolean[] passVisible = {false};

        JLabel lblFb = new JLabel("<html><div style='width:300px;word-wrap:break-word;'>&nbsp;</div></html>");
        lblFb.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFb.setForeground(new Color(0xED4956));

        JLabel lblHint = new JLabel(
                "<html><small style='color:#737373'>Leave blank to keep current password</small></html>");

        // feedback en tiempo real: valida password Y confirmacion juntas
        // tambien valida que sea diferente a la contraseña actual
        javax.swing.event.DocumentListener dlPass = new javax.swing.event.DocumentListener() {
            void actualizar() {
                String p = new String(tfPass.getPassword()).trim();
                String c = new String(tfConfirm.getPassword()).trim();
                if (p.isEmpty() && c.isEmpty()) {
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>&nbsp;</div></html>");
                    return;
                }
                // validar que sea diferente a la contraseña actual
                if (!p.isEmpty() && p.equals(u.getPassword())) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>"
                            + "New password must be different from current.</div></html>");
                    return;
                }
                String err = validarPassword(p);
                if (err != null) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>" + err + "</div></html>");
                } else if (c.isEmpty()) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>Please confirm your password.</div></html>");
                } else if (!p.equals(c)) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>Passwords do not match.</div></html>");
                } else {
                    lblFb.setForeground(new Color(0x2ECC71));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>Passwords match</div></html>");
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }
        };
        tfPass.getDocument().addDocumentListener(dlPass);
        tfConfirm.getDocument().addDocumentListener(dlPass);

        // panel de password con boton Show/Hide integrado
        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.add(tfPass, BorderLayout.CENTER);
        JLabel btnOjoPass = new JLabel("Show");
        btnOjoPass.setFont(new Font("Arial", Font.BOLD, 11));
        btnOjoPass.setForeground(new Color(0x262626));
        btnOjoPass.setPreferredSize(new Dimension(44, 28));
        btnOjoPass.setHorizontalAlignment(SwingConstants.CENTER);
        btnOjoPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOjoPass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                passVisible[0] = !passVisible[0];
                tfPass.setEchoChar(passVisible[0] ? (char) 0 : '\u25CF');
                tfConfirm.setEchoChar(passVisible[0] ? (char) 0 : '\u25CF');
                btnOjoPass.setText(passVisible[0] ? "Hide" : "Show");
            }
        });
        passWrapper.add(btnOjoPass, BorderLayout.EAST);

        // validacion de username en tiempo real
        tfUsername.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void chequear() {
                String nu = tfUsername.getText().trim().toLowerCase();
                if (nu.isEmpty() || nu.equals(u.getUsername().toLowerCase())) {
                    // sin cambio o vacio - limpiar feedback de username
                    return;
                }
                if (!nu.matches("[a-zA-Z0-9._]+") || nu.length() < 3) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;'>Username: min 3 chars, only letters/numbers/._</div></html>");
                    return;
                }
                new javax.swing.SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        UsuarioStorage st = new UsuarioStorage();
                        boolean existe = st.existeUsername(nu);
                        st.cerrar();
                        return existe;
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                lblFb.setForeground(new Color(0xED4956));
                                lblFb.setText("<html><div style='width:300px;'>Username not available.</div></html>");
                            } else {
                                lblFb.setForeground(new Color(0x2ECC71));
                                lblFb.setText("<html><div style='width:300px;'>Username available!</div></html>");
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }.execute();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                chequear();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                chequear();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                chequear();
            }
        });

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        form.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(tfUsername, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        form.add(new JLabel("Full name:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(tfNombre, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        form.add(new JLabel("New password:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(passWrapper, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        form.add(new JLabel("Confirm:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(tfConfirm, gc);

        gc.gridx = 1;
        gc.gridy = 4;
        form.add(lblHint, gc);
        gc.gridx = 1;
        gc.gridy = 5;
        form.add(lblFb, gc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnOk = new JButton("Save");
        btnOk.setBackground(new Color(0x0095F6));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnOk.addActionListener(e -> {
            String nuevoUsername = tfUsername.getText().trim().toLowerCase();
            String n = tfNombre.getText().trim();
            String p = new String(tfPass.getPassword()).trim();
            String c = new String(tfConfirm.getPassword()).trim();
            String usernameAnterior = u.getUsername();

            // validar username
            if (nuevoUsername.isEmpty()) {
                lblFb.setForeground(new Color(0xED4956));
                lblFb.setText("<html><div style='width:300px;'>Username cannot be empty.</div></html>");
                return;
            }
            if (nuevoUsername.length() < 3 || !nuevoUsername.matches("[a-zA-Z0-9._]+")) {
                lblFb.setForeground(new Color(0xED4956));
                lblFb.setText("<html><div style='width:300px;'>Username: min 3 chars, only letters/numbers/._</div></html>");
                return;
            }
            if (n.isEmpty()) {
                lblFb.setForeground(new Color(0xED4956));
                lblFb.setText("<html><div style='width:300px;'>Full name cannot be empty.</div></html>");
                return;
            }
            if (!p.isEmpty()) {
                if (p.equals(u.getPassword())) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;'>New password must be different from current.</div></html>");
                    return;
                }
                String err = validarPassword(p);
                if (err != null) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;word-wrap:break-word;'>" + err + "</div></html>");
                    return;
                }
                if (!p.equals(c)) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;'>Passwords do not match.</div></html>");
                    return;
                }
                u.setPassword(p);
            }

            u.setNombreCompleto(n);
            final boolean cambiaUsername = !nuevoUsername.equals(usernameAnterior.toLowerCase());

            // si cambia el username, verificar que no exista antes de guardar
            if (cambiaUsername) {
                try {
                    UsuarioStorage stCheck = new UsuarioStorage();
                    if (stCheck.existeUsername(nuevoUsername)) {
                        stCheck.cerrar();
                        lblFb.setForeground(new Color(0xED4956));
                        lblFb.setText("<html><div style='width:300px;'>Username not available.</div></html>");
                        return;
                    }
                    stCheck.cerrar();
                } catch (Exception ex) {
                    lblFb.setForeground(new Color(0xED4956));
                    lblFb.setText("<html><div style='width:300px;'>Error checking username.</div></html>");
                    return;
                }
                u.setUsername(nuevoUsername);
            }

            try {
                UsuarioStorage st = new UsuarioStorage();
                if (cambiaUsername) {
                    st.actualizarConUsernameAnterior(usernameAnterior, u);
                } else {
                    st.actualizar(u);
                }
                st.cerrar();

                // si cambio el username, renombrar carpeta y propagar cambio a todos los archivos
                if (cambiaUsername) {
                    boolean renombrado = proyecto.ii.programacion.ii.storage.FileManager
                            .renombrarCarpetaUsuario(usernameAnterior, nuevoUsername);
                    if (!renombrado) {
                        lblFb.setForeground(new Color(0xED4956));
                        lblFb.setText("<html><div style='width:300px;'>Error renaming user folder.</div></html>");
                        return;
                    }

                    // actualizar la ruta de foto de perfil con ambos posibles separadores
                    // (Windows usa \ pero a veces las rutas tienen /)
                    String rutaFoto = u.getRutaFotoPerfil();
                    if (rutaFoto != null) {
                        String rutaNueva = rutaFoto;
                        // intentar con / primero, luego con \
                        String pat1 = "/" + usernameAnterior + "/";
                        String pat2 = "\\" + usernameAnterior + "\\";
                        if (rutaNueva.contains(pat1)) {
                            rutaNueva = rutaNueva.replace(pat1, "/" + nuevoUsername + "/");
                        } else if (rutaNueva.contains(pat2)) {
                            rutaNueva = rutaNueva.replace(pat2, "\\" + nuevoUsername + "\\");
                        }
                        if (!rutaNueva.equals(rutaFoto)) {
                            u.setRutaFotoPerfil(rutaNueva);
                        }
                    }

                    // propagar el cambio de username a followers, following, inbox, comentarios y posts
                    try {
                        UsuarioStorage.propagarCambioUsername(usernameAnterior, nuevoUsername);
                    } catch (Exception propEx) {
                        System.err.println("Warning: propagacion parcial: " + propEx.getMessage());
                    }

                    // guardar la ruta de foto actualizada en users.ins
                    try {
                        UsuarioStorage stFoto = new UsuarioStorage();
                        stFoto.actualizarConUsernameAnterior(nuevoUsername, u);
                        stFoto.cerrar();
                    } catch (Exception ignored) {
                    }

                    // recargar el usuario desde disco para que todo este sincronizado
                    try {
                        UsuarioStorage stReload = new UsuarioStorage();
                        UsuarioRegistrado uActualizado = stReload.buscarPorUsername(nuevoUsername);
                        stReload.cerrar();
                        if (uActualizado != null) {
                            AppFrame.getInstance().setSesion(uActualizado);
                        } else {
                            AppFrame.getInstance().setSesion(u);
                        }
                    } catch (Exception ignored) {
                        AppFrame.getInstance().setSesion(u);
                    }

                    // forzar refrescar el feed para que los posts usen las rutas nuevas
                    java.awt.Component feedComp = AppFrame.getInstance()
                            .getPantallaPorNombre(AppFrame.PANTALLA_FEED);
                    if (feedComp instanceof AppFrame.Refrescable) {
                        ((AppFrame.Refrescable) feedComp).refrescar();
                    }
                }
                dlg.dispose();
                refrescar();
            } catch (Exception ex) {
                lblFb.setForeground(new Color(0xED4956));
                lblFb.setText("<html><div style='width:300px;'>Error saving changes.</div></html>");
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnOk);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.getRootPane().setDefaultButton(btnOk);
        dlg.setVisible(true);
    }

    // mismas reglas que en registro - consistencia
    private String validarPassword(String p) {
        if (p.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (!p.matches(".*[A-Z].*")) {
            return "Password needs at least one uppercase letter.";
        }
        if (!p.matches(".*[a-z].*")) {
            return "Password needs at least one lowercase letter.";
        }
        if (!p.matches(".*[0-9].*")) {
            return "Password needs at least one number.";
        }
        if (!p.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
            return "Password needs at least one special character.";
        }
        return null;
    }

    // dialogo para aceptar o rechazar solicitudes de follow
    private void mostrarSolicitudes(UsuarioRegistrado u) {
        ArrayList<String> pendientes = new ArrayList<>();
        try {
            pendientes = new FollowStorage().leerLista(
                    proyecto.ii.programacion.ii.storage.FileManager
                            .getRutaPendingFollowers(u.getUsername()));
        } catch (IOException ignored) {
        }

        if (pendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending follow requests.");
            return;
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(), "Follow Requests", true);
        dlg.setSize(340, 400);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        for (String sol : new ArrayList<>(pendientes)) {
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setBackground(Color.WHITE);
            fila.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            JLabel nombre = new JLabel("@" + sol);
            nombre.setFont(new Font("Arial", Font.BOLD, 13));

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            btns.setBackground(Color.WHITE);

            JButton accept = new JButton("Confirm");
            accept.setBackground(new Color(0x0095F6));
            accept.setForeground(Color.WHITE);
            accept.setFont(new Font("Arial", Font.BOLD, 12));
            accept.setBorderPainted(false);
            accept.setFocusPainted(false);
            accept.addActionListener(e -> {
                try {
                    FollowStorage fs = new FollowStorage();
                    // agregar sol a mis followers (el acepta la solicitud)
                    fs.agregar(proyecto.ii.programacion.ii.storage.FileManager
                            .getRutaFollowers(u.getUsername()), sol);
                    // agregar yo a su following (ellos ahora me siguen)
                    fs.agregar(proyecto.ii.programacion.ii.storage.FileManager
                            .getRutaFollowing(sol), u.getUsername());
                    // eliminar de pendientes
                    fs.eliminar(proyecto.ii.programacion.ii.storage.FileManager
                            .getRutaPendingFollowers(u.getUsername()), sol);
                    fila.setVisible(false);
                    refrescar();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dlg, "Error confirming.");
                }
            });

            JButton deny = new JButton("Delete");
            deny.setBackground(FONDO_BTN);
            deny.setForeground(NEGRO);
            deny.setFont(new Font("Arial", Font.BOLD, 12));
            deny.setBorderPainted(false);
            deny.setFocusPainted(false);
            deny.addActionListener(e -> {
                try {
                    new FollowStorage().eliminar(
                            proyecto.ii.programacion.ii.storage.FileManager
                                    .getRutaPendingFollowers(u.getUsername()), sol);
                    fila.setVisible(false);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dlg, "Error deleting.");
                }
            });

            btns.add(accept);
            btns.add(deny);
            fila.add(nombre, BorderLayout.CENTER);
            fila.add(btns, BorderLayout.EAST);
            panel.add(fila);
        }

        JScrollPane spSolicitudes = new JScrollPane(panel);
        spSolicitudes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dlg.add(spSolicitudes);
        dlg.setVisible(true);
    }

    private void verMenciones(UsuarioRegistrado u) {
        ArrayList<Publicacion> lista = new ArrayList<>();
        java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();
        try {
            UsuarioStorage us = new UsuarioStorage();
            for (UsuarioRegistrado otro : us.leerTodos()) {
                if (!otro.isActivo()) {
                    continue;
                }

                PublicacionStorage ps = new PublicacionStorage(otro.getUsername());
                ArrayList<Publicacion> postsOtro = ps.leerTodas();
                ps.cerrar();

                for (Publicacion p : postsOtro) {
                    // buscar mencion en el campo menciones del post
                    if (p.mencionaA(u.getUsername()) && ids.add(p.getId())) {
                        lista.add(p);
                    }
                    //buscar tambien en comentarios del post
                    if (!ids.contains(p.getId())) {
                        try {
                            ComentarioStorage cs = new ComentarioStorage(otro.getUsername());
                            for (proyecto.ii.programacion.ii.model.Comentario c
                                    : cs.leerPorPost(p.getId())) {
                                if (c.getContenido().toLowerCase()
                                        .contains("@" + u.getUsername().toLowerCase())) {
                                    if (ids.add(p.getId())) {
                                        lista.add(p);
                                    }
                                    break;
                                }
                            }
                            cs.cerrar();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
            us.cerrar();
        } catch (IOException ignored) {
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(), "Mentioned in", true);
        dlg.setSize(380, 500);
        dlg.setLocationRelativeTo(AppFrame.getInstance());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        if (lista.isEmpty()) {
            JLabel l = new JLabel(
                    "<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br>No posts mention you yet.</div></html>");
            l.setFont(new Font("Arial", Font.PLAIN, 14));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(l);
        } else {
            for (Publicacion p : lista) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // miniatura del post - 44px para que quepa dentro del dialogo de 380px
                ImageIcon thumb = Assets.getPostImagen(p.getRutaImagen(), 44, 44);
                if (thumb != null) {
                    JLabel imgLbl = new JLabel(thumb);
                    imgLbl.setPreferredSize(new Dimension(44, 44));
                    row.add(imgLbl, BorderLayout.EAST);
                }

                JLabel lbl = new JLabel(
                        "<html><div style='width:270px;word-wrap:break-word;'><b>@"
                        + p.getUsernameAutor()
                        + "</b>  " + esc(p.getContenido())
                        + "<br><small style='color:#737373;'>" + p.getFecha()
                        + "</small></div></html>");
                lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                row.add(lbl, BorderLayout.CENTER);

                //click lleva al perfil del autor del post
                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        dlg.dispose();
                        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
                        if (sesion != null && sesion.getUsername()
                                .equalsIgnoreCase(p.getUsernameAutor())) {
                            AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
                        } else {
                            AppFrame.getInstance().verPerfilAjeno(p.getUsernameAutor());
                        }
                    }
                });
                panel.add(row);
            }
        }
        JScrollPane scrollMenc = new JScrollPane(panel);
        scrollMenc.setBorder(BorderFactory.createEmptyBorder());
        scrollMenc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMenc.getVerticalScrollBar().setUnitIncrement(12);
        dlg.add(scrollMenc);
        dlg.setVisible(true);
    }

    // escapa html para mostrar en JLabel
    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
