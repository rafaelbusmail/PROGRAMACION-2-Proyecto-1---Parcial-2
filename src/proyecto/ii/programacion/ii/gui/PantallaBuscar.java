package proyecto.ii.programacion.ii.gui;

import proyecto.ii.programacion.ii.gui.componentes.Assets;
import proyecto.ii.programacion.ii.gui.componentes.FotoCircular;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.storage.FollowStorage;
import proyecto.ii.programacion.ii.storage.PublicacionStorage;
import proyecto.ii.programacion.ii.storage.UsuarioStorage;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class PantallaBuscar extends JPanel implements AppFrame.Refrescable {

    private static final Color BORDE = new Color(0xDBDBDB);
    private static final Color GRIS = new Color(0x737373);
    private static final Color FONDO_BUSQ = new Color(0xEFEFEF);

    private JTextField campoBusqueda;
    private JPanel panelResultados;

    public PantallaBuscar() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construir();
    }

    @Override
    public void refrescar() {
        campoBusqueda.setText("");
        panelResultados.removeAll();
        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private void construir() {
        //topbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        topBar.setPreferredSize(new Dimension(390, 54));

        campoBusqueda = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0x8E8E8E));
                    g.setFont(getFont());
                    g.drawString("🔍  Search users or #hashtags", 12, getHeight() / 2 + 5);
                }
            }
        };
        campoBusqueda.setBackground(FONDO_BUSQ);
        campoBusqueda.setFont(new Font("Arial", Font.PLAIN, 14));
        campoBusqueda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FONDO_BUSQ, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        campoBusqueda.setOpaque(true);

        // Bordes redondeados simulados con background
        JPanel wrapBusq = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FONDO_BUSQ);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        wrapBusq.setOpaque(false);
        wrapBusq.add(campoBusqueda, BorderLayout.CENTER);
        topBar.add(wrapBusq, BorderLayout.CENTER);

        // panel de resultados
        panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        // Listener en tiempo real
        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscar();
            }
        });

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void buscar() {
        String query = campoBusqueda.getText().trim();
        panelResultados.removeAll();
        panelResultados.revalidate();
        panelResultados.repaint();

        if (query.isEmpty()) {
            return;
        }

        // carga fuera del EDT para no laguear mientras escribe
        final String q = query;
        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (q.startsWith("#")) {
                    buscarHashtag(q.substring(1));
                } else {
                    buscarUsuarios(q);
                }
                return null;
            }

            @Override
            protected void done() {
                panelResultados.revalidate();
                panelResultados.repaint();
            }
        }.execute();
    }

    private void buscarUsuarios(String query) {
        try {
            UsuarioStorage st = new UsuarioStorage();
            ArrayList<UsuarioRegistrado> resultados = st.buscarParcial(query);
            st.cerrar();

            UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();

            if (resultados.isEmpty()) {
                panelResultados.add(lblNoResultados("No users found for \"" + query + "\""));
                return;
            }

            for (UsuarioRegistrado u : resultados) {
                if (sesion != null && u.getUsername()
                        .equalsIgnoreCase(sesion.getUsername())) {
                    continue;
                }
                panelResultados.add(buildFilaUsuario(u));
            }
        } catch (IOException e) {
            panelResultados.add(lblNoResultados("Error searching. Try again."));
        }
    }

    private void buscarHashtag(String tag) {
        try {
            UsuarioStorage us = new UsuarioStorage();
            ArrayList<UsuarioRegistrado> todos = us.leerTodos();
            us.cerrar();

            UsuarioRegistrado sesion = AppFrame.getInstance().getSesion();
            FollowStorage fs = new FollowStorage();

            LinkedHashSet<String> postIds = new LinkedHashSet<>();
            // mapa de postId -> autor para saber a quien pertenece cada post al hacer click
            java.util.HashMap<String, UsuarioRegistrado> autorPorPost = new java.util.HashMap<>();
            ArrayList<Publicacion> resultados = new ArrayList<>();

            for (UsuarioRegistrado u : todos) {
                if (!u.isActivo()) {
                    continue;
                }

                // filtrar posts de cuentas privadas que no seguimos
                if (!u.isPublico()) {
                    boolean yoSigo = sesion != null
                            && fs.sigueA(sesion.getUsername(), u.getUsername());
                    boolean esMia = sesion != null
                            && sesion.getUsername().equalsIgnoreCase(u.getUsername());
                    if (!yoSigo && !esMia) {
                        continue;
                    }
                }

                PublicacionStorage ps = new PublicacionStorage(u.getUsername());
                for (Publicacion p : ps.buscarPorHashtag(tag)) {
                    if (!postIds.contains(p.getId())) {
                        postIds.add(p.getId());
                        resultados.add(p);
                        autorPorPost.put(p.getId(), u);
                    }
                }
                ps.cerrar();
            }

            if (resultados.isEmpty()) {
                panelResultados.add(lblNoResultados("No posts found for #" + tag));
                return;
            }

            JLabel lblTitulo = new JLabel("  Posts with #" + tag);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 13));
            lblTitulo.setForeground(Color.BLACK);
            lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
            panelResultados.add(lblTitulo);

            JPanel grid = new JPanel(new GridLayout(0, 3, 2, 2));
            grid.setBackground(Color.WHITE);
            grid.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);

            int tam = (390 - 4) / 3;
            for (Publicacion p : resultados) {
                final UsuarioRegistrado autorPost = autorPorPost.get(p.getId());
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBackground(new Color(0xEFEFEF));
                cell.setPreferredSize(new Dimension(tam, tam));
                cell.add(new JLabel(Assets.getPostImagen(p.getRutaImagen(), tam, tam)),
                        BorderLayout.CENTER);
                // celdas clickeables navegan al perfil del autor del post
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (autorPost == null) {
                            return;
                        }
                        if (sesion != null && sesion.getUsername()
                                .equalsIgnoreCase(autorPost.getUsername())) {
                            AppFrame.getInstance().mostrarPantalla(AppFrame.PANTALLA_PERFIL);
                        } else {
                            AppFrame.getInstance().verPerfilAjeno(autorPost.getUsername());
                        }
                    }
                });
                grid.add(cell);
            }
            panelResultados.add(grid);

        } catch (IOException e) {
            panelResultados.add(lblNoResultados("Error searching hashtag."));
        }
    }

    private JPanel buildFilaUsuario(UsuarioRegistrado u) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(Color.WHITE);
        fila.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fila.setMaximumSize(new Dimension(390, 62));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        FotoCircular foto = new FotoCircular(u.getRutaFotoPerfil(), 44);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel lblUser = new JLabel("@" + u.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        lblUser.setForeground(Color.BLACK);

        JLabel lblNombre = new JLabel(u.getNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNombre.setForeground(GRIS);

        // Candado si privado
        JPanel topInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topInfo.setBackground(Color.WHITE);
        topInfo.add(lblUser);
        if (!u.isPublico()) {
            ImageIcon lock = Assets.getIcon("ic_lock.png", 12);
            JLabel lck = lock != null ? new JLabel(lock) : new JLabel("🔒");
            lck.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
            topInfo.add(lck);
        }

        info.add(topInfo);
        info.add(lblNombre);

        fila.add(foto, BorderLayout.WEST);
        fila.add(info, BorderLayout.CENTER);

        // Click → ir al perfil ajeno
        MouseAdapter click = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                AppFrame.getInstance().verPerfilAjeno(u.getUsername());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                fila.setBackground(new Color(0xFAFAFA));
                info.setBackground(new Color(0xFAFAFA));
                topInfo.setBackground(new Color(0xFAFAFA));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fila.setBackground(Color.WHITE);
                info.setBackground(Color.WHITE);
                topInfo.setBackground(Color.WHITE);
            }
        };
        fila.addMouseListener(click);
        foto.addMouseListener(click);

        return fila;
    }

    private JLabel lblNoResultados(String msg) {
        JLabel l = new JLabel("<html><div style='text-align:center;color:#737373;'>"
                + "<br><br>" + msg + "</div></html>");
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(350, 80));
        return l;
    }
}
