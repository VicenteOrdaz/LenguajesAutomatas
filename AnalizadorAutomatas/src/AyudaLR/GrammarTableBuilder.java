package AyudaLR;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static AyudaLR.LR_Generator2.file;

public class GrammarTableBuilder
{
    private LR_Generator2 ref;

    private int index4Table;

    private LinkedHashMap<String,Integer> terminalColumns;

    public GrammarTableBuilder(LR_Generator2 ref, int sheetIndex) {
        this.ref = ref;
        this.index4Table=sheetIndex;
        terminalColumns = new LinkedHashMap<>();
    }

    public void build(){
        try (FileInputStream fileInputStream  = new FileInputStream(file)) {
            extractData(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractData(FileInputStream fis) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(index4Table);
        Iterator<Row> it = sheet.iterator();

        ref.terminals.add("$");
        //Pone los terminales y no terminales en el primer renglon
        int ind=1,rowIndex=0;
        Row row = sheet.createRow(rowIndex++);

        for(String term : ref.terminals)
        {
            row.createCell(ind);
            row.getCell(ind).setCellValue(term);
            terminalColumns.put(term,ind);
            ind++;
        }

        for(NotTerminal nt : ref.list)
        {
            if(nt.nt.charAt(nt.nt.length()-1)=='\'')
                continue;
            row.createCell(ind);
            row.getCell(ind).setCellValue(nt.nt);
            terminalColumns.put(nt.nt,ind);
            ind++;
        }

        int mayor;
        for(int i=0; i<ref.estadosCerraduras.size(); i++)
        {
            row = sheet.createRow(rowIndex++);
            Cerradura cerradura = ref.estadosCerraduras.get(i);
            row.createCell(0);
            row.getCell(0).setCellValue(i);
            mayor=1;
            for(ProduccionCerradura pc : cerradura.sub_producciones)
            {
//                System.out.println("terminal columns: "+terminalColumns);
//                System.out.println("PC cuuretn: "+pc.getCurrentItem());
                ind = terminalColumns.get(pc.getCurrentItem());

                if(ind==-1){
                    System.err.println("Ind = -1, something it's wrong");
                    System.exit(-1);
                }

                if(mayor<=ind)
                {
                    for(;mayor<=ind; mayor++)
                    {
                        row.createCell(mayor);
                    }
                }

                Cerradura.Data data = cerradura.prodAndstates.get(pc.getCurrentItem());

                if(data==null){
                    System.err.println("Data is null, something it's wrong");
                    System.exit(-1);
                }


                row.getCell(ind).setCellValue(data.state);
            }

            //1302184428
//            Recorrer las producciones (P0,P1) que puede producir cada estado de cerradura, para añadirlos al excel
              for(Integer key : cerradura.producciones.keySet())
              {
                  HashSet<String> sig = ref.getNextFromNoTerminal(cerradura.producciones.get(key).no_terminal);

                  if(sig.isEmpty()){
                      System.err.println("Nexts are empty, something it's wrong");
                      System.exit(-1);
                  }

                  for(String sg : sig)
                  {
                      if(sg.trim().isEmpty())
                          continue;
                      System.out.println("Getting sig: "+sg);
                      ind = terminalColumns.get(sg);

                      if(ind==-1){
                          System.err.println("Ind = -1 (Productions|Next), something it's wrong");
                          System.exit(-1);
                      }

                      if(mayor<=ind)
                      {
                          for(;mayor<=ind; mayor++)
                          {
                              row.createCell(mayor);
                          }
                      }

                      if(row.getCell(ind).getCellTypeEnum()==CellType.NUMERIC){
                          System.err.println("Collision Productions and States");
                          System.exit(-1);
                      }

                      row.getCell(ind).setCellValue("P"+key);
                  }

              }
        }

        row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(0);


        wb.write(new FileOutputStream(file));

    }


}
