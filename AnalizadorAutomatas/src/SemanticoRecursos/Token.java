package SemanticoRecursos;

public enum Token {

    ID(1),
    SYMBOLO(2),
    PALABRA_RESERRVADA(3),
    LITERAL(4),
    NUMERO(5);

    private int id;
    Token(int id)
    {
        this.id=id;
    }

    public int getId() {
        return id;
    }
}
