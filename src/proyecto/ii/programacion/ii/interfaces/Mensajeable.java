package proyecto.ii.programacion.ii.interfaces;

import proyecto.ii.programacion.ii.model.Mensaje;
import java.util.ArrayList;

public interface Mensajeable {

    void enviarMensaje(Mensaje m);

    ArrayList<Mensaje> getMensajes(String otroUsername);
}
