import java.util.ArrayList;

public class PattFunction extends Patt {
    String FUNCTIONString = "FUNCTION";

    public PattFunction(Closure f, int n, ArrayList<Pointer> pointers) {
        pattCells.add(FUNCTIONString);
        pattCells.add(f);
        pattCells.add(n);
        for (int i = 0; i < pointers.size(); i++) {
            pattCells.add(pointers.get(i));
        }
    }
}
