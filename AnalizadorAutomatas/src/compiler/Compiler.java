package compiler;

import SemanticoRecursos.Token;

import javax.swing.*;

public class Compiler
{
    public AnalisisLexico analisisLexico;
    public AnalisisSintactico analisisSintactico;
    private static final String cadena_correcta = "OK";
    private String value;
    public StringBuilder erroresLexicos, erroresSintacticos;
    public AnalisisSemantico analisisSemantico;
    public String semError;


    public Compiler(AnalisisSemantico analisisSemantico,Runnable sintaticoAceptado)
    {
        this.analisisSemantico = analisisSemantico;
        erroresLexicos = new StringBuilder();
        erroresSintacticos = new StringBuilder();
        analisisLexico = new AnalisisLexico(this::previousTask,this::currentTask,this::endTask);
        analisisSintactico = new AnalisisSintactico(sintaticoAceptado);
        Analisis.setLexico(analisisLexico);
    }

    private void previousTask()
    {

        semError="";
        analisisSintactico.reset();
        analisisSemantico.reiniciar();
        erroresSintacticos.setLength(0);
        erroresLexicos.setLength(0);
        analisisLexico.setIndexOf(-1);
        analisisLexico.seguirAnalisis =true;
        analisisLexico.setLineaAct(0);


    }


    private void currentTask(){
        if(analisisLexico.getCurrentToken().trim().isEmpty())
            return;
        if(DefincionDeDatos.isSpecialWord(analisisLexico.getCurrentToken()))
        {
            analisisSemantico.currentToken = Token.PALABRA_RESERRVADA;

            if(!(analisisLexico.seguirAnalisis = analisisSintactico.analizar(analisisLexico.getCurrentToken())))
            {
                erroresSintacticos.append(String.format("Error sintatico en linea: %d en el token: `%s`\n\n",
                        analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));
            }else{
                analisisLexico.seguirAnalisis = analisisSemantico.semanticAnalyze(analisisLexico.getCurrentToken());
            }

        }
        else if(DefincionDeDatos.isSymbol(analisisLexico.getCurrentToken(), true))
        {
            analisisSemantico.currentToken = Token.SYMBOLO;

            if(!(analisisLexico.seguirAnalisis = analisisSintactico.analizar(analisisLexico.getCurrentToken())))
            {
                erroresSintacticos.append(String.format("Error sintatico en linea: %d en el token: `%s`\n\n",
                        analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));

            }else{
                analisisLexico.seguirAnalisis = analisisSemantico.semanticAnalyze(analisisLexico.getCurrentToken());
            }
        }
        else if((value= AnalisisLexico.isString(analisisLexico.getCurrentToken()))!=null)
        {
            if(value.equalsIgnoreCase(cadena_correcta))
            {
                analisisSemantico.currentToken = Token.LITERAL;
                if(!(analisisLexico.seguirAnalisis = analisisSintactico.analizar("literal")))
                {
                    erroresSintacticos.append(String.format("Error sintatico en linea: %d en el token: `%s`\n\n",
                            analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));
                }else{

                    analisisLexico.seguirAnalisis = analisisSemantico.semanticAnalyze(analisisLexico.getCurrentToken());
                }
            }else{
                erroresLexicos.append(String.format("Error en linea: %d en el token: `%s` => %s\n\n",
                        analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken(),value));
            }



        }
        else if((value= AnalisisLexico.isNum(analisisLexico.getCurrentToken()))!=null)
        {
            if(value.equalsIgnoreCase(cadena_correcta))
            {
                analisisSemantico.currentToken = Token.NUMERO;
                if(!(analisisLexico.seguirAnalisis = analisisSintactico.analizar("numero")))
                {
                    erroresSintacticos.append(String.format("Error sintatico en linea: %d en el token: `%s`\n\n",
                            analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));
                }else{
                    analisisLexico.seguirAnalisis = analisisSemantico.semanticAnalyze(analisisLexico.getCurrentToken());
                }

            }else{
                erroresLexicos.append(String.format("Error en linea: %d en el token: `%s` => %s\n\n",
                        analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken(),value));
            }


        }
        else if(AnalisisLexico.isIdentifier(analisisLexico.getCurrentToken()))
        {
            analisisSemantico.currentToken = Token.ID;
            if(!(analisisLexico.seguirAnalisis = analisisSintactico.analizar("id")))
            {
                erroresSintacticos.append(String.format("Error sintatico en linea: %d en el token: `%s`\n\n",
                        analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));
            }else{
                analisisLexico.seguirAnalisis = analisisSemantico.semanticAnalyze(analisisLexico.getCurrentToken());
            }

        }
        else{
            erroresLexicos.append(String.format("Error en linea: %d en el token: `%s`\n\n",
                    analisisLexico.getCurrentLine(), analisisLexico.getCurrentToken()));
        }
    }


    private void endTask(){

        if(analisisLexico.seguirAnalisis)
        {
            analisisSintactico.analizar("$");

            if(!analisisSintactico.acepeted) {
                erroresSintacticos.append(String.format("Linea %d: El token final: %s no es valido", analisisLexico.getCurrentLine(),
                        analisisLexico.getCurrentToken()));
            }

        }else{
            if(analisisSintactico.acepeted) {
                analisisSemantico.callBackError.run();
            }
        }
    }
}