import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.util.Pair;

public class Generation {
    int startingPoint;
    HashMap<Pointer, Patt> generation;
    int maxSize;

    public Generation(int sp, int size) {
        startingPoint = sp;
        generation = new HashMap<>();
        maxSize = size;
    }

    /**
     * Signals whether or not the generation is full, and it's time to collect from the generation
     * @return true if the total size of objects in the generation
     * reaches or exceeds the maximum size a generation ought to have
     */
    public boolean reachedMax(){
        return this.getSize() >= maxSize;
    }

    /**
     * Checks the total size of the objects in the generation
     */
    public int getSize(){
        int total = 0;
        for (Patt value : generation.values()) {
            total += value.pattCells.size();
        }
        return total;
    }

    /**
     * Returns all the Patts that are still alive
     * @param referenceSet The reference set which helps to check if a pointer is being pointed to from something in another generation and not the root directly
     * @return
     */
    public ArrayList<Patt> checkReferenceSet (ArrayList<Pair<Patt,Pointer>> referenceSet) {
        ArrayList<Patt> result = new ArrayList<>();
        Iterator<HashMap.Entry<Pointer, Patt>> it = generation.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Pointer, Patt> p = it.next();
            Pair<Patt, Pointer> pair = new Pair(p.getValue(), p.getKey());
            if (referenceSet.contains(pair)) {
                result.add(pair.getKey());
        }
        }
        return result;
    }

    /**
     * Check if any roots point to one of the objects in the generation
     * @param roots
     * @return
     */
    public ArrayList<Patt> checkRoots(ArrayList<Pointer> roots) {
        ArrayList<Patt> result = new ArrayList<>();
        Iterator<HashMap.Entry<Pointer, Patt>> it = generation.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Pointer, Patt> pair = it.next();
            if (roots.contains(pair.getKey())) {
                result.add(pair.getValue());
            }
        }
        return result;
    }

    public void addToGeneration(Pointer p, Patt patt) {
        generation.put(p, patt);
    }

    public ArrayList<Pair<Patt,Pointer>> getLeftovers(ArrayList<Patt> inputList) {
        ArrayList<Pair<Patt,Pointer>> result = new ArrayList<>();
        Iterator<HashMap.Entry<Pointer, Patt>> it = generation.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Pointer, Patt> pair = it.next();
            if (!inputList.contains(pair.getValue())) {
                Pair<Patt, Pointer> p = new Pair(pair.getValue(), pair.getKey());
                result.add(p);
            }
        }
        return result;
    }

    public void clearGeneration() {
        generation.clear();
    }
}
