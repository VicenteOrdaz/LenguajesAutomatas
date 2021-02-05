package Semantico;

import SemanticoRecursos.Tipos;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TraductoCodigoC {


    private BloquesDeCodigo block,lastBlock;
    private StringBuilder codeLine,codeMacros,codeTempLine;
    private int definesCantidad;
    private HijaSemantico semRef;

    protected TraductoCodigoC(HijaSemantico ref)
    {
        semRef  = ref;
        BloquesDeCodigo.semantico = ref;
        codeLine = new StringBuilder();
        codeMacros = new StringBuilder();
        codeTempLine = new StringBuilder();
        block = null;
    }

    public String getCurrentIdBlockName(){
        return block.blockIdentifier;
    }

    public String getCurrentBlockName(){
        return block.blockName;
    }

    public static String obtenerVariableDeTipo(String type)
    {
        switch (type) {
            case "texto":
                return "$cadenas";

            case "entero":
                return "$enteros";

            case "decimal":
                return "$decimales";

            default:
                return null;
        }
    }

    public String addConstant(String value)
    {
        definesCantidad++;
        String var="$var"+ definesCantidad;
        codeMacros.append("\n#define ").append(var).append(" ").append(value).append("\n");

        return var;
    }

    public String getCode(){

        try {
            return "#include <stdio.h>\n" +
                    codeMacros + block.code;
        }catch(NullPointerException ex){
            return "";
        }
    }

    public void finishBlock(String id){
        lastBlock = block;
        block = block.finishBlock();
    }

    private int currentIdBlock;

    public int getCurrentIdBlock(){
        return block.ID_BLOCK;
    }

    public void addBlock(String blockname,String idName)
    {
        if(block==null)
        {
            block = new BloquesDeCodigo(null,blockname,0,currentIdBlock++,idName);
            lastBlock = null;
        }
        else{
            BloquesDeCodigo aux = block;
            block = new BloquesDeCodigo(aux,blockname,aux.nroTabulaciones +1,currentIdBlock++,idName);
            aux.addBlock(block);
        }
        codeLine.setLength(0);
    }

    public BloquesDeCodigo getLastBlock(){
        return lastBlock;
    }

    public void addSwitch()
    {
        BloquesDeCodigo aux = block;
        block = new BloquesDeCodigo(aux,"switch",aux.nroTabulaciones +1,aux.ID_BLOCK,"Switch");
        aux.addBlock(block);
    }

    public void agregaBloqueMientras(){
        BloquesDeCodigo aux = block;
        block = new BloquesDeCodigo(aux,siguienteMientras(),aux.nroTabulaciones +1,aux.ID_BLOCK,"Mientras");
        aux.addBlock(block);
    }

    public void addCase()
    {
        BloquesDeCodigo aux = block;
        block = new BloquesDeCodigo(aux, siguienteCaso(),aux.nroTabulaciones +1,aux.ID_BLOCK,"Caso");
        aux.addBlock(block);
    }

    public void addDefault(){
        BloquesDeCodigo aux = block;
        block = new BloquesDeCodigo(aux,"default",aux.nroTabulaciones +1,aux.ID_BLOCK,"Default");
        aux.addBlock(block);
    }

    public static String getEquivalent(String comp)
    {
        switch (comp)
        {
            case "texto":
                return "char ";

            case "entero":
                return "int ";
            case "decimal":
                return "float ";

            case "leer":
                return "scanf";

            default:
                return comp;
        }
    }

    private int mientrasCant =-1;
    public String siguienteMientras(){
        mientrasCant++;
        return mientrasCant ==0 ? "Mientras" : "Mientras"+ mientrasCant;
    }

    private int caseCant=-1;
    private String currentCase;
    public String siguienteCaso(){
        caseCant++;

        if(caseCant==0){
            currentCase = "Caso";
        }
        else
        {
            currentCase = "Caso"+caseCant;
        }

        return currentCase;
    }

    private int endSwitchCant=-1;
    public String siguientesFinSwitch(){
        endSwitchCant++;
        return endSwitchCant==0 ? "finSwitch" :"finSwitch"+endSwitchCant;

    }

    private int finWhile=-1;
    public String siguienteFinMientras(){
        finWhile++;
        return finWhile==0 ? "finMientras" :"finMientras"+finWhile;

    }

    public String getCurrentCase(){
        return currentCase;
    }

    public void addCode()
    {
        block.addCode(codeLine.toString());
        codeLine.setLength(0);
    }

//    public String getCurrentTabulation(){
//        return block.tabulaciones.toString()+"\t";
//    }

    public TraductoCodigoC appendCode(String comp){
        if(comp.equals("entero")){
            comp="int ";
        }else if(comp.equals("~")){
            comp="!=";
        }
//        if(DataDefinition.isSpecialWord(comp))
        String eq = getEquivalent(comp);
        codeLine.append(eq);
        return this;
    }

    public TraductoCodigoC appendTemporalCode(String comp)
    {
        if(comp.equals("entero")){
            comp="int ";
        }else if(comp.equals("~")){
            comp="!=";
        }
        codeTempLine.append(comp);
        return this;
    }

    public void toCString(boolean logicExp)
    {
        if(!logicExp) {
            if (codeTempLine.length() > 0)
                calculateVariables();
        }else{
            checkLogicVariables();
        }
    }

    private static final String[] symbols = {"==","<=",">=","<",">","~"};

    private void checkLogicVariables(){
        String[] lines = codeTempLine.toString().split("\n");
        String[] tks;
        codeTempLine.setLength(0);
        for(String line : lines)
        {
            tks = line.split(" ");


            //Si el mañao de la linea es 3  o 5, se vreficiara las expresiones
            //para en caso de haber variables string, cambie el codigo
            if(tks.length==5 || tks.length==3) {

                if(tks.length==3){
                    //si es se cumple es porque se estaba haciendo una negeacion: iVar = !iVar
                    if(tks[2].startsWith("!")) {
                        codeTempLine.append(line).append("\n");
                        continue;
                    }

                    String res = evaluaExpresionNormal(tks[2]);
                    codeTempLine.append(tks[0]).append(" = ").append(res).append(";\n");
                }
                else{
                    String res = evaluaExpresionNormal(tks[2]).replace(";","");
                    String res1 = evaluaExpresionNormal(tks[4]).replace(";","");
                    codeTempLine.append(tks[0]).append(" = ").append(res).append(" ").append(tks[3]).append(" ").append(res1).append(";\n");
                }
            }else{
                codeTempLine.append(line).append("\n");
            }
        }
    }

    public String pasarALectura(String id, String type){
        if(type.equals("texto"))
        {
            String value = "\"%s\"";
            return String.format("%s = (char *) malloc(sizeof (char));\nscanf(%s,%s);",id, value,id);
        }
        else{
            return String.format("scanf(\"%s\",&%s);",getModouleEquiv(type),id);
        }
    }

    public String convertToPrintf(String litcad,String[][] args)
    {
        if(args==null || args.length==0){
            return String.format("printf(%s);",litcad);
        }else{
            return "printf(".concat(reducirCadena(new StringBuilder(litcad),args).toString()).concat(");");
        }
    }

    public StringBuilder reducirCadena(StringBuilder cad, String[][] argsAndTypes){

        for(String[] typeArg : argsAndTypes)
        {
            String mod = getModouleEquiv(typeArg[1]);

            if (mod==null) throw new IllegalArgumentException("The type is not correct.");

            typeArg[1] = mod;
        }

        String num="";
        int openIndex=-1,closeIndex,i=0;
        StringBuilder  parameters = new StringBuilder();

        while (i != cad.length()) {

            if (cad.charAt(i) == '{') {
                openIndex = i++;
            } else if (cad.charAt(i) == '}') {

                if (openIndex == -1) {
                    i++;
                    continue;
                }

                closeIndex = i;

                if (openIndex + 1 < closeIndex) {
                    AtomicInteger numInt = new AtomicInteger();
                    if (esNumeroPositivo(num, numInt) && numInt.get() < argsAndTypes.length) {
                        int aux = openIndex;

                        while (aux <= closeIndex) {
                            cad.deleteCharAt(openIndex);
                            aux++;
                            i--;
                        }

                        i += 3;
                        cad.insert(openIndex, argsAndTypes[numInt.get()][1]);
                        parameters.append(argsAndTypes[numInt.get()][0]).append(",");
                    }

                } else i++;
                num = "";
                openIndex = -1;

            } else {
                if (openIndex != -1) {
                    num = num.concat(cad.charAt(i) + "");
                }

                i++;
            }
        }

        if(parameters.length()>0)
            parameters.deleteCharAt(parameters.length()-1); //Borra la ,

        return parameters.length()==0 ? cad :cad.append(", ").append(parameters);
    }


    private boolean esNumeroPositivo(String cad, AtomicInteger num){

        cad=cad.replace(" ","");
        try{
            num.set(Integer.parseInt(cad));

            return num.get() >= 0;

        }catch(NumberFormatException ex){
            return false;
        }

    }

    private String getModouleEquiv(String type)
    {
        return type.equals("texto") ? "%s" : type.equals("entero") ? "%d" : type.equals("decimal") ? "%f" : null;
    }

    public String convertPrintlnToPf(String cad,String type){

        String typMod = type.equals("texto") ? "%s" : type.equals("entero") ? "%d" :
                type.equals("decimal") ? "%f" : null;

        if(typMod==null)  throw new IllegalArgumentException("The type given is not correct.");


        return String.format("printf(\"%s\\n\",%s);",typMod,cad);

    }

    private String evaluaExpresionNormal(String exp)
    {
        String firstVar, secondVar,symbol=null;

        for(String sym : symbols)
        {
            if(exp.contains(sym))
            {
                symbol = sym;
                break;
            }
//            throw new IllegalArgumentException("CodeCreator.checkLogicVariables: The expression doesnt contain an logic symbol");
        }

        if(symbol==null){
            //Se puede dar el caso que la expresion sea iVar || x<=10;
            return exp;
//            throw new NullPointerException("CodeCreator.checkLogicVariables: The var symbol is null.");
        }

        String[] subs = exp.split(symbol);

        firstVar = subs[0];
        secondVar = subs[1].replace(";","");


        Tipos type1,type2;

        type1  = semRef.getTypeOf(firstVar);

        type2  = semRef.getTypeOf(secondVar);

        //Se supon que si un tipo string, el otro lo debe ser, en caso de que no, entonces algo esta mal
        //al validar las expresiones logicas anterioremente
        if ((type1== Tipos.TEXTO && type2!= Tipos.TEXTO)
            || (type1 != Tipos.TEXTO && type2== Tipos.TEXTO)){
            throw new IllegalArgumentException("Error: Tipos diferentes el crear el codigo");
        }else{
            if(type1 == Tipos.TEXTO)
            {
                return getCLogicEquivalent(symbol,firstVar,secondVar);
            }else{
                if(symbol.equals("~"))
                  exp=exp.replace("~","!=");
               return exp;
            }
        }
    }

    private String getCLogicEquivalent(String symbol,String var1, String var2){
        if(symbol.equals("~"))
            symbol = "!=";
        return String.format("strcmp(%s,%s) %s 0",var1,var2,symbol);
    }

    private void calculateVariables()
    {
        String [] lines = codeTempLine.toString().split("\n");

        boolean isEqual;
        StringBuilder strlens = new StringBuilder();
        StringBuilder vars = new StringBuilder();

        for(String line : lines)
        {
            isEqual=false;
            for(String tk : line.split(" "))
            {
                if(!isEqual)
                {
                    isEqual = tk.equals("=");
                }else{

                    if(tk.equals("+") || tk.equals(";"))
                        continue;

                    if(tk.equals(semRef.arreglosTiposVars[1])) {
                        continue;
                    }

                    vars.append(tk).append(" ");
                    if(tk.startsWith("$")){
                        strlens.append("+strlen(").append(tk).append(")");
                    }else{
                        strlens.append("+strlen(").append(tk).append(")");
                    }

                }
            }
        }

        strlens.deleteCharAt(0);
        codeTempLine.setLength(0);
        codeTempLine.append(semRef.arreglosTiposVars[1]).append(" = (char*) malloc (").append(strlens).append(");\n");
        for(String var : vars.toString().split(" "))
        {
            codeTempLine.append("strcat(").append(semRef.arreglosTiposVars[1]).append(",").append(var).append(");\n");
        }
    }

    public void addTemporalCode()
    {
        block.addCode(codeTempLine.toString());
        codeTempLine.setLength(0);
    }

    public void reset(){
        codeLine.setLength(0);
        codeMacros.setLength(0);
        codeTempLine.setLength(0);
        definesCantidad =0;
        currentIdBlock=0;
        finWhile=endSwitchCant=caseCant= mientrasCant =-1;
        block=null;
    }


}
