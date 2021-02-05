package Semantico;

import SemanticoRecursos.Tipos;

public class MacroConstant {

        public String nombreVariable;
        public String valor;
        public Tipos tipo;

        public MacroConstant(String varName, String value, Tipos type) {
            this.nombreVariable = varName;
            this.valor = value;
            this.tipo = type;
        }


    }