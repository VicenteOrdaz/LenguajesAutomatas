package compiler;


import Construye_LR.CellValue;
import Construye_LR.GrammarTable;
import Construye_LR.Productions;

import java.util.Stack;

public class AnalisisSintactico {

    public Stack<String> pilaSintactico;
    public GrammarTable tableLRGrammar;
    private Runnable onAccepted;
    public boolean acepeted;

    public AnalisisSintactico(Runnable onAccepted) {
        this.onAccepted = onAccepted;
        tableLRGrammar = GrammarTable.load();
        tableLRGrammar.show();
        pilaSintactico = new Stack<>();
    }


    public void reset(){
        pilaSintactico.clear();
        pilaSintactico.push("0");
        acepeted=false;
    }


    public boolean analizar(String componente)
    {
        int edo;
        CellValue<?> cellValue;

        while(true)
        {
            edo = Integer.parseInt(pilaSintactico.peek());


            cellValue = tableLRGrammar.table_lr.get(edo).get(componente);

            if(cellValue==null){
                return false;
            }


            if (cellValue.object instanceof Integer) {

                pilaSintactico.push(componente);
                pilaSintactico.push(cellValue.object + "");
                break;

            } else {
                Productions productions = (Productions) cellValue.object;


                if(tableLRGrammar.productions.get(0).noTerminal.equals(productions.noTerminal)){
                    acepeted=true;
                    onAccepted.run();
                    return true;
                }

                for(String pr : productions.production)
                {
                    pilaSintactico.pop();

                    //Esto siempre se debria cumplir, en caso de no, entonces esta mal
                    if(pilaSintactico.peek().trim().equals(pr.trim()))
                    {
                        pilaSintactico.pop();
                    }else{
                        System.err.println("Error en el analisis semantico");
                        System.exit(-1);
                    }
                }
                edo = Integer.parseInt(pilaSintactico.peek());
                cellValue = tableLRGrammar.table_lr.get(edo).get(productions.noTerminal);

                //Deberia producir estado siempre y no produccion
                if(cellValue.object instanceof  Integer){
                    pilaSintactico.push(productions.noTerminal);
                    pilaSintactico.push(cellValue.object+"");
                }else{
                    System.exit(-1);
                }
            }
        }

        return true;
    }
}
