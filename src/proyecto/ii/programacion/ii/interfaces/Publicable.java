package proyecto.ii.programacion.ii.interfaces;

import proyecto.ii.programacion.ii.model.Publicacion;
import java.util.ArrayList;

public interface Publicable {

    void publicar(Publicacion p);

    void eliminarPost(String postId);

    ArrayList<Publicacion> getMisPublicaciones();
}
