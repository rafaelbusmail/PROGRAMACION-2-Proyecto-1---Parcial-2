package proyecto.ii.programacion.ii;

import proyecto.ii.programacion.ii.gui.AppFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> AppFrame.getInstance().iniciar());
    }
}
