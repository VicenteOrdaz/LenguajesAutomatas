package AyudaLR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static AyudaLR.LR_Generator2.instance;

class Cerradura implements Serializable {


    public ArrayList<ProduccionCerradura> parameters;
    public String NtcameFrom;
    public int estadoFrom,estadoCer;

    class Data implements Serializable{
        public int state;
        public boolean isNew=false;

        public Data(int s, boolean isNew){
            state=s;
            this.isNew=isNew;

        }

        @Override
        public String toString() {
            return "Data{" +
                    "state=" + state +
                    ", isNew=" + isNew +
                    '}';
        }
    }

    //private LinkedHashMap<String, ArrayList<ProduccionCerradura>> sub_producciones;
    protected ListCerraduras sub_producciones;
    protected LinkedHashMap<Integer,ProduccionCerradura> producciones;
    protected LinkedHashMap<String,Data> prodAndstates;

    public Cerradura(int estado, ArrayList<ProduccionCerradura> parametrs, String NtcameFrom, int edoFrom) {
        this.estadoFrom= edoFrom;
        this.estadoCer=estado;
        this.parameters = parametrs;
        this.NtcameFrom = NtcameFrom;
        this.prodAndstates = new LinkedHashMap<>();
        sub_producciones=new ListCerraduras();
        this.producciones = new LinkedHashMap();

    }

    protected void addParameter(ProduccionCerradura pr){
        for(ProduccionCerradura p : parameters)
        {
            if(p.compare(pr.produccion) && pr.no_terminal.equals(p.no_terminal) &&
                p.position==pr.position)
                return;

        }
        parameters.add(pr);
    }

    private int index;

    protected void analyze() {
        String item;
        int edo;
        sub_producciones.clear();
        this.producciones.clear();

        for (ProduccionCerradura pc : parameters) {
                item = pc.getCurrentItem();
                if(item!=null){
                    edo=instance.getStateOf(pc.no_terminal,pc.produccion,item);

                    if(edo==this.estadoCer)
                        continue;

                    //Los parametros leidos ya existen en otros edos de cerradura
                    if(edo!=-1){
//                        System.out.printf("Adding edo %d in edo %d\n",edo,this.estadoCer);
                        sub_producciones.add(new ProduccionCerradura(pc.position,
                                pc.no_terminal,pc.produccion));
                        prodAndstates.put(item,new Data(edo,false));
                        continue;
                    }

                    int vec[]=instance.nextPointer(pc.no_terminal,pc.produccion);
                    //sub_producciones.put(pc.no_terminal,new ArrayList<>(){{add(pc.produccion);}});
                    sub_producciones.add(new ProduccionCerradura(pc.position,
                            pc.no_terminal,pc.produccion));

                    //genera estado
                    if(vec!=null){
                        instance.estadosCerraduras.add(new Cerradura(vec[1],
                                new ArrayList(){{add(new ProduccionCerradura(vec[0],pc.no_terminal,
                                        pc.produccion));}},
                                item,this.estadoCer));
                        prodAndstates.put(item,new Data(vec[1],true));
                    }else{
                        System.err.println("Error while analyzing");
                        System.exit(-1);
                    }

                }else{
                    //Genera produccion
                    int val = instance.getNumProduction(pc.no_terminal,pc.produccion);
                    if(val==-1){
                        System.err.println("Error in productions");
                        System.exit(-1);
                    }
                    producciones.put(val,pc);
                    continue;
                }

                subProductions(item);
        }


        sub_producciones.forEach( pc->{
            //prodAndstates.put(key);
            String currentI;
            int state;
            //for(String prod[] : pc.produccion)
            {
                currentI=pc.getCurrentItem();

               // System.out.printf("Checking subprod %s -> %s with current: %s\n"
                 //       ,pc.no_terminal,Arrays.toString(pc.produccion),currentI);
                if(!prodAndstates.containsKey(currentI))
                {
                    state=instance.getStateOf(pc.no_terminal,pc.produccion,currentI);
                   // System.out.printf("The state of %s -> %s with current on %s is %d\n",
                     //       pc.no_terminal,Arrays.toString(pc.produccion),currentI,state);
                    //Es un nuevo estado
                    if(state==-1){
                        state=instance.estadoActual++;
                        prodAndstates.put(currentI,new Data(state,true));
                        instance.addCerradura(pc.no_terminal,pc.produccion,state,
                                currentI,this.estadoCer,pc.position+1);
                    //    System.out.printf("Created state of %s -> %s with current on %d is %d\n",
                      //          pc.no_terminal,Arrays.toString(pc.produccion),pc.position+1,state);
                    }
                    //Ya exisitia ese estado
                    else{
                        prodAndstates.put(currentI,new Data(state,false));
                        //instance.addCerradura(pc.no_terminal,pc.produccion,state,currentI,this.estadoCer,pc.position+1);
                    }
                }else{
                    //state=instance.getStateOf(pc.no_terminal,pc.produccion,currentI);
                 //   System.out.printf("The state existed of %s -> %s with current on %s is %d\n",
                   //         pc.no_terminal,Arrays.toString(pc.produccion),currentI,prodAndstates.get(currentI).state);
                    instance.addCerradura(pc.no_terminal,pc.produccion,prodAndstates.get(currentI).state,
                            currentI,this.estadoCer,pc.position+1);
                }
            }
        });



    }


    private void subProductions(String item)
    {
        if (item != null) {
            index = instance.searchNT(item);

            if (index != -1) {
                NotTerminal nt = instance.list.get(index);
              //  System.out.println("Evaluating: "+nt.nt+" -> "+nt.productions);
                nt.productions.forEach(prod->{
                   /* if(sub_producciones.get(nt.nt)!=null)
                    {
                        sub_producciones.get(nt.nt).add(prod.split(" "));
                    }
                    else{
                        sub_producciones.put(nt.nt,new ArrayList<>(){{add(prod.split(" "));}});
                    }*/
                    ProduccionCerradura pc = new ProduccionCerradura(
                            0,item,prod.split(" ")
                    );
                   // System.out.printf("Adding sub %s in edo %d\n",pc,this.estadoCer);
                    sub_producciones.add(pc);
                    String k=prod.split(" ")[0];
                    //System.out.println("Checking key: ");
                  //  System.out.println("Checking: "+k);
                    if(!sub_producciones.contains(k))
                       subProductions(k);
                    else{
                        if(sub_producciones.get(0).no_terminal.equals(k)){
                            subProductions(k);
                        }
                    }
                });
            }
        }
    }

    @Override
    public String toString() {

//        System.out.println(prodAndstates);
        StringBuilder prods=new StringBuilder();
        sub_producciones.forEach(p->{

             prods.append(p.no_terminal).append("-->").append(Arrays.toString(p.produccion)).append
                        (" > ").append(prodAndstates.get(p.getCurrentItem()).state).append
                     ("(").append(p.getCurrentItem()).append(")\n");
        });
        producciones.forEach((key,value)->{
            prods.append(value.no_terminal).append(" --> ").append
                    (Arrays.toString(value.produccion)).append(" > P").append(key).append("\n");
        });
        if(this.estadoFrom==-1)
        return String.format("I%d = Cerradura (%s) =  {\n" +
                "\n%s\n" +
                "}",this.estadoCer,this.parameters,prods);
        else{

            return String.format("I%d = Cerradura (%s) = Ir_a(I%d,%s) = {" +
                    "\n%s\n" +
                    "}",this.estadoCer,this.parameters,this.estadoFrom,this.NtcameFrom,
                    prods);
        }
    }
}
