package Semantico;

import java.util.ArrayList;

public class BloquesDeCodigo {

        public static HijaSemantico semantico;

        public final String blockName;
        public StringBuilder code;
        public ArrayList<BloquesDeCodigo> codeBlocks;
        public BloquesDeCodigo parentBlock;
        public boolean hasFinished;
        public final int nroTabulaciones,ID_BLOCK;
        public final String blockIdentifier;

        protected BloquesDeCodigo(BloquesDeCodigo parent, String name, int nro, int id, String blockIdentifier){


            this.blockIdentifier = blockIdentifier;
            this.ID_BLOCK = id;
            this.nroTabulaciones =nro;
            blockName = name;
            parentBlock = parent;
            code = new StringBuilder();
            codeBlocks = new ArrayList<>();
            check();
        }

        private void check(){


            if(blockName.equals("principal"))
            {
                code.append("\nint main(int argc, char *argv[])\n{\n");
            }else if(blockIdentifier.equals("Mientras")){
                code.append(blockName).append(": ;\n");
            }

        }

        public void addCode(String code)
        {
            if(code.trim().isEmpty())
                return;

            BloquesDeCodigo bl = new BloquesDeCodigo(this,"normal",this.nroTabulaciones +1,ID_BLOCK,"Normal");
            bl.code.append(code);
            this.codeBlocks.add(bl);
        }

        public void addBlock(BloquesDeCodigo block){
            codeBlocks.add(block);
        }

        private String obtenerFinWhile(){
            String numeroWhile = blockName.replace("Mientras","");

            return "finMientras"+numeroWhile+": ;\n";
        }

        public BloquesDeCodigo finishBlock()
        {
            if(blockIdentifier.equals("Principal"))
            {
                if(!hasFinished){
                    getCodeBlocks();
                    code.append("\n}");
                    hasFinished=true;
                }
            }
            else{
                getCodeBlocks();

                if (blockIdentifier.equals("Mientras"))
                        code.append("\ngoto ").append(blockName).append(";\n").append(obtenerFinWhile());

                else if (blockIdentifier.equals("Caso"))
                    code.append("\ngoto ").append(semantico.pilaDeSwitch.peek().bloqueFin).append(";\n").append(blockName).append(": ;");

            }


                return this.parentBlock==null ? this : this.parentBlock;
        }

        private void getCodeBlocks()
        {
            for(BloquesDeCodigo block : codeBlocks)
            {
                code.append(block.code);
            }
        }
    }
