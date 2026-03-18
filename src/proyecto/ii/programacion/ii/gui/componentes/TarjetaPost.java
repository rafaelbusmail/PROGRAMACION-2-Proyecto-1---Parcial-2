package proyecto.ii.programacion.ii.gui.componentes;

import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class TarjetaPost extends JPanel {

    //colores insta
    private static final Color COL_TEXTO = new Color(0x000000);
    private static final Color COL_GRIS = new Color(0x737373);
    private static final Color COL_HASHTAG = new Color(0x00376B);
    private static final Color COL_SEPARADOR = new Color(0xDBDBDB);
    private static final Color COL_ROJO_LIKE = new Color(0xED4956);

    private final Publicacion post;
    private final UsuarioRegistrado autor;
    private boolean liked;
    private JLabel lblLikes;
    private JLabel btnLike;
    private Runnable likeListener;
    private Runnable perfilListener;  // click en username/avatar

    public TarjetaPost(Publicacion post, UsuarioRegistrado autor, boolean liked) {
        this.post = post;
        this.autor = autor;
        this.liked = liked;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(390, Integer.MAX_VALUE));

        construir();
    }

    public void setLikeListener(Runnable r) {
        this.likeListener = r;
    }

    public void setPerfilListener(Runnable r) {
        this.perfilListener = r;
    }

    private void construir() {
        add(buildHeader());
        add(buildImagen());
        add(buildBotones());
        add(buildLikes());
        add(buildContenido());
        add(buildSeparador());
    }

    // Header avatar + username + fecha
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.setMaximumSize(new Dimension(390, 52));

        FotoCircular avatar = new FotoCircular(autor.getRutaFotoPerfil(), 32);

        JLabel lblUser = new JLabel("@" + autor.getUsername());
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        lblUser.setForeground(COL_TEXTO);

        JLabel lblFecha = new JLabel(post.getFecha());
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFecha.setForeground(COL_GRIS);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        infoPanel.add(lblUser);
        infoPanel.add(lblFecha);

        header.add(avatar, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.CENTER);

        //Click en header para ir al perfil del autor
        MouseAdapter clickPerfil = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (perfilListener != null) {
                    perfilListener.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        };
        avatar.addMouseListener(clickPerfil);
        lblUser.addMouseListener(clickPerfil);

        return header;
    }

    // Imagen del post
    private JLabel buildImagen() {
        // ancho fijo 390px
        int ancho = 390;
        int alto = 390; // cuadrado por defecto
        if (post.tieneImagen()) {
            
        }

        ImageIcon img = Assets.getPostImagen(post.getRutaImagen(), ancho, alto);
        JLabel lblImg = new JLabel(img);
        lblImg.setMaximumSize(new Dimension(ancho, alto));
        lblImg.setPreferredSize(new Dimension(ancho, alto));
        lblImg.setMinimumSize(new Dimension(ancho, alto));
        return lblImg;
    }

    private JPanel buildBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(390, 44));

        btnLike = crearBtnIcono(liked
                ? "ic_heart_filled.png"
                : "ic_heart.png", 26);
        btnLike.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                liked = !liked;
                if (liked) {
                    post.incrementarLikes();
                    ImageIcon icon = Assets.getIcon("ic_heart_filled.png", 26);
                    btnLike.setIcon(icon != null ? icon : null);
                    if (icon == null) {
                        btnLike.setText("❤️");
                    }
                } else {
                    post.decrementarLikes();
                    ImageIcon icon = Assets.getIcon("ic_heart.png", 26);
                    btnLike.setIcon(icon != null ? icon : null);
                    if (icon == null) {
                        btnLike.setText("🤍");
                    }
                }
                actualizarLikes();
                if (likeListener != null) {
                    likeListener.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });

        JLabel btnComment = crearBtnIcono("ic_comment.png", 26);
        JLabel btnShare = crearBtnIcono("ic_share.png", 26);

        panel.add(btnLike);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(btnComment);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(btnShare);

        return panel;
    }

    // X likes
    private JPanel buildLikes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 12));
        panel.setMaximumSize(new Dimension(390, 22));

        lblLikes = new JLabel(post.getLikes() + " likes");
        lblLikes.setFont(new Font("Arial", Font.BOLD, 13));
        lblLikes.setForeground(COL_TEXTO);
        panel.add(lblLikes);
        return panel;
    }

    //contenido "username texto" + hashtags
    private JPanel buildContenido() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));
        panel.setMaximumSize(new Dimension(390, Integer.MAX_VALUE));

        // Contenido
        if (post.getContenido() != null && !post.getContenido().isEmpty()) {
            JLabel lblContenido = new JLabel(
                    "<html><b>@" + autor.getUsername() + "</b>  "
                    + post.getContenido() + "</html>");
            lblContenido.setFont(new Font("Arial", Font.PLAIN, 13));
            lblContenido.setForeground(COL_TEXTO);
            panel.add(lblContenido);
        }

        // Hashtags
        if (post.getHashtags() != null && !post.getHashtags().trim().isEmpty()) {
            JLabel lblHash = new JLabel(
                    "<html><font color='#00376B'>"
                    + post.getHashtags() + "</font></html>");
            lblHash.setFont(new Font("Arial", Font.PLAIN, 13));
            panel.add(Box.createVerticalStrut(2));
            panel.add(lblHash);
        }

        return panel;
    }

    //Separador
    private JPanel buildSeparador() {
        JPanel sep = new JPanel();
        sep.setBackground(COL_SEPARADOR);
        sep.setMaximumSize(new Dimension(390, 1));
        sep.setPreferredSize(new Dimension(390, 1));
        return sep;
    }

    private void actualizarLikes() {
        lblLikes.setText(post.getLikes() + " likes");
    }

    private JLabel crearBtnIcono(String nombreIcono, int size) {
        JLabel lbl = new JLabel();
        ImageIcon icon = Assets.getIcon(nombreIcono, size);
        if (icon != null) {
            lbl.setIcon(icon);
        } else {
            lbl.setText("♥");
        }
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return lbl;
    }
}
