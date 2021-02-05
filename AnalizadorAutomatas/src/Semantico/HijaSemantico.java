package Semantico;

import SemanticoRecursos.Tipos;
import compiler.AnalisisLexico;
import compiler.AnalisisSemantico;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HijaSemantico extends AnalisisSemantico
{

    public int getIndexSymbol(String symbol)
    {
        switch(symbol)
        {
            case "+":
                return 0;

            case "-":
            case "*":
            case "/":
                return 1;

            case "<":
            case "~":
            case ">":
            case "<=":
            case ">=":
            case "==":
                return 2;

            default:
                return -1;
        }
    }

    public static String[][][] tiposCorrectos =
            {{{ "entero",null,"entero"},{null,"texto",null},{"decimal",null,"decimal"}},
            {{"entero",null,"entero"},{null,null,null},{"decimal",null,"decimal"}},
            {{"entero",null,"entero"}, {null,"texto",null},{"decimal",null,"decimal"}}};

    public boolean isConstant;
    public static String[] datatypes={"entero","texto","decimal"};

    private final ArrayList<MacroConstant> macroConstants = new ArrayList<>();
    private final LinkedHashMap<String, ArrayList<Identificadores_Valores>> dataIdTypes;
    public StringBuilder erroresSemantico;
    public StringBuilder expresion;
    public TraductoCodigoC traductoCodigoC;
    private String currentType;
    private boolean isAsign,isPuntoComa, esMientras;
    private boolean esUnSwitch;
    private boolean isDefault,isCase,isReading,isPrintln;
    public String symboloLogico;


    public String [] arreglosTiposVars;

    public HijaSemantico(Runnable callBackError, Runnable callBackAceptado) {
        super(callBackError, callBackAceptado);
        expresion = new StringBuilder();
        erroresSemantico = new StringBuilder();
        dataIdTypes = new LinkedHashMap<>();
        traductoCodigoC = new TraductoCodigoC(this);
    }

    public void reiniciar(){
        super.reiniciar();
        pilaDeSwitch.clear();
        traductoCodigoC.reset();
        dataIdTypes.clear();
        erroresSemantico.setLength(0);
        expresion.setLength(0);
        isDefault=isCase=isReading=isPrintln= esMientras =isPuntoComa=isConstant= esUnSwitch =isAsign=false;
        arreglosTiposVars =null;
        currentType= symboloLogico =null;
        macroConstants.clear();
    }


    private String verificaTipos(Tipos type1, Tipos type2, int index)
    {
        return tiposCorrectos[index][type1.posicion][type2.posicion];
    }




    protected Stack<SwitchCase> pilaDeSwitch = new Stack<>();


    private void checarReservadas(String comp){
        switch (comp)
        {
            case "leer":
                isReading = true;
                break;

            case "caso":
                isCase = true;
                traductoCodigoC.addCase();
                break;

            case "default":
                isDefault = true;
                traductoCodigoC.addDefault();
                break;

            case "switch":
                traductoCodigoC.addSwitch();
                esUnSwitch =true;
                break;

            case "imprimir":
                isPrintln=true;
                break;

//            case "do":
//                currentType=null;
//                codeCreator.addBlock(codeCreator.getNextDo(),"Do");
//                break;

            case "principal":
                currentType=null;
                traductoCodigoC.addBlock("principal","Principal");
                break;

            case "mientras":
                esMientras =true;
                currentType=null;
//                traductoCodigoC.addBlock(traductoCodigoC.siguienteMientras(),"Mientras");
                traductoCodigoC.agregaBloqueMientras();
                break;

            case "inicio":
                if(esMientras)
                {
                    esMientras =false;
                    currentType=null;
                    String aux = expresion.toString();
                    boolean ex =  evalLogicExpression();
                    if(!ex){
                        lanzaErrorSemantico("Error en la expresion: "+aux);
                    }
                }
            break;

            case "fin":

                if(traductoCodigoC.getCurrentIdBlockName().equals("Default")){
                    traductoCodigoC.finishBlock("def");
                    isDefault=false;
                }

                else if(traductoCodigoC.getCurrentIdBlockName().equals("Switch")){
                    String endS = pilaDeSwitch.pop().bloqueFin;
                    traductoCodigoC.finishBlock("swi");
                    traductoCodigoC.appendCode("\n").appendCode(endS).appendCode(": ;\n").addCode();
                }

                else if(traductoCodigoC.getCurrentIdBlockName().equals("Mientras")){

                    traductoCodigoC.finishBlock("mientras");
                }

                else if(traductoCodigoC.getCurrentIdBlockName().equals("Caso")){

                    traductoCodigoC.addCode();
                    traductoCodigoC.finishBlock("case");
                }

                else if(traductoCodigoC.getCurrentBlockName().equals("principal"))
                {
                    traductoCodigoC.finishBlock("princ");
                }
                break;

            default:
                int indexType = Arrays.asList(datatypes).indexOf(comp);

                //Es algun tipo de dato
                if(indexType!=-1)
                {
                    currentType=comp;
                    if(!dataIdTypes.containsKey(comp))
                        dataIdTypes.put(comp,new ArrayList<>());
                    traductoCodigoC.appendCode(comp);
                }
        }
    }

    public void checarLiterales(String comp)
    {
        if(isCase){
            isCase=false;

            if(convertToIf(comp,Tipos.TEXTO))
                return;
            else
                lanzaErrorSemantico(String.format("El tipo del valor %s no es compatible con " +
                                "el tipo de dato de la variable %s usada en el switch\n",
                        comp,pilaDeSwitch.peek().nombreVariable));
            return;
        }

        if(isPrintln){
            convertPrint(comp);
            return;
//            expresion.append(comp).append(" ");
        }

        String vname  = getVarNameMacroConstant(comp);

        if(vname==null) {
            String mCVarName = traductoCodigoC.addConstant(comp);
            macroConstants.add(new MacroConstant(mCVarName, comp, Tipos.TEXTO));
            expresion.append(mCVarName).append(" ");
        }else{
            expresion.append(vname).append(" ");
        }
    }

    private void checarNumeros(String comp)
    {
        if(isCase){
            isCase=false;

            if(convertToIf(comp,comp.contains(".") ? Tipos.DECIMAL : Tipos.ENTERO))
                return;
            else
                lanzaErrorSemantico(String.format("El tipo del valor %s no es compatible con " +
                                "el tipo de dato de la variable %s usada en el switch\n",
                        comp,pilaDeSwitch.peek().nombreVariable));
            return;
        }


        if(isPrintln){
            convertPrint(comp);
            return;
        }

        String vname  = getVarNameMacroConstant(comp);

        if(vname==null) {

            String mCVarName = traductoCodigoC.addConstant(comp);
            macroConstants.add(new MacroConstant(mCVarName, comp, comp.contains(".") ? Tipos.DECIMAL :
                    Tipos.ENTERO));
            expresion.append(mCVarName).append(" ");
        }else{
            expresion.append(vname).append(" ");
        }
    }

    public boolean semanticAnalyze(String comp)
    {
        switch (currentToken)
        {
            case PALABRA_RESERRVADA:
                checarReservadas(comp);
                break;

            case LITERAL:
                checarLiterales(comp);
                break;

            case NUMERO:
                checarNumeros(comp);
                break;

            case SYMBOLO:
                switch (comp)
                {
                    case ";":
                        isPuntoComa=true;

                        if(isAsign)
                        {
                            isAsign=false;
                            initVariable();
                            boolean ex=evalExpression();
                            currentType=null;
                            if(isConstant) isConstant=false;
                            return  ex;
                        }

                        if(isReading){
                            isReading=false;
                            return true;
                        }
                        if(isPrintln){
                            isPrintln=false;
                        }
                        else{
                            //Agrega el codigo necesario en caso de que la ultima variable no haya sido asignada

                            traductoCodigoC.appendCode(";\n").addCode();
                            if(arreglosTiposVars[0].equals("texto"))
                            {
                                traductoCodigoC.toCString(false);
                            }
                            traductoCodigoC.addTemporalCode();
                            arreglosTiposVars =null;
                            isAsign=false;
                            expresion.setLength(0);
                            if(isConstant) isConstant=false;
                            currentType=null;
                        }
                        return true;

                    case ",":
                        isPuntoComa=false;
                        if(isAsign)
                        {
                            isAsign=false;
                            initVariable();
                            return evalExpression();
                        }
                        else{
                            traductoCodigoC.appendCode(", ");
                        }
                        break;

                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        expresion.append(comp).append(" ");
                        break;

                    case "(":
                    case ")":
                        if(isAsign){

                            expresion.append(comp).append(" ");
                        }
                        break;



                    case "=":
                        symboloLogico =comp;
                        isAsign=true;
                        break;

                    case "<=":
                    case ">=":
                    case "~":
                    case "<":
                    case ">":
                    case "==":
                        symboloLogico =comp;
                        expresion.append(comp).append(" ");
                        break;
                }
                break;

            case ID:

                if(isCase){
                    isCase=false;

                    //Comprueba si el case [varname] exista, este incializad y ademas es de un tipo compatible
                    if(existieEInicializada(comp,false))
                    {
                        Tipos t = getVariableType(comp);
                        if(convertToIf(comp,t))
                            return true;
                        else
                            lanzaErrorSemantico(String.format("El tipo de la variable %s no es compatible con " +
                                            "el tipo de dato de la variable %s usada en el switch\n",
                                    comp, pilaDeSwitch.peek().nombreVariable));
                    }
                    return false;
                }

                if(esUnSwitch){
                    esUnSwitch =false;

                    if(existieEInicializada(comp,false)){
                        pilaDeSwitch.add(new SwitchCase(comp,getVariableType(comp),
                                traductoCodigoC.siguientesFinSwitch()));
                        return true;
                    }

                    return false;
                }

                if(isReading){

                    if(expresion.length()!=0) {
                        System.out.println("The exprs have: "+expresion);
                        throw new IllegalStateException("expresion must be empty when is " +
                                "used the read method. Verify code");
                    }

                    //Solo se debe checar si existe, ya que el read se ebcarga de inicializar
                    arreglosTiposVars = variableExistente(comp);

                    if(arreglosTiposVars ==null){
                        erroresSemantico.append(String.format("semanticAnalyze: No se ha declarado el identifcador %s ",
                                comp));
                        super.callBackError.run();
                        return false;
                    }

                    convertReader();
                    return true;
                }

                if(isPrintln){
                    if(currentType!=null) {
                        throw new IllegalStateException("CurrentType must be null when is " +
                                "used  println. Verify code");
                    }

                     return convertPrint(comp);

                }

                //Si es un identificador y el tipo actual es nuloy hay asingacion
                // es porque es esta evaluando una expresion en otra linea, y no en una
                //declaracion: int x; x=->x+2*x;
                if (currentType==null)
                {
                    if(isAsign){
                        return existieEInicializada(comp,true);
                    }else if(esMientras){

                        if(existieEInicializada(comp,true)){
                            arreglosTiposVars = variableExistente(comp);
                            return true;
                        }
                        return false;
                    }
                    else{

                        //va a guadar la variable y su tipo en variable auxiliar
                        arreglosTiposVars = variableExistente(comp);
                        if(arreglosTiposVars ==null){
                            erroresSemantico.append(String.format("semanticAnalyze: No se ha declarado el identifcador %s ",
                                    comp));
                            super.callBackError.run();
                            return false;
                        }
                    }
                }
                else{
                    if(isAsign){
                        //Si esto se cumple es porque currentType trae algun tipo de dato
                        //y se esta declrando y asingnando en linea:
                        //int x=->y*z;
                        return existieEInicializada(comp,true);
                    }
                    else{
                        //Se esta creadbo,declarando algun tipo de variable
                        //int x;
                        boolean g = existsVariableOnBlock(comp);
                        if(g){

                            //va a guadar la variable y su tipo en variable auxiliar
                            arreglosTiposVars = variableExistente(comp);

                            if(arreglosTiposVars ==null){
                                erroresSemantico.append(String.format("semanticAnalyze: No se ha declarado el identifcador %s ",
                                        comp));
                                super.callBackError.run();
                                return false;
                            }
                            if(currentType.equals("texto"))
                                traductoCodigoC.appendCode("*").appendCode(comp);
                            else if(isConstant)
                                traductoCodigoC.appendCode(" * ").appendCode(comp);
                            else
                                traductoCodigoC.appendCode(comp);

                        }

                        return g;
                    }

                }
                break;
        }

        return true;
    }

    private boolean convertToIf(String comp, Tipos type)
    {
        if(verificaTipos(type, pilaDeSwitch.peek().tipos,getIndexSymbol("+"))!=null) {
            String compString;

            if (type == Tipos.TEXTO)
                compString = String.format
                        ("\nif(strcmp(%s,%s)!=0)\n", pilaDeSwitch.peek().nombreVariable, comp);
            else
                compString = String.format
                        ("\nif(%s!=%s)\n", pilaDeSwitch.peek().nombreVariable, comp);

            traductoCodigoC.appendCode(compString).appendCode
                    ("goto ").appendCode(traductoCodigoC.getCurrentCase()).appendCode
                    (";\n");

            return true;
        }
        return false;
    }

    public Tipos getTypeMacro(String varname)
    {
        for (MacroConstant macroConstant : macroConstants) {
            if (macroConstant.nombreVariable.equals(varname))
            {
                return macroConstant.tipo;
            }
        }
        return null;
    }

    public String getValueMacroConstant(String varname)
    {
        for (MacroConstant macroConstant : macroConstants) {
            if (macroConstant.nombreVariable.equals(varname))
            {
                return macroConstant.valor;
            }
        }
        return null;
    }

    public String getVarNameMacroConstant(String value)
    {
        for (MacroConstant macroConstant : macroConstants) {
            if (macroConstant.valor.equals(value))
            {
                return macroConstant.nombreVariable;
            }
        }
        return null;
    }

//    private void setValue2IdValue(String type, String varName,String value){
//        for (IdValue idValue : dataIdTypes.get(type)) {
//            if(idValue.id.equals(varName)){
//                idValue.value=value;
//                break;
//            }
//        }
//    }

   /* public String getValueOfId(String type, String varName){
        for (IdValue idValue : dataIdTypes.get(type)) {
            if(idValue.id.equals(varName)){

                return idValue.value;
            }
        }
        return null;
    }*/

    private void initVariable()
    {
        int id= traductoCodigoC.getCurrentIdBlock();
        while(id>=0)
        {
            for (Identificadores_Valores idv : dataIdTypes.get(arreglosTiposVars[0])) {
                if (idv.nombreVariable.equals(arreglosTiposVars[1]) && idv.idBlock == id) {
                    idv.initilized = true;
                    break;
                }
            }
            id--;
        }

    }

    private boolean evalLogicExpression(){
        String[] expArray = expresion.toString().split(" ");
        String compatibility;


        Tipos type1,type2;

        expresion.setLength(0);
        boolean swap=false;
        String comp=null;
        for (int i = 0; i < expArray.length; i++)
        {
            comp = expArray[i];

            switch (comp)
            {
                case "(":
                case ")":
                    if (swap){
                        swap=false;
                        expresion.append(" ");
                    }
                    expresion.append(comp).append(" ");
                    break;

                case "<=":
                case "<":
                case ">":
                case ">=":
                case "~":
                case "==":
                    type1 = getTypeOf(expArray[i-1]);
                    type2 = getTypeOf(expArray[i+1]);

                    compatibility = verificaTipos(type1,type2,2);

                    if(compatibility==null){
                        lanzaErrorSemantico(String.format("evalLogicExpression: La comparacion entre %s %s %s no es compatible\n",
                                expArray[i-1].startsWith("$") ? getValueMacroConstant(expArray[i-1]): expArray[i-1],
                                comp,
                                expArray[i+1].startsWith("$") ? getValueMacroConstant(expArray[i+1]): expArray[i+1]));
                        return false;
                    }

                default:
                    swap=true;
//                    if(comp.equals("~"))
//                        comp="!=";
                    expresion.append(comp);
            }
        }

        String varC = TraductoCodigoC.obtenerVariableDeTipo("entero");

        assert varC != null;
        boolean exist;

        exist = existVariable("entero",varC );

        if (!exist) {
            traductoCodigoC.appendTemporalCode("int").appendTemporalCode
                    (" ").appendTemporalCode(varC).appendTemporalCode(";\n");
            //esto solo es para crear una variable como $iVar en caso de que no existe previamenente
            Identificadores_Valores identificadoresValores = new Identificadores_Valores(varC, false, traductoCodigoC.getCurrentIdBlock());
            identificadoresValores.initilized = true;
            dataIdTypes.get("entero").add(identificadoresValores);


        }

        Posfix.creatorCode = (var)->{

            if(existVariable("entero",var))
                return false;

            traductoCodigoC.appendTemporalCode("int").appendTemporalCode
                    (" ").appendTemporalCode(var).appendTemporalCode(";\n");
            Identificadores_Valores identificadoresValores = new Identificadores_Valores(var, false, traductoCodigoC.getCurrentIdBlock());
            identificadoresValores.initilized = true;
            dataIdTypes.get("entero").add(identificadoresValores);
            return true;
        };

        Posfix.infixToPostfix(expresion.toString().split(" "), varC,"entero",false);

        while(!Posfix.expressionCode.isEmpty())
        {
            comp = Posfix.expressionCode.poll();
            traductoCodigoC.appendTemporalCode(comp).appendTemporalCode(";\n");
        }

        traductoCodigoC.toCString(true);

        if(comp==null) throw new NullPointerException("The [comp] variables is null.\nError in eval logical exp.");


        traductoCodigoC.appendTemporalCode("if (!").appendTemporalCode(comp.split(" ")[0]).appendTemporalCode(")\n");
        traductoCodigoC.appendTemporalCode
("goto ").appendTemporalCode(traductoCodigoC.siguienteFinMientras()).appendTemporalCode(";\n");
//                (traductoCodigoC.getLastBlock().blockName).appendTemporalCode(";\n");
        traductoCodigoC.addTemporalCode();

        expresion.setLength(0);

        return true;
    }

    public Tipos getTypeOf(String varName){
        Tipos type1 = getVariableType(varName);

        if(type1==null){
            type1 = getTypeMacro(varName);
        }

        if(type1==null) throw new NullPointerException("The given "+varName+" is null.\nVerify the code.");

        return type1;
    }

    private boolean evalExpression()
    {
        boolean get=true;

        String[] expArray = expresion.toString().split(" ");


        if(expArray.length==1)
        {
            Tipos type1;
            String vl;
            if(expArray[0].startsWith("$")){
                type1 = getTypeMacro(expArray[0]);
                vl = getValueMacroConstant(expArray[0]);
            }else{
                //Es variable
                vl = expArray[0];
                type1 = getVariableType(vl);
            }

            assert type1 != null;
            vl = verificaTipos(Tipos.valueOf(arreglosTiposVars[0].toUpperCase()),type1,getIndexSymbol("+"));

            if(vl!=null && vl.equals(arreglosTiposVars[0])){
                get=true;

                if(currentType==null)
                    traductoCodigoC.appendCode(arreglosTiposVars[1]);

                traductoCodigoC.appendCode(" = ");
                traductoCodigoC.appendCode(expArray[0]);
                if(isPuntoComa)
                {
                    traductoCodigoC.appendCode(";\n").addCode();
                }
                else traductoCodigoC.appendCode(", ");
            }else{
                erroresSemantico.append(String.format
                        ("_evalExpression: El termino %s de tipo de dato %s no coincide con el tipo de dato " +
                                "de la variable %s\n", vl,type1.tipo, arreglosTiposVars[1]));
                super.callBackError.run();
                get=false;
            }

        }

        else {
            String varC = null;
            String compatibility;
            boolean firstTime = true;
            boolean isCons;
            if (isCons = isConstant(arreglosTiposVars[1], arreglosTiposVars[0], false)) {
                varC = TraductoCodigoC.obtenerVariableDeTipo(arreglosTiposVars[0]);
                assert varC != null;
                boolean exist;

                exist = existVariable(arreglosTiposVars[0],varC );


                if (!exist) {
                    traductoCodigoC.appendTemporalCode(arreglosTiposVars[0]).appendTemporalCode
                            (" ").appendTemporalCode(varC).appendTemporalCode(";\n");
                    Identificadores_Valores identificadoresValores = new Identificadores_Valores(varC, false, traductoCodigoC.getCurrentIdBlock());
                    identificadoresValores.initilized = true;
                    dataIdTypes.get(arreglosTiposVars[0]).add(identificadoresValores);


                }

                Posfix.creatorCode = (var)->{

                    if(existVariable(arreglosTiposVars[0],var))
                        return false;

                    traductoCodigoC.appendTemporalCode(arreglosTiposVars[0]).appendTemporalCode
                            (" ").appendTemporalCode(var).appendTemporalCode(";\n");
                    Identificadores_Valores identificadoresValores2 = new Identificadores_Valores(var, false, traductoCodigoC.getCurrentIdBlock());
                    identificadoresValores2.initilized = true;
                    dataIdTypes.get(arreglosTiposVars[0]).add(identificadoresValores2);
                    return true;
                };
                Posfix.infixToPostfix(expArray, varC, arreglosTiposVars[0], true);
            } else {

                {
                    Posfix.creatorCode = (var)->{

                        if(existVariable(arreglosTiposVars[0],var))
                            return false;

                        traductoCodigoC.appendTemporalCode(arreglosTiposVars[0]).appendTemporalCode
                                (" ").appendTemporalCode(var).appendTemporalCode(";\n");
                        Identificadores_Valores identificadoresValores = new Identificadores_Valores(var, false, traductoCodigoC.getCurrentIdBlock());
                        identificadoresValores.initilized = true;
                        dataIdTypes.get(arreglosTiposVars[0]).add(identificadoresValores);
                        return true;
                    };
                }

                //Aqui no es necesario la varC porque datTypeAux ya existe
                // por lo taanto siempre por primera vez entrara y asignara dicha variable
                Posfix.infixToPostfix(expArray, arreglosTiposVars[1], arreglosTiposVars[0],true);
            }

            Tipos type1 , type2;

            String valueId1, valueId2;
            int indexSymbol;

            while (!Posfix.expressionCode.isEmpty()) {
                String[] expresionSplit = Posfix.expressionCode.poll().split(" ");

                indexSymbol = getIndexSymbol(expresionSplit[3]);

                //Es macro
                if (expresionSplit[2].startsWith("$")) {
                    type1 = getTypeMacro(expresionSplit[2]);
                    if (type1 != null)
                        valueId1 = getValueMacroConstant(expresionSplit[2]);
                    else {
                        type1 = getVariableType(expresionSplit[2]);
                        valueId1 = expresionSplit[2];
                    }
                } else {
                    //Es variable
                    valueId1 = expresionSplit[2];
                    type1 = getVariableType(valueId1);
                }

                //Es macro
                if (expresionSplit[4].startsWith("$")) {
                    type2 = getTypeMacro(expresionSplit[4]);
                    if (type2 != null)
                        valueId2 = getValueMacroConstant(expresionSplit[4]);
                    else {
                        type2 = getVariableType(expresionSplit[4]);
                        valueId2 = getValueMacroConstant(expresionSplit[4]);
                    }

                } else {
                    //Es variable
                    valueId2 = expresionSplit[4];
                    type2 = getVariableType(valueId2);
                }


                if (type1 == null) throw new AssertionError("Type 1 is null");
                if (type2 == null) throw new AssertionError("Type 2 is null");

                compatibility = verificaTipos(Tipos.valueOf(arreglosTiposVars[0].toUpperCase()),
                        type1, indexSymbol);

                if (compatibility == null) {
                    //No coinciden los tipos
                    erroresSemantico.append(String.format
                            ("evalExpression: Las valores de %s & %s de tipos [%s] & [%s] no son compatibles\n",
                                    valueId1, valueId2, type1.tipo, type2.tipo));
                    super.callBackError.run();
                    get = false;
                    break;
                } else {

                    compatibility = verificaTipos(Tipos.valueOf(compatibility.toUpperCase()),
                            type2, indexSymbol);

                    if (compatibility!=null && compatibility.equals(arreglosTiposVars[0])) {

                        if (isPuntoComa) {
                            if (firstTime) {
                                if(currentType!=null)
                                    traductoCodigoC.appendCode(";\n").addCode();
                                else traductoCodigoC.addCode();
                                firstTime = false;
                            }
//                            codeCreator.appendTemporalCode(codeCreator.getCurrentTabulation());
                            for (String s : expresionSplit) {
                                traductoCodigoC.appendTemporalCode(s).appendTemporalCode(" ");
                            }
                            traductoCodigoC.appendTemporalCode(";\n");
                        } else {
                            if(firstTime)
                            {
                                traductoCodigoC.appendCode(", ");
                                firstTime=false;
                            }

//                            codeCreator.appendTemporalCode(codeCreator.getCurrentTabulation());
                            for (String s : expresionSplit) {
                                traductoCodigoC.appendTemporalCode(s).appendTemporalCode(" ");
                            }
                            traductoCodigoC.appendTemporalCode(";\n");
                        }

                    } else {
                        erroresSemantico.append(String.format
                                ("__evalExpression: EL termino de [%s %s %s] no coincide con el tipo de dato " +
                                                "de la variable %s\n",
                                        valueId1, expresionSplit[3], valueId2, arreglosTiposVars[1]));
                        super.callBackError.run();
                        get = false;
                        break;
                    }
                }
            }


        }

        if(isPuntoComa && tiposCorrectos !=null)
        {
            if(arreglosTiposVars[0].equals("texto") && expArray.length>1)
            {
                traductoCodigoC.toCString(false);
            }

            traductoCodigoC.addTemporalCode();
        }

        arreglosTiposVars =null;
        expresion.setLength(0);
        return get;
    }

    private boolean isConstant(String var,String type,boolean checkInit)
    {
        int id= traductoCodigoC.getCurrentIdBlock();

        while(id>=0) {
            for (Identificadores_Valores identificadoresValores : dataIdTypes.get(type)) {
                if (identificadoresValores.nombreVariable.equals(var) && identificadoresValores.idBlock==id) {
                    return checkInit ? identificadoresValores.isConstant && identificadoresValores.initilized : identificadoresValores.isConstant;
                }
            }
            id--;
        }
        return false;
    }

    private void convertReader()
    {
        String app = traductoCodigoC.pasarALectura(arreglosTiposVars[1], arreglosTiposVars[0]);

        traductoCodigoC.appendCode(app).appendCode("\n").addCode();

        initVariable();

        arreglosTiposVars =null;
        expresion.setLength(0);
    }

    private boolean convertPrint(String id)
    {
        boolean accepted=true;

            String var = null;

            if(Objects.equals(AnalisisLexico.isString(id), AnalisisLexico.cadena_correcta)) {
                var = traductoCodigoC.convertPrintlnToPf(id, "texto");
            }

            else if(Objects.equals(AnalisisLexico.isNum(id), AnalisisLexico.cadena_correcta)) {
                var = id.contains(".") ? traductoCodigoC.convertPrintlnToPf(id, "decimal") :
                        traductoCodigoC.convertPrintlnToPf(id, "entero");
            }
            else
                if(existieEInicializada(id,false))
            {
                Tipos t = getVariableType(id);
                if(t==null) throw new NullPointerException
                        ("convertPrint: The type returned is null. It seems the variable "+id+" not exist." +
                                "Check the methd verifyExistAndInit is ok");

                var = traductoCodigoC.convertPrintlnToPf(id,t.tipo);
//                traductoCodigoC.appendCode(var).appendCode("\n").addCode();

            }else accepted=false;

        if (accepted)
            traductoCodigoC.appendCode(var).appendCode("\n").addCode();

        expresion.setLength(0);
        return accepted;
    }

    private boolean existieEInicializada(String comp, boolean concatenarExpresion)
    {
        String data[] = variableExistente(comp);


        if(data==null){
            lanzaErrorSemantico(String.format("verifyExistAndInit: No se ha declarado el identifcador %s ", comp));
            return false;
        }
        else{
            if(data[2].equals("false")){
                lanzaErrorSemantico(String.format("verifyExistAndInit: El identificador %s no se ha inicializado\n",comp));
                return false;
            }
            else{
                if(concatenarExpresion)
                    expresion.append(comp).append(" ");
            }
        }

        return true;
    }

    private void lanzaErrorSemantico(String msg){
        erroresSemantico.append(msg);
        super.callBackError.run();
    }

    protected Tipos getVariableType(String varname)
    {
        Set<String> keys = dataIdTypes.keySet();

        int id = traductoCodigoC.getCurrentIdBlock();

        while(id>=0) {
            for (String k : keys) {
                for (Identificadores_Valores identificadoresValores : dataIdTypes.get(k)) {
                    if (identificadoresValores.nombreVariable.equals(varname) && identificadoresValores.idBlock == id)
                    {
                        return Tipos.valueOf(k.toUpperCase());
                    }
                }
            }
            id--;
        }

        return null;
    }

    private boolean existVariable(String type,String varName)
    {
        if(dataIdTypes.containsKey(type))
            for (Identificadores_Valores identificadoresValores : dataIdTypes.get(type)) {
                if(identificadoresValores.nombreVariable.equals(varName) && identificadoresValores.idBlock== traductoCodigoC.getCurrentIdBlock())
                {
                    return true;
                }
            }else{ dataIdTypes.put(type,new ArrayList<>()); }
        return false;
    }

    private String[] variableExistente(String comp){

        Set<String> keys = dataIdTypes.keySet();
        int id= traductoCodigoC.getCurrentIdBlock();

        while(id>=0)
        {
            for (String k : keys) {
                for (Identificadores_Valores identificadoresValores : dataIdTypes.get(k)) {
                    //Checa si el nombre de la variable coincide con la de la lista de variables
                    //en caso de, checa ahora que el bloque de la variable sea igual id
                    if (identificadoresValores.nombreVariable.equals(comp) && identificadoresValores.idBlock == id) {
                        return new String[]{k, comp, String.valueOf(identificadoresValores.initilized)};
                    }
                }
            }
            id--;
        }


        return null;
    }

    //Checa si ya existe la variable dada, en caso de que no, la crea
    private boolean existsVariableOnBlock(String comp){
        Set<String> keys = dataIdTypes.keySet();
        AtomicBoolean status= new AtomicBoolean(true);

        keys.forEach(k->{

            ArrayList<Identificadores_Valores> identificadoresValores = dataIdTypes.get(k);

            identificadoresValores.forEach(identificadoresValores1 ->
            {
                //Aqui debe checar solo si la variable existe dentro del bloque de codigo
                //da igual si esta variable existe en otro bloque como un do, o un case
                //si existe manda error
                if(identificadoresValores1.nombreVariable.equals(comp) && identificadoresValores1.idBlock== traductoCodigoC.getCurrentIdBlock()){
                    erroresSemantico.append(String.format("checkDataTypes: Ya existe el identifcador %s declarado con el "+
                            "tipo de dato %s\n",comp,k));
                    super.callBackError.run();
                    status.set(false);
                }
            });

        });


        if(status.get()){
            //Si no existe el idemtifcador lo crea, con el tipo que le corsonde
            dataIdTypes.get(currentType).add(new Identificadores_Valores(comp,isConstant, traductoCodigoC.getCurrentIdBlock()));
        }

        return status.get();
    }
}
