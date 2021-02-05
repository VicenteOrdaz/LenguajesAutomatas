package test;

import compiler.AnalisisSemantico;
import compiler.Analisis;
import compiler.Compiler;

public class Test {
    public static Compiler compiler;

    public static void test(String texto){
        AnalisisSemantico as = new AnalisisSemantico(()-> System.out.println("Error sem"),()-> System.out.println("Aceptado semn")) {
            @Override
            public boolean semanticAnalyze(String comp) {
                return true;
            }
        };

        compiler = new Compiler(as,()->{
            System.out.println("Sintactico acptado");
        });

        System.out.println("Analizando entrada: "+texto);
        Analisis.perfomance(texto);

        System.out.println();
        System.out.println("Error lexico: "+compiler.erroresLexicos);
        System.out.println("Error sintactico: "+compiler.erroresSintacticos);
        System.out.println("Error semantico: "+compiler.semError);
    }

    public static void main(String[] args) throws Exception
    {
       test("public static void principal()\n   fin");
    }

}