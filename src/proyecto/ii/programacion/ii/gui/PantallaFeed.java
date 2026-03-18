package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.model.Comentario;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.ComentarioStorage;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.PublicacionStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import proyecto.ii.programacion.ii.model.ListaSimple;
import java.util.Collections;
import java.util.Date;

public class PantallaFeed extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color GRIS = new Color(0x737373);

    // seed accounts que siempre aparecen en feed aunque no se les siga
    private static final String[] SEEDS = {"rafael", "fcbarcelona", "primos_unitedfc"};

    public PantallaFeed() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    @Override
    public void refrescar() {
        removeAll();

        // spinner mientras carga para que la UI no se congele
        JLabel cargando = new JLabel("Loading...");
        cargando.setFont(new Font("Arial", Font.PLAIN, 13));
        cargando.setForeground(new Color(0x737373));
        cargando.setHorizontalAlignment(SwingConstants.CENTER);
        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.add(cargando, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        revalidate();
        repaint();

        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        // carga de datos fuera del EDT para evitar lag con 2 instancias corriendo
        new javax.swing.SwingWorker<ArrayList<PostConAutor>, Void>() {
            @Override
            protected ArrayList<PostConAutor> doInBackground() {
                return cargarTimeline(sesion);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<PostConAutor> timeline = get();
                    removeAll();
                    construir(sesion, timeline);
                    revalidate();
                    repaint();
                } catch (Exception e) {
                    System.err.println("error en SwingWorker feed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void construir(UsuarioRegistrado sesion, ArrayList<PostConAutor> timeline) {

        // topbar con logo
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        topBar.setPreferredSize(new Dimension(390, 46));
        JLabel lblLogo = new JLabel("Instagram");
        Font fi = Assets.getFuenteInstagram(28f);
        if (fi.getFamily().equals("Serif")) {
            fi = new Font("Georgia", Font.ITALIC, 28);
        }
        lblLogo.setFont(fi);
        topBar.add(lblLogo, BorderLayout.WEST);

        // panel de feed con ancho forzado a 390
        JPanel feedPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(390, d.height);
            }
        };
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.WHITE);

        if (timeline.isEmpty()) {
            JLabel empty = new JLabel(
                    "<html><div style='text-align:center;color:#737373;'>"
                    + "<br><br><br>No posts yet.</div></html>");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedPanel.add(empty);
        } else {
            for (PostConAutor pa : timeline) {
                feedPanel.add(buildCard(pa.post, pa.autor));
            }
        }

        JScrollPane scroll = new JScrollPane(feedPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private ArrayList<PostConAutor> cargarTimeline(UsuarioRegistrado sesion) {
        // ListaSimple para acumular el timeline (uso real de estructura propia)
        ListaSimple<PostConAutor> timeline = new ListaSimple<>();
        ListaSimple<String> agregados = new ListaSimple<>();
        try {
            UsuarioStorage us = new UsuarioStorage();
            FollowStorage fs = new FollowStorage();

            // mis posts
            agregarPosts(timeline, sesion, sesion);
            agregados.agregar(sesion.getUsername());

            // posts de a quien sigo
            ArrayList<String> following = fs.leerLista(
                    proyecto.ii.programacion.ii.storage.FileManager
                            .getRutaFollowing(sesion.getUsername()));
            for (String uname : following) {
                if (agregados.contiene(uname)) {
                    continue;
                }
                UsuarioRegistrado autor = us.buscarPorUsername(uname);
                if (autor != null && autor.isActivo()) {
                    agregarPosts(timeline, autor, sesion);
                    agregados.agregar(uname);
                }
            }

            // seed accounts siempre en feed aunque no les hagas follow
            for (String seed : SEEDS) {
                if (agregados.contiene(seed)) {
                    continue;
                }
                if (seed.equalsIgnoreCase(sesion.getUsername())) {
                    continue;
                }
                UsuarioRegistrado seedUser = us.buscarPorUsername(seed);
                if (seedUser != null && seedUser.isActivo()) {
                    agregarPosts(timeline, seedUser, sesion);
                    agregados.agregar(seed);
                }
            }
            us.cerrar();
        } catch (IOException e) {
            // mostrar error en consola para debug en vez de ignorarlo
            System.err.println("Error loading timeline: " + e.getMessage());
            e.printStackTrace();
        }

        // convertir a ArrayList para el sort (ListaSimple no soporta Comparator directo)
        ArrayList<PostConAutor> timelineList = timeline.toArrayList();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        Collections.sort(timelineList, (a, b) -> {
            try {
                Date da = sdf.parse(a.post.getFecha() + " " + a.post.getHora());
                Date db = sdf.parse(b.post.getFecha() + " " + b.post.getHora());
                return db.compareTo(da);
            } catch (Exception ex) {
                // fallback lexico si el parse falla
                String fa = a.post.getFecha() + " " + a.post.getHora();
                String fb = b.post.getFecha() + " " + b.post.getHora();
                return fb.compareTo(fa);
            }
        });
        return timelineList;
    }

    private void agregarPosts(ListaSimple<PostConAutor> lista,
            UsuarioRegistrado autor,
            UsuarioRegistrado sesion) {
        try {
            PublicacionStorage ps = new PublicacionStorage(autor.getUsername());
            for (Publicacion p : ps.leerTodas()) {
                lista.agregar(new PostConAutor(p, autor));
            }
            ps.cerrar();
        } catch (IOException e) {
            System.err.println("Error loading posts for " + autor.getUsername()
                    + ": " + e.getMessage());
        }
    }

    private JPanel buildCard(Publicacion p, UsuarioRegistrado autor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));

        // header
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        header.setMaximumSize(new Dimension(390, 52));

        FotoCircular avatar = new FotoCircular(autor.getRutaFotoPerfil(), 34);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                irAPerfil(autor);
            }
        });

        JLabel lblUser = new JLabel("@" + autor.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        lblUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                irAPerfil(autor);
            }
        });

        JLabel lblFecha = new JLabel(p.getFecha());
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFecha.setForeground(GRIS);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.setBackground(Color.WHITE);
        izq.add(lblUser);

        header.add(avatar, BorderLayout.WEST);
        header.add(izq, BorderLayout.CENTER);
        header.add(lblFecha, BorderLayout.EAST);

        // imagen con altura proporcional al aspect ratio (sec 4.1 PDF)
        ImageIcon imgIcon = Assets.getPostImagenFeed(p.getRutaImagen());
        int imgH = imgIcon != null ? imgIcon.getIconHeight() : 390;
        JLabel lblImg = new JLabel(imgIcon);
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setPreferredSize(new Dimension(390, imgH));
        lblImg.setMaximumSize(new Dimension(390, imgH));
        lblImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // botones like/comment/share
        UsuarioRegistrado sesionLike = AppFrame.getInstance().getSesion();
        final boolean[] liked = {sesionLike != null && p.yaDioLike(sesionLike.getUsername())};
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(Color.WHITE);
        btnRow.setMaximumSize(new Dimension(390, 42));

        // mostrar corazon rojo si ya dio like
        JLabel btnLike = liked[0]
                ? iconBtn("ic_heart_filled.png", 24)
                : iconBtn("ic_heart.png", 24);
        JLabel btnComment = iconBtn("ic_comment.png", 24);
        JLabel btnShare = iconBtn("ic_share.png", 24);

        // contar comentarios del post
        int numComentarios = 0;
        try {
            ComentarioStorage cs = new ComentarioStorage(autor.getUsername());
            numComentarios = cs.contarPorPost(p.getId());
            cs.cerrar();
        } catch (IOException ignored) {
        }
        final int nComents = numComentarios;
        JLabel lblComments = new JLabel(String.valueOf(nComents));
        lblComments.setFont(new Font("Arial", Font.BOLD, 13));

        // declarado aqui para que el listener del like pueda actualizarlo
        JLabel lblLikesText = new JLabel(p.getLikes() + " likes");
        lblLikesText.setFont(new Font("Arial", Font.BOLD, 13));

        btnComment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                abrirComentarios(p, autor, lblComments);
            }
        });

        btnLike.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
                if (sesion == null) {
                    return;
                }
                if (liked[0]) {
                    // quitar like
                    p.decrementarLikes();
                    p.quitarLike(sesion.getUsername());
                    liked[0] = false;
                    ImageIcon ic = Assets.getIcon("ic_heart.png", 24);
                    if (ic != null) {
                        btnLike.setIcon(ic);
                    }
                } else {
                    // dar like - agregarLike retorna false si no hay espacio en menciones
                    if (!p.agregarLike(sesion.getUsername())) {
                        return;
                    }
                    p.incrementarLikes();
                    liked[0] = true;
                    ImageIcon ic = Assets.getIcon("ic_heart_filled.png", 24);
                    if (ic != null) {
                        btnLike.setIcon(ic);
                    }
                }
                lblLikesText.setText(p.getLikes() + " likes");
                try {
                    PublicacionStorage ps = new PublicacionStorage(autor.getUsername());
                    ps.actualizarLikesYMenciones(p.getId(), p.getLikes(), p.getMenciones());
                    ps.cerrar();
                } catch (IOException ignored) {
                }
            }
        });

        btnRow.add(btnLike);
        btnRow.add(btnComment);
        btnRow.add(lblComments);
        btnRow.add(btnShare);

        // "X likes" debajo de los botones y se actualiza al dar like
        JPanel likesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        likesRow.setBackground(Color.WHITE);
        likesRow.setMaximumSize(new Dimension(390, 20));
        likesRow.add(lblLikesText);

        // caption con hashtags
        // hashtags en linea separada para no desbordarse con hashtags largos
        JPanel captionRow = new JPanel(new BorderLayout());
        captionRow.setBackground(Color.WHITE);
        captionRow.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        captionRow.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));
        String htmlCaption = "<html><div style='width:330px;word-wrap:break-word;'><b>@"
                + autor.getUsername() + "</b>  " + esc(p.getContenido());
        if (p.getHashtags() != null && !p.getHashtags().trim().isEmpty()) {
            htmlCaption += "<br><font color='#00376B'>" + esc(p.getHashtags()) + "</font>";
        }
        htmlCaption += "</div></html>";
        JLabel lblCap = new JLabel(htmlCaption);
        lblCap.setFont(new Font("Arial", Font.PLAIN, 13));
        captionRow.add(lblCap, BorderLayout.CENTER);

        JPanel sep = new JPanel();
        sep.setBackground(BORDE);
        sep.setMaximumSize(new Dimension(390, 1));
        sep.setPreferredSize(new Dimension(390, 1));

        card.add(header);
        card.add(lblImg);
        card.add(btnRow);
        card.add(likesRow);
        card.add(captionRow);
        card.add(Box.createVerticalStrut(6));
        card.add(sep);
        return card;
    }

    private void irAPerfil(UsuarioRegistrado autor) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion != null && sesion.getUsername().equalsIgnoreCase(autor.getUsername())) {
            AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
        } else {
            AppFrame.getInstance().verPerfilAjeno(autor.getUsername());
        }
    }

    private JLabel iconBtn(String nombre, int size) {
        JLabel l = new JLabel();
        ImageIcon ic = Assets.getIcon(nombre, size);
        if (ic != null) {
            l.setIcon(ic);
        } else {
            l.setText("♥");
        }
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    // dialogo de comentarios para un post
    // permite ver comentarios existentes y agregar uno nuevo con soporte de menciones
    private void abrirComentarios(Publicacion p, UsuarioRegistrado autor,
            JLabel lblCount) {
        UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
        if (sesion == null) {
            return;
        }

        // verificar si puede comentar
        // para cuentas privadas basta con que yo siga al autor (me aceptaron el follow)
        if (!autor.isPublico()) {
            try {
                FollowStorage fs = new FollowStorage();
                boolean yoSigo = fs.sigueA(sesion.getUsername(), autor.getUsername());
                if (!yoSigo) {
                    JOptionPane.showMessageDialog(this,
                            "This account is private. Follow to comment.");
                    return;
                }
            } catch (IOException ignored) {
            }
        }

        JDialog dlg = new JDialog(AppFrame.getInstance(),
                "Comments", true);
        dlg.setSize(390, 560);
        dlg.setLocationRelativeTo(AppFrame.getInstance());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // lista de comentarios
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        lista.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        try {
            ComentarioStorage cs = new ComentarioStorage(autor.getUsername());
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
                // cargar usuarios una vez para los avatares
                java.util.HashMap<String, UsuarioRegistrado> cacheUsuarios
                        = new java.util.HashMap<>();
                try {
                    UsuarioStorage usTemp = new UsuarioStorage();
                    for (Comentario c : comentarios) {
                        if (!cacheUsuarios.containsKey(c.getUsername())) {
                            UsuarioRegistrado uComent
                                    = usTemp.buscarPorUsername(c.getUsername());
                            if (uComent != null) {
                                cacheUsuarios.put(c.getUsername(), uComent);
                            }
                        }
                    }
                    usTemp.cerrar();
                } catch (IOException ignored) {
                }

                for (Comentario c : comentarios) {
                    JPanel row = new JPanel(new BorderLayout(10, 0));
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                    row.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));

                    // avatar del comentarista
                    UsuarioRegistrado uComent = cacheUsuarios.get(c.getUsername());
                    String rutaAvatar = uComent != null
                            ? uComent.getRutaFotoPerfil() : "avatars/default_avatar.png";
                    FotoCircular avatarComent = new FotoCircular(rutaAvatar, 32);
                    avatarComent.setAlignmentY(Component.TOP_ALIGNMENT);
                    avatarComent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    avatarComent.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            UsuarioRegistrado sesionActual = AppFrame.getInstance().getSesion();
                            if (sesionActual != null && sesionActual.getUsername()
                                    .equalsIgnoreCase(c.getUsername())) {
                                AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
                            } else {
                                AppFrame.getInstance().verPerfilAjeno(c.getUsername());
                            }
                        }
                    });

                    // contenido nombre bold + texto + hora abajo en gris
                    JPanel contenidoPanel = new JPanel();
                    contenidoPanel.setLayout(new BoxLayout(contenidoPanel, BoxLayout.Y_AXIS));
                    contenidoPanel.setBackground(Color.WHITE);

                    final String uComentUsername = c.getUsername();
                    JLabel lblNombre = new JLabel("@" + uComentUsername);
                    lblNombre.setFont(new Font("Arial", Font.BOLD, 13));
                    lblNombre.setForeground(new Color(0x00376B));
                    lblNombre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    lblNombre.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            UsuarioRegistrado sesionActual = AppFrame.getInstance().getSesion();
                            if (sesionActual != null && sesionActual.getUsername()
                                    .equalsIgnoreCase(uComentUsername)) {
                                AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
                            } else {
                                AppFrame.getInstance().verPerfilAjeno(uComentUsername);
                            }
                        }
                    });

                    JLabel lblTexto = new JLabel(
                            "<html><div style='width:270px;'>" + esc(c.getContenido())
                            + "</div></html>");
                    lblTexto.setFont(new Font("Arial", Font.PLAIN, 13));
                    lblTexto.setForeground(Color.BLACK);

                    JLabel lblHora = new JLabel(c.getFecha() + " · " + c.getHora());
                    lblHora.setFont(new Font("Arial", Font.PLAIN, 11));
                    lblHora.setForeground(new Color(0x737373));

                    contenidoPanel.add(lblNombre);
                    contenidoPanel.add(Box.createVerticalStrut(2));
                    contenidoPanel.add(lblTexto);
                    contenidoPanel.add(Box.createVerticalStrut(3));
                    contenidoPanel.add(lblHora);

                    row.add(avatarComent, BorderLayout.WEST);
                    row.add(contenidoPanel, BorderLayout.CENTER);

                    // separador fino entre comentarios
                    JPanel wrapper = new JPanel();
                    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
                    wrapper.setBackground(Color.WHITE);
                    wrapper.add(row);
                    JPanel sep = new JPanel();
                    sep.setBackground(new Color(0xF0F0F0));
                    sep.setMaximumSize(new Dimension(390, 1));
                    sep.setPreferredSize(new Dimension(390, 1));
                    wrapper.add(sep);

                    lista.add(wrapper);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading comments: " + e.getMessage());
        }

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        // barra de ingreso de comentario
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
                    g.drawString("Add a comment... use @username to mention",
                            4, getHeight() / 2 + 5);
                }
            }
        };
        campo.setFont(new Font("Arial", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        JLabel btnPost = new JLabel("Post");
        btnPost.setFont(new Font("Arial", Font.BOLD, 13));
        btnPost.setForeground(new Color(0x0095F6));
        btnPost.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Runnable enviar = () -> {
            String texto = campo.getText().trim();
            if (texto.isEmpty()) {
                return;
            }
            if (texto.length() > 200) {
                JOptionPane.showMessageDialog(dlg,
                        "Comment cannot exceed 200 characters.");
                return;
            }
            String ts8 = String.valueOf(System.currentTimeMillis());
            ts8 = ts8.substring(Math.max(0, ts8.length() - 8));
            String uPart = sesion.getUsername().substring(0,
                    Math.min(8, sesion.getUsername().length()));
            String id = uPart + "_c" + ts8;
            String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String hora = new SimpleDateFormat("HH:mm").format(new Date());
            try {
                ComentarioStorage cs = new ComentarioStorage(autor.getUsername());
                cs.agregar(new Comentario(id, p.getId(),
                        sesion.getUsername(), texto, fecha, hora));
                // ahora reutiliza la misma instancia para contar y la cerramos al final
                int total = cs.contarPorPost(p.getId());
                cs.cerrar();
                campo.setText("");
                if (lblCount != null) {
                    lblCount.setText(String.valueOf(total));
                }
                dlg.dispose();
                abrirComentarios(p, autor, lblCount);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dlg, "Error posting comment.");
            }
        };

        campo.addActionListener(e -> enviar.run());
        btnPost.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                enviar.run();
            }
        });

        inputRow.add(campo, BorderLayout.CENTER);
        inputRow.add(btnPost, BorderLayout.EAST);

        main.add(scroll, BorderLayout.CENTER);
        main.add(inputRow, BorderLayout.SOUTH);
        dlg.add(main);
        dlg.setVisible(true);
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class PostConAutor {

        Publicacion post;
        UsuarioRegistrado autor;

        PostConAutor(Publicacion p, UsuarioRegistrado a) {
            post = p;
            autor = a;
        }
    }
}
