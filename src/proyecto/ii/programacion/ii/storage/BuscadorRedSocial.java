package proyecto.ii.programacion.ii.storage;

import proyecto.ii.programacion.ii.interfaces.Buscable;
import proyecto.ii.programacion.ii.model.Publicacion;
import proyecto.ii.programacion.ii.model.UsuarioRegistrado;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

// implementa Buscable punto unico de busqueda para usuarios y hashtags
public class BuscadorRedSocial implements Buscable {

    @Override
    public ArrayList<UsuarioRegistrado> buscarPorUsername(String query) {
        try {
            UsuarioStorage us = new UsuarioStorage();
            ArrayList<UsuarioRegistrado> r = us.buscarParcial(query);
            us.cerrar();
            return r;
        } catch (IOException e) {
            System.err.println("buscarPorUsername error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<Publicacion> buscarPorHashtag(String hashtag) {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        try {
            UsuarioStorage us = new UsuarioStorage();
            ArrayList<UsuarioRegistrado> todos = us.leerTodos();
            us.cerrar();
            for (UsuarioRegistrado u : todos) {
                if (!u.isActivo()) {
                    continue;
                }
                PublicacionStorage ps = new PublicacionStorage(u.getUsername());
                for (Publicacion p : ps.buscarPorHashtag(hashtag)) {
                    if (ids.add(p.getId())) {
                        resultados.add(p);
                    }
                }
                ps.cerrar();
            }
        } catch (IOException e) {
            System.err.println("buscarPorHashtag error: " + e.getMessage());
        }
        return resultados;
    }
}
