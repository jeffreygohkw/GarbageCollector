import java.util.ArrayList;

public class PattConstr extends Patt{
    String CONSTRString = "CONSTR";

    public PattConstr(int n, ArrayList<Pointer> pointers) {
        pattCells.add(CONSTRString);
        pattCells.add(n);
        for (int i = 0; i < pointers.size(); i++) {
            pattCells.add(pointers.get(i));
        }
    }
}
