package AyudaLR;

import java.io.Serializable;
import java.util.Arrays;

class ProduccionCerradura implements Serializable {

    public final int position;
    public String produccion[];
    public String no_terminal;

    public ProduccionCerradura(int pos, String no_terminal, String prod[]) {
        position = pos;
        this.no_terminal = no_terminal;
        produccion = prod;
    }

    public boolean compare(String prod[]){
        return Arrays.equals(this.produccion,prod);
    }

    public String getCurrentItem() {
        return (position < produccion.length) ? produccion[position] : null;
    }


    @Override
    public String toString() {
        return no_terminal +" --> ("+position+")"+
                Arrays.toString(produccion) + '\n';
    }
}
