package Construye_LR;



import cargar.Ejecutar;
import compiler.Serializa;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GrammarTable implements  Serializable{
    public ArrayList<LinkedHashMap<String,CellValue>> table_lr;

    public ArrayList<Productions> productions;

    public transient static String path= Ejecutar.pathFiles+"/grammarLR.data";

    protected GrammarTable()
    {
        productions = new ArrayList<>();
        table_lr = new ArrayList<>();
    }

    //Solo para LR_Table
    protected Productions getProduction(String prod)
    {
        prod=prod.substring(1);
        return productions.get(Integer.parseInt(prod+""));
    }

    protected void finishProductions(){
        String nt = productions.get(0).noTerminal;
        productions.add(0,new Productions(nt+"'",new String[]{nt}));
    }

    public void show()
    {
        for(LinkedHashMap<String,CellValue> values : table_lr)
        {
//            System.out.println(values);
        }
    }

    public void save(){
        Serializa.saveObject(this,new File(path));
    }

    public static GrammarTable load(){
        return (GrammarTable) Serializa.writeObject(Ejecutar.class.getResourceAsStream
                ("/files/grammarLR.data"));

    }
}
