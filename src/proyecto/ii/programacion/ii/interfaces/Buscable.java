package proyecto.ii.programacion.ii.interfaces;

import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import proyecto.ii.programacion.ii.model.Publicacion;
import java.util.ArrayList;

public interface Buscable {

    ArrayList<UsuarioRegistrado> buscarPorUsername(String query);

    ArrayList<Publicacion> buscarPorHashtag(String hashtag);
}
