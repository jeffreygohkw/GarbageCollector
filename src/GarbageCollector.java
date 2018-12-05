import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.util.Pair;

public class GarbageCollector {
    //A list of generations, the older generations are on the front of the list
    ArrayList<Generation> generations;
    ArrayList<Pair<Patt,Pointer>> referenceSet = new ArrayList<>();
    Heap heapobj = new Heap();
    int lastPointer = 0;
    Thread thread;

    public GarbageCollector(int initialGenSize) {
        generations = new ArrayList<>();
        generations.add(new Generation(0, initialGenSize));
    }

    public void updateThread(Thread t) {
        thread = t;
    }

    /**
     * Checks each generation from the oldest to the newest, once it finds the oldest generation that needs collection
     * run garbage collection from that generation to the newest generation.
     */
    public void checkGenerations() throws Exception {
        for (int i = 0; i < generations.size(); i++) {
            if (generations.get(i).reachedMax()) {
                collectGarbage(i);
            }
        }
    }

    /**
     * Collects the garbage from generation at index i and generations newer than that generation
     * @param i The oldest generation that needs collecting
     */
    public void collectGarbage(int i) throws Exception {
        for (int j = i; j < generations.size(); j++) {
            Generation g = generations.get(j);
            ArrayList<Patt> list1 = g.checkRoots(thread.getRoots());
            ArrayList<Patt> list2 = g.checkReferenceSet(referenceSet);
            ArrayList<Patt> merged = mergePattLists(list1, list2);
            ArrayList<Patt> scavenged = scavenge(merged, g);
            if (j == 0) {
                //If the generation is the oldest generation, add a new old generation that's double the size
                addOldGeneration(g.maxSize * 2);

                j += 1; //To ensure that j still points to the same generation
            }
            Generation older = generations.get(j - 1);
            //Move all the objects up one generation
            moveToGeneration(scavenged, g, older);
            //Get all the dead objects
            ArrayList<Pair<Patt,Pointer>> leftovers = g.getLeftovers(scavenged);
            //Remove them from reference set
            referenceSet.removeAll(leftovers);
            //Empty the generation
            g.clearGeneration();
            //Discard dead objects from the overall heap
            for (int x = 0; x < leftovers.size(); x++) {
                heapobj.removePatt(leftovers.get(x).getValue());
            }
            if (older.reachedMax()) {
                collectGarbage(generations.indexOf(older));
            }
        }
    }

    /**
     * Adds a generation to the end of generations
     */
    public void addNewGeneration(int genSize) {
        generations.add(new Generation(lastPointer, genSize));
        lastPointer += genSize;
    }

    /**
     * Adds a generation to the start of generations
     */
    public void addOldGeneration(int genSize) {
        generations.add(0, new Generation(lastPointer, genSize));
        lastPointer += genSize;
    }

    /**
     * Adds Patt object p to the generation at a defined index
     * If generation would exceed it's threshold when the object is added, do GC on the generation first
     * Assumes that the Patt object is smaller than the max size of the generation
     * @param p
     * @param index
     * @throws Exception
     */
    public void addPatt(Patt p, Pointer index) throws Exception {
        heapobj.addPatt(p, index);
        generations.get(generations.size() - 1).addToGeneration(index, p);
        // If youngest generation exceeded its threshold, do garbage collection on that generation
        if (generations.get(generations.size() - 1).reachedMax()) {
            collectGarbage(generations.size() - 1);
        }
    }

    /**
     * Removes Patt object at a defined index in the heap
     * This removes from the generation as well
     * @param index
     * @throws Exception
     */
    public void removePatt(Pointer index) throws Exception {
        int genIndex = 0;
        //Iterate through the generations until we find the one that contains the Patt at index
        for (int i = 0; i < generations.size(); i++) {
            if (generations.get(i).generation.containsKey(index)) {
                genIndex = i;
                break;
            }
        }
        //Remove it from both the generation and the heap
        generations.get(genIndex).generation.remove(index);
        heapobj.removePatt(index);
    }

    public ArrayList<Patt> mergePattLists(ArrayList<Patt> r1, ArrayList<Patt> r2) {
        //Merge the two lists, removing duplicates
        ArrayList<Patt> result = new ArrayList<>();
        result.addAll(r1);
        result.remove(r2);
        result.addAll(r2);
        return result;
    }

    /**
     *
     * @param pattArrayList Contains the list of objects in the generation g to scavenge
     * @param g The generation we are scavenging from
     */
    public ArrayList<Patt> scavenge(ArrayList<Patt> pattArrayList, Generation g) {
        for (int i = 0; i < pattArrayList.size(); i++) {
            Patt obj = pattArrayList.get(i);
            if (obj instanceof PattConstr || obj instanceof PattCons || obj instanceof PattInd) {
                ArrayList<Object> cells = obj.pattCells;
                for (int j = 1; j < cells.size(); j++) {
                    //Iterate through the object
                    if (cells.get(j) instanceof Pointer) {
                        //Ignore all the non Pointers
                        if (heapobj.checkPatt((Pointer) cells.get(j))) {
                            //If one of the pointers is pointing to an actual object,
                            // Add the object that the pointer comes from and the destination of the pointer to the reference set if it doesn't already  exist
                            Pair p = new Pair(obj, cells.get(j));
                            if (!referenceSet.contains(p)) {
                                referenceSet.add(p);
                            }
                            //If generation g houses the object the pointer is pointing to, add it to the list to scavenge if it isn't in already
                            if (g.generation.containsKey(cells.get(j))) {
                                if (!pattArrayList.contains(g.generation.get(cells.get(j)))) {
                                    pattArrayList.add(g.generation.get(cells.get(j)));
                                }
                            }
                        }
                    }
                }
            }
        }
        //Once done with all the objects in the pattArrayList with strong pointers, check again but with weak pointers
        for (int i = 0; i < pattArrayList.size(); i++) {
            Patt obj = pattArrayList.get(i);
            if (obj instanceof PattWeak) {
                ArrayList<Object> cells = obj.pattCells;
                if (heapobj.checkPatt((Pointer) cells.get(1))) {
                    for (int j = 0; i < referenceSet.size(); i++) {
                        if (referenceSet.get(j).getValue().equalPointer((Pointer) cells.get(1)))  {
                            //If reference set contains a pointer that matches the pointer of WEAK,
                            //Add to reference set if it isn't already in
                            Pair p = new Pair(obj, cells.get(1));
                            if (!referenceSet.contains(p)) {
                                referenceSet.add(p);
                            }
                            //If generation g houses the object the pointer is pointing to, add it to the list to scavenge if it isn't in already
                            if (g.generation.containsKey(cells.get(1))) {
                                if (!pattArrayList.contains(g.generation.get(cells.get(1)))) {
                                    pattArrayList.add(g.generation.get(cells.get(1)));
                                }
                            }
                        }
                    }
                }
            }
        }
        //Return the final pattArrayList
        return pattArrayList;
    }

    /**
     * Iterates through the previous generation, and adds objects to the new generation if it is found in toMove
     * @param toMove
     * @param gPrev
     * @param gNew
     */
    public void moveToGeneration(ArrayList<Patt> toMove, Generation gPrev, Generation gNew) throws Exception {
        HashMap<Pointer, Patt> generation = gPrev.generation;
        Iterator<HashMap.Entry<Pointer, Patt>> it = generation.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Pointer, Patt> pair = it.next();
            if (toMove.contains(pair.getValue())) {
                gNew.addToGeneration(pair.getKey(), pair.getValue());
            }
        }
    }
}
