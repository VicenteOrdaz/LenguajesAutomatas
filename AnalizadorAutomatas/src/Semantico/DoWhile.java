package Semantico;

import SemanticoRecursos.Tipos;

public class DoWhile {

    public String nombreVariable, bloqueFin;
    public Tipos tipos;

    public DoWhile(String varname, Tipos type, String blockEnd) {
        this.bloqueFin = blockEnd;
        this.nombreVariable = varname;
        this.tipos = type;
    }

}
