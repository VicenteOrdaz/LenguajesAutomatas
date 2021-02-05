package cargar;

import AyudaLR.GrammarTableBuilder;
import AyudaLR.LR_Generator2;
import Construye_LR.LR_Table;
import compiler.Automata;
import compiler.DefincionDeDatos;

import java.io.*;

public class Ejecutar {

    public static String pathFiles=Automata.class.getClassLoader().getResource("files").getPath();

    public static void generarCerraduras_1(){
        LR_Generator2 g =  new LR_Generator2();
        g.build();
    }

    public static void construirTablaAS_2(){
        new GrammarTableBuilder(new LR_Generator2().load(),1).build();
    }

    public static void construirGramatica_3(){
        LR_Table.build();
    }

    public static void ejecutarTablaLexico(){

        Automata.defaultBuild();
        DefincionDeDatos.build(3,2);
    }

    public static void main(String[] args) {
//        ejecutarTablaLexico();
//          generarCerraduras_1();
//        construirTablaAS_2();
//        construirGramatica_3();
    }

}
