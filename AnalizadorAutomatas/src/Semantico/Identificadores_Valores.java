package Semantico;

public class Identificadores_Valores {

        public String nombreVariable;
        public boolean initilized=false, isConstant;
        public int idBlock;
        public Identificadores_Valores(String varName, boolean isConsntatn, int idBlock) {
            this.idBlock = idBlock;
            this.nombreVariable = varName;
            this.isConstant = isConsntatn;
        }
    }
