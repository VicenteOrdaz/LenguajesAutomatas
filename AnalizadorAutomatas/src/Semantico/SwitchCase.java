package Semantico;

import SemanticoRecursos.Tipos;

public class SwitchCase {
        public String nombreVariable, bloqueFin;
        public Tipos tipos;

        public SwitchCase(String varname, Tipos type, String blockEnd) {
            this.bloqueFin = blockEnd;
            this.nombreVariable = varname;
            this.tipos = type;
        }

}