import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class Thread {
    public ArrayList<Pointer> thread;
    public GarbageCollector gc;

    public Thread() {
        thread = new ArrayList<>();
    }

    public ArrayList<Pointer> getRoots() {
        Set<Pointer> threadSet = new LinkedHashSet<Pointer>(thread);
        ArrayList<Pointer> result = new ArrayList<>();
        result.addAll(threadSet);
        return result;
    }

    public void addToThread(Pointer p) {
        thread.add(p);
    }

    /**
     * Remove the Pointer at a specified index from the thread
     * @param index
     */
    public void removeFromThread(int index) {
        thread.remove(index);
    }

    /**
     * Empties the thread
     */
    public void clearThread() {
        thread.clear();
    }

    public void setGC(GarbageCollector gc) {
        this.gc = gc;
    }
}
