package proyecto.ii.programacion.ii.enums;

public enum Genero {
    M, F;

    @Override
    public String toString() {
        return this == M ? "Male" : "Female";
    }
}
