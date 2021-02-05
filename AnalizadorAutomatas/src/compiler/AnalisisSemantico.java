package compiler;

import SemanticoRecursos.Token;

public abstract class AnalisisSemantico
{
    public Runnable callBackError,callBackAceptado;
    public StringBuilder expresion;
    public Token currentToken;

    public AnalisisSemantico(Runnable callBackError,Runnable callBackAceptado)
    {
        this.callBackError=callBackError;
        this.callBackAceptado=callBackAceptado;
        expresion = new StringBuilder();
    }

    public void reiniciar(){
        expresion.setLength(0);
    }

    public abstract boolean semanticAnalyze(String comp);
}
