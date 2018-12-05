import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.rules.Stopwatch;

public class GarbageCollectorTest {
    private Thread thread = new Thread();
    private GarbageCollector gc = new GarbageCollector(4);
    private Heap heap = gc.heapobj;
    private int initialGenSize = 4;

    @Test
    public void addToThread() {
        thread.clearThread();
        Pointer zero = new Pointer(0);
        Pointer two = new Pointer(2);
        thread.addToThread(zero);
        thread.addToThread(two);
        thread.addToThread(zero);
        ArrayList<Pointer> testArray = new ArrayList<>();
        testArray.add(zero);
        testArray.add(two);
        ArrayList<Pointer> roots = thread.getRoots();

        //Check if the duplicate zero pointer is dropped
        assertTrue(roots.size() == 2);
        //Check if roots match the expected output
        for (int i = 0; i < roots.size(); i++) {
            assertEquals(roots.get(i), testArray.get(i));
        }
    }

    @Test
    public void testGCNoPattPointersAllLive() throws Exception {
        thread.clearThread();

        PattInt one = new PattInt(1);
        PattNull n = new PattNull();
        Patt trueBool = new PattBool(true);
        Pointer zeroPointer = new Pointer(0);
        Pointer twoPointer = new Pointer(2);
        Pointer threePointer = new Pointer(3);
        Pointer fivePointer = new Pointer(5);

        thread.addToThread(zeroPointer);
        thread.addToThread(twoPointer);
        thread.addToThread(threePointer);
        //Update the thread in gc once we're done updating it.
        gc.updateThread(thread);

        //Add the Patt objects to the heap,
        gc.addPatt(one, zeroPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(one, gc.generations.get(0).generation.get(zeroPointer));

        gc.addPatt(n, twoPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(twoPointer));
        assertEquals(n, gc.generations.get(0).generation.get(twoPointer));

        gc.addPatt(trueBool, threePointer);
        //At this point, there should have been one round of garbage collection, since all three are in the thread, they should all be in gen 1

        //There are two generations now
        assertTrue(gc.generations.size() == 2);

        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(one, gc.generations.get(0).generation.get(zeroPointer));
        assertTrue(gc.generations.get(0).generation.containsKey(twoPointer));
        assertEquals(n, gc.generations.get(0).generation.get(twoPointer));
        assertTrue(gc.generations.get(0).generation.containsKey(threePointer));
        assertEquals(trueBool, gc.generations.get(0).generation.get(threePointer));


        //The new addition should be in gen 0
        gc.addPatt(n, fivePointer);
        assertTrue(gc.generations.get(1).generation.containsKey(fivePointer));
        assertEquals(n, gc.generations.get(1).generation.get(fivePointer));

        //Check if generation sizes are 8 for gen 1 and 4 for gen 0
        assertEquals(initialGenSize * 2, gc.generations.get(0).maxSize);
        assertEquals(initialGenSize, gc.generations.get(1).maxSize);
    }

    @Test
    public void testGCNoPattPointersSomeLive() throws Exception {
        thread.clearThread();

        PattInt one = new PattInt(1);
        PattNull n = new PattNull();
        Patt trueBool = new PattBool(true);
        Pointer zeroPointer = new Pointer(0);
        Pointer twoPointer = new Pointer(2);
        Pointer threePointer = new Pointer(3);
        Pointer fivePointer = new Pointer(5);

        thread.addToThread(zeroPointer);
        thread.addToThread(threePointer);
        //Update the thread in gc once we're done updating it.
        gc.updateThread(thread);

        //Add the Patt objects to the heap,
        gc.addPatt(one, zeroPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(one, gc.generations.get(0).generation.get(zeroPointer));

        gc.addPatt(n, twoPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(twoPointer));
        assertEquals(n, gc.generations.get(0).generation.get(twoPointer));

        gc.addPatt(trueBool, threePointer);
        //At this point, there should have been one round of garbage collection,

        //There are two generations now
        assertTrue(gc.generations.size() == 2);

        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(one, gc.generations.get(0).generation.get(zeroPointer));

        //Since PattNull at twoPointer is not part of the thread, it should not be in either generation or in the heap
        assertFalse(gc.generations.get(0).generation.containsKey(twoPointer));
        assertFalse(gc.generations.get(1).generation.containsKey(twoPointer));
        assertFalse(heap.fullHeap.containsKey(twoPointer.pointer));

        assertTrue(gc.generations.get(0).generation.containsKey(threePointer));
        assertEquals(trueBool, gc.generations.get(0).generation.get(threePointer));

        //The new addition should be in gen 0
        gc.addPatt(n, fivePointer);
        assertTrue(gc.generations.get(1).generation.containsKey(fivePointer));
        assertEquals(n, gc.generations.get(1).generation.get(fivePointer));

        //Check if generation sizes are 8 for gen 1 and 4 for gen 0
        assertEquals(initialGenSize * 2, gc.generations.get(0).maxSize);
        assertEquals(initialGenSize, gc.generations.get(1).maxSize);
    }

    @Test
    public void testGCPointersSomeLive() throws Exception {
        thread.clearThread();

        Pointer zeroPointer = new Pointer(0);
        Pointer twoPointer = new Pointer(2);
        Pointer threePointer = new Pointer(3);
        Pointer fivePointer = new Pointer(5);

        PattInd threeP = new PattInd(threePointer);
        PattNull n = new PattNull();
        Patt trueBool = new PattBool(true);

        thread.addToThread(zeroPointer);
        //Update the thread in gc once we're done updating it.
        gc.updateThread(thread);

        //Add the Patt objects to the heap,
        gc.addPatt(threeP, zeroPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(0).generation.get(zeroPointer));

        gc.addPatt(n, twoPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(twoPointer));
        assertEquals(n, gc.generations.get(0).generation.get(twoPointer));

        gc.addPatt(trueBool, threePointer);
        //At this point, there should have been one round of garbage collection,

        //There are two generations now
        assertTrue(gc.generations.size() == 2);

        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(0).generation.get(zeroPointer));
        assertFalse(gc.generations.get(1).generation.containsKey(zeroPointer));

        //Since PattNull at twoPointer is not part of the roots and is not pointed to by any other object, it should not be in either generation or in the heap
        assertFalse(gc.generations.get(0).generation.containsKey(twoPointer));
        assertFalse(gc.generations.get(1).generation.containsKey(twoPointer));
        assertFalse(heap.fullHeap.containsKey(twoPointer.pointer));

        //However, since threePointer is being pointed to by zeroPointer, trueBool should also be in Generation 1
        assertTrue(gc.generations.get(0).generation.containsKey(threePointer));
        assertEquals(trueBool, gc.generations.get(0).generation.get(threePointer));
        assertFalse(gc.generations.get(1).generation.containsKey(threePointer));

        //The new addition should be in gen 0
        gc.addPatt(n, fivePointer);
        assertTrue(gc.generations.get(1).generation.containsKey(fivePointer));
        assertEquals(n, gc.generations.get(1).generation.get(fivePointer));

        //Check if generation sizes are 8 for gen 1 and 4 for gen 0
        assertEquals(initialGenSize * 2, gc.generations.get(0).maxSize);
        assertEquals(initialGenSize, gc.generations.get(1).maxSize);
    }

    @Test
    public void testThreeGenerations() throws Exception {
        thread.clearThread();

        Pointer zeroPointer = new Pointer(0);
        Pointer twoPointer = new Pointer(2);
        Pointer threePointer = new Pointer(3);
        Pointer fivePointer = new Pointer(5);
        Pointer sevenPointer = new Pointer(7);
        Pointer ninePointer = new Pointer(9);

        PattInd threeP = new PattInd(threePointer);
        PattNull n = new PattNull();
        Patt trueBool = new PattBool(true);
        Patt falseBool = new PattBool(false);
        Patt intOne = new PattInt(1);
        Patt intTwo = new PattInt(2);

        thread.addToThread(zeroPointer);
        thread.addToThread(fivePointer);
        thread.addToThread(sevenPointer);
        //Update the thread in gc once we're done updating it.
        gc.updateThread(thread);

        //Add the Patt objects to the heap,
        gc.addPatt(threeP, zeroPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(0).generation.get(zeroPointer));

        gc.addPatt(n, twoPointer);
        assertTrue(gc.generations.get(0).generation.containsKey(twoPointer));
        assertEquals(n, gc.generations.get(0).generation.get(twoPointer));

        gc.addPatt(trueBool, threePointer);
        //At this point, there should have been one round of garbage collection,
        //There are two generations now, gen 1 0/4, gen 2 4/8
        assertTrue(gc.generations.size() == 2);

        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(0).generation.get(zeroPointer));

        //Since PattNull at twoPointer is not part of the thread, it should not be in either generation or in the heap
        assertFalse(gc.generations.get(0).generation.containsKey(twoPointer));
        assertFalse(gc.generations.get(1).generation.containsKey(twoPointer));
        assertFalse(heap.fullHeap.containsKey(twoPointer.pointer));

        //However, since threePointer is being pointed to by zeroPointer, trueBool should also be in Generation 1
        assertTrue(gc.generations.get(0).generation.containsKey(threePointer));
        assertEquals(trueBool, gc.generations.get(0).generation.get(threePointer));

        //The new addition should be in gen 0
        gc.addPatt(intOne, fivePointer);
        assertTrue(gc.generations.get(1).generation.containsKey(fivePointer));
        assertEquals(intOne, gc.generations.get(1).generation.get(fivePointer));

        //Gen 1 now is 2/8 full
        gc.removePatt(zeroPointer);
        assertFalse(gc.generations.get(0).generation.containsKey(zeroPointer));

        gc.addPatt(falseBool, sevenPointer);

        // Gen 0 full, both objects move to gen 1, gen 1 6/8 full
        assertTrue(gc.generations.get(0).generation.containsKey(sevenPointer));
        assertEquals(falseBool, gc.generations.get(0).generation.get(sevenPointer));

        gc.addPatt(threeP, zeroPointer);
        assertTrue(gc.generations.get(1).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(1).generation.get(zeroPointer));

        gc.addPatt(intTwo, ninePointer);
        //Gen 0 full, intTwo discarded, threeP moves to gen 1, gen 1 full, all 4 objects move to gen 2 since they are all live
        //Gen 0 and Gen 1 should both be empty
        assertTrue(gc.generations.get(0).generation.containsKey(zeroPointer));
        assertEquals(threeP, gc.generations.get(0).generation.get(zeroPointer));
        assertTrue(gc.generations.get(0).generation.containsKey(threePointer));
        assertEquals(trueBool, gc.generations.get(0).generation.get(threePointer));
        assertTrue(gc.generations.get(0).generation.containsKey(fivePointer));
        assertEquals(intOne, gc.generations.get(0).generation.get(fivePointer));
        assertTrue(gc.generations.get(0).generation.containsKey(sevenPointer));
        assertEquals(falseBool, gc.generations.get(0).generation.get(sevenPointer));
        assertEquals(0, gc.generations.get(1).getSize());
        assertEquals(0, gc.generations.get(2).getSize());

        //Check if generation sizes are 16 for gen 2, 8 for gen 1 and 4 for gen 0
        assertEquals(3, gc.generations.size());
        assertEquals(16, gc.generations.get(0).maxSize);
        assertEquals(initialGenSize * 2, gc.generations.get(1).maxSize);
        assertEquals(initialGenSize, gc.generations.get(2).maxSize);

    }

    @Test
    public void testComplexity() throws Exception {
        thread.clearThread();
        thread.addToThread(new Pointer(0));
        gc.updateThread(thread);
        int index = 0;
        ArrayList<Integer> gp = new ArrayList<>();
        int highest = 2;
        while (highest < 200000000) {
            gp.add(highest);
            highest *= 2;
        }
        System.out.println(gp);
        int size  = gp.size();
        for (int i = 0; i < 100000000; i+= 2) {
            Pointer pCurrent = new Pointer(i);
            Pointer pNext = new Pointer(i + 2);
            PattInd pi = new PattInd(pNext);
            if (index < size) {
                if (i == gp.get(index)) {
                    index += 1;
                    Long timeStart = System.nanoTime();
                    gc.addPatt(pi, pCurrent);
                    Long timeEnd = System.nanoTime();
                    Long timeElapsed = timeEnd - timeStart;
                    System.out.println(index + " " + timeElapsed.toString());
                } else {
                    gc.addPatt(pi, pCurrent);
                }
            } else {
                gc.addPatt(pi, pCurrent);
            }
        }
    }
}
