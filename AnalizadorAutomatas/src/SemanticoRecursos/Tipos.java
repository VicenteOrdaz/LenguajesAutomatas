package SemanticoRecursos;

public enum Tipos {

    TEXTO("texto",1),
    ENTERO("entero",0),
    DECIMAL("decimal",2);

    public String tipo;
    public int posicion;

    Tipos(String type,int pos) {
            this.tipo = type;
            this.posicion = pos;
        }
}
