import java.util.ArrayList;
import java.util.HashMap;

public class Heap {
    HashMap<Integer, Patt> fullHeap;

    public Heap() {
        fullHeap = new HashMap<>();
    }

    public void addPatt(Patt p, Pointer index) throws Exception {
        int psize = p.pattCells.size();
        //Check if p can fit in the space starting from index
        for (int i = 0; i < psize; i++) {
            if (fullHeap.containsKey(i + index.pointer)) {
                throw new Exception("Patt can't fit in that space.");
            }
        }
        //Add p to fullHeap at index
        fullHeap.put(index.pointer, p);
        // Mark all the spaces that p occupies as taken
        for (int i = 1; i < psize; i++) {
            fullHeap.put(index.pointer + i, null);
        }
    }

    public void removePatt(Pointer p) throws Exception {
        int index = p.pointer;
        if (checkPatt(p)) {
            Patt obj = fullHeap.get(index);
            int psize = obj.pattCells.size();
            for (int i = 0; i < psize; i++) {
                fullHeap.remove(index + i);
            }
        }
        else {
            //No object located at that index or it is not the starting index of an object
            return;
        }
    }

    //Checks if there is an object that starts at that index
    public boolean checkPatt(Pointer p) {
        int index = p.pointer;
        if (fullHeap.containsKey(index)) {
            Patt obj = fullHeap.get(index);
            if (obj == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


}
