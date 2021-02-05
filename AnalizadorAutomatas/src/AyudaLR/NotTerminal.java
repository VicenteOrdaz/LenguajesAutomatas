package AyudaLR;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class NotTerminal implements Serializable {
    public String nt;
    public LinkedHashSet<String> productions;
    public HashSet<String> firsts;
    public HashSet<String> nexts;

    public NotTerminal(String name) {
        nt = name;

        productions = new LinkedHashSet<>();
        firsts = new HashSet<>();
        nexts = new HashSet<>();
    }


    public void addProduction(String prod) {
        productions.add(prod);
    }

    @Override
    public String toString() {
        return "NotTerminal{" +
                "nt='" + nt + '\'' +
                ", productions=" + productions +
                ", firsts=" + firsts +
                ", nexts=" + nexts +
                '}';
    }
}
