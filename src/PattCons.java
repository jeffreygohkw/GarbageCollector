public class PattCons extends Patt {
    String CONSString = "CONS";

    public PattCons(int n, Pointer p1, Pointer p2) {
        pattCells.add(CONSString);
        pattCells.add(p1);
        pattCells.add(p2);
    }
}

