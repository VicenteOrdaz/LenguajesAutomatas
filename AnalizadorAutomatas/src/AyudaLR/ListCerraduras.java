package AyudaLR;

import java.util.ArrayList;

public class ListCerraduras extends ArrayList<ProduccionCerradura> {

    @Override
    public boolean add(ProduccionCerradura produccionCerradura) {

        int ind;
        for(ind=0; ind<this.size(); ind++)
        {
            if(this.get(ind).no_terminal.equals(produccionCerradura.no_terminal))
            {
                if(ind+1<this.size())
                {
                    if(!this.get(ind+1).no_terminal.equals(produccionCerradura.no_terminal))
                    {
                        super.add(ind+1,produccionCerradura);

                        return true;
                    }
                    else continue;
                }
                else{
                    super.add(ind+1,produccionCerradura);
                    return true;
                }

            }
        }

        return super.add(produccionCerradura);
    }

    @Override
    public boolean contains(Object o) {
        for(ProduccionCerradura pc : this)
        {
            if(pc.no_terminal.equals(o.toString()))
                return true;
        }
        return false;
    }
}
