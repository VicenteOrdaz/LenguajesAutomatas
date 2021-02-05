package AyudaLR;

import cargar.Ejecutar;
import compiler.Serializa;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LR_Generator2 implements Serializable
{

    public static void main(String[] args) {
        new LR_Generator2(0).build();
    }

    public static final File file = new File(Ejecutar.pathFiles+"/LR.xlsx");

    public ArrayList<String> terminals;
    public ArrayList<NotTerminal> list;
    {
        list = new ArrayList<>();
    }

    public int estadoActual=0;
    private int ind;

    public ArrayList<Cerradura> estadosCerraduras = new ArrayList<>();
    public LinkedHashMap<String[],Integer> productionsKeys;

    public LR_Generator2() {
    }

    public LR_Generator2(int ind)
    {
        this.ind=ind;

    }

    public HashSet<String> getNextFromNoTerminal(String nt)
    {
        AtomicReference<HashSet<String>> set = new AtomicReference<>();

        list.forEach(n->{
            if(nt.equals(n.nt))
               set.set(n.nexts);
        });

        return set.get();
    }

    public void build()
    {
        try (FileInputStream fileInputStream  = new FileInputStream(file)) {

            terminals = new ArrayList<>();
            productionsKeys = new LinkedHashMap<>();
            extractData(fileInputStream);
            run(true);
            info();
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public transient static LR_Generator2 instance;
    {
        instance=this;
    }

    public void setInd(int ind) {
        this.ind = ind;
    }

    protected int getStateOf(String nterm,String prod[],String cameFrom)
    {
        AtomicInteger estado = new AtomicInteger(-1);

            estadosCerraduras.stream().filter(
                    cerradura -> cerradura.NtcameFrom!=null && cerradura.NtcameFrom.equals(cameFrom)
            ).forEach(c -> {
              //  System.out.println("Checking state: "+c.estadoCer);
                c.parameters.forEach(prc -> {
                    if (prc.no_terminal.equals(nterm) && prc.compare(prod))
                    {
                        estado.set(c.estadoCer);
                    }

                });

            });

        return estado.get();
    }

    protected void addCerradura(String noTerminal,String produccion[],int estado,String cameFrom,
                             int cameEdoFrom, int pos){
        for(Cerradura c : estadosCerraduras){
            if(c.estadoCer==estado){

                c.addParameter(new ProduccionCerradura(pos,noTerminal,produccion));
                return;
            }
        }
        Cerradura c = new Cerradura(estado,new ArrayList(){{
            add(new ProduccionCerradura(pos,noTerminal,produccion));}},
                cameFrom,cameEdoFrom);
        estadosCerraduras.add(c);
    }

    protected int[] nextPointer(String nterm,String[] prod){
            int pos=-1,edo=-1;

            for(Cerradura edoC : estadosCerraduras)
            {
                lbl:
                {
                    for(ProduccionCerradura it : edoC.parameters)
                    {
                        lbl2:
                        {
                            if (it.no_terminal.equals(nterm) && prod.length == it.produccion.length) {
                                for (int i = 0; i < prod.length; i++) {
                                    if (!prod[i].equals(it.produccion[i]))
                                        break lbl2;
                                }
                                pos=it.position+1;
                                edo=estadoActual;
                                break lbl;
                            }
                        }
                    }
                }
            }

        if(pos==-1)
            return null;
        estadoActual++;
        return new int[]{pos,edo};
    }

    protected void cerradura()
    {
        estadosCerraduras.add(new Cerradura(estadoActual++,new ArrayList(){{
            add(new ProduccionCerradura(0,
                    list.get(0).nt,
                    list.get(0).productions.iterator().next().split(" ")));
        }},null,-1));

        int s=0;
        while(s<estadosCerraduras.size())
        {
            estadosCerraduras.get(s++).analyze();
        }
        //System.out.println(estadosCerraduras.size());
        Scanner ls = new Scanner(System.in);
        while(true)
        {
            System.out.println("Type a edo: ");
            int l = ls.nextInt();
            if(l==-1){
                estadosCerraduras.forEach(System.out::println);
                break;
            }
            System.out.println(estadosCerraduras.get(l));
        }
    }

    protected int searchNT(String nt){
            for(int i=0; i<list.size(); i++){
                if(nt.equals(list.get(i).nt))
                    return i;
            }

            return -1;
        }

    protected int getNumProduction(String nt,String prod[])
    {
        AtomicInteger v = new AtomicInteger(-1);
        productionsKeys.forEach((key,value)->{
            if(key[0].equals(nt)  && Arrays.equals(key[1].split(" "),prod))
                v.set(value);
        });
        return v.get();
    }

    int currentNT=0;
    protected void addProduction(String nt, String data)
        {
            int ind = searchNT(nt);

            if(ind!=-1){
                list.get(ind).addProduction(data);
                productionsKeys.put(new String[]{nt,data},++currentNT);
            }else{
                System.err.println("Not found");
                System.out.println("Nt -> "+nt+" : data -> "+data);
                System.exit(-1);
            }
        }

    protected void addNewNT(String name)
        {
            if(list.isEmpty()) {
                list.add(new NotTerminal(name + "'"));
                list.get(0).addProduction(name);
            }

            list.add(new NotTerminal(name));
        }


    public void info(){
//        System.out.println("Terminals: "+terminals);
//        list.forEach(System.out::println);
//        productionsKeys.forEach((k,v)->
//            System.out.println(Arrays.toString(k) +" --> "+v)
//        );
//        estadosCerraduras.forEach(System.out::println);
    }

    private void save(){
        Serializa.saveObject(this,new File(Ejecutar.pathFiles+"/LR_object.gram"));
    }

    public LR_Generator2 load(){
        LR_Generator2 obj = (LR_Generator2) Serializa.writeObject(Ejecutar.class.getResourceAsStream
                ("/files/LR_object.gram"));

        this.list = obj.list;
        this.terminals = obj.terminals;
        this.productionsKeys = obj.productionsKeys;
        this.estadosCerraduras = obj.estadosCerraduras;

        return this;
    }

    private boolean again;
    private void run(boolean again)
    {
        String data;
        int index;

        if(again) {
            productionsKeys.put(new String[]{list.get(0).nt, list.get(1).nt}, 0);
//            productionsKeys.forEach((key, value) -> {
//                System.out.printf("%s --> %s = P%d\n", key[0], key[1], value);
//            });
            list.get(0).nexts.add("$");
        }
        //First
        for(int i=again ? list.size()-1 : 0; again ? i>=0 : i<list.size(); i = again ? i-1 : i+1){
            NotTerminal nt = list.get(i);

            for(String pr : nt.productions)
            {
                data = pr.split(" ")[0];

                if(data.equals(nt.nt))
                    continue;

                index = searchNT(data);
                if(index!=-1){
                    NotTerminal notTerminal = list.get(index);

                    nt.firsts.addAll(notTerminal.firsts);
                }
                else nt.firsts.add(data);
            }

        }

        //Next
        list.stream().filter(notTerminal -> !notTerminal.nt.equals(list.get(0).nt)).forEach
                (this::getNext);

        if(again)
            run(false);
        else {
            getNextNT();
            cerradura();
        }
    }

    private void getNextNT()
    {
        int ind;
        for(NotTerminal notTerminal1 : list)
        {
            ArrayList<String> toRemove = new ArrayList<>();
            ArrayList<String> toAdd = new ArrayList<>();

            for(String next : notTerminal1.nexts)
            {
                ind = searchNT(next);

                if(ind!=-1)
                {
                    toRemove.add(next);
                    toAdd.addAll(list.get(ind).firsts);
                }
            }

            toRemove.forEach(rmv->notTerminal1.nexts.remove(rmv));
            toAdd.forEach(ad->notTerminal1.nexts.add(ad));

        }
    }

    protected void getNext(NotTerminal notTerminal){
        list.forEach(nt -> {

            String array[];

            for(String pr : nt.productions)
            {
                array = pr.split(" ");

                for(int i=0; i<array.length; i++)
                {
                    if(notTerminal.nt.equals(array[i]))
                    {
                        if(i+1<array.length){

                                {

                                notTerminal.nexts.add(array[i+1]);
                            }


                        }else{
                            notTerminal.nexts.addAll(nt.nexts);
                        }
                    }
                }
            }

        });
    }

    private void extractData(FileInputStream fis) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(ind);
        Iterator<Row> itr = sheet.iterator();
        String type,value;
        while (itr.hasNext())
        {
            Row row = itr.next();
            if(row.getCell(0)==null)
                continue;
            type = row.getCell(0).getStringCellValue().trim();

            Iterator<Cell> cellIterator = row.cellIterator();

            cellIterator.next();

            while(cellIterator.hasNext())
            {
                value = cellIterator.next().getStringCellValue().trim();

                if(value.trim().isEmpty())
                    break;



                switch(type)
                {
                    case "TER":
                        terminals.add(value);
                        break;

                    case "N":
                        addNewNT(value);
                        break;

                    default:
                        addProduction(type,value);
                }
            }
        }

    }
}
