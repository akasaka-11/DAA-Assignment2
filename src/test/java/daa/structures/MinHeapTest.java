package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.PriorityQueue;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {

    private static void assertHeapProperty(MinHeap h) {
        int[] a = h.toArray();
        for (int child = 1; child < a.length; child++) {
            int parent = (child - 1) / 2;
            assertTrue(a[parent] <= a[child], "a[" + parent + "]=" + a[parent] + " > a[" + child + "]=" + a[child]);
        }
    }

    @Test
    void heapPropertyHoldsAfterEveryInsertAndExtractMin() {
        Random rnd = new Random(42);
        MinHeap h = new MinHeap();
        for (int i = 0; i < 1_000; i++) {
            h.insert(rnd.nextInt(500)); // duplicates included
            assertHeapProperty(h);
        }
        while (!h.isEmpty()) {
            h.extractMin();
            assertHeapProperty(h);
        }
    }

    @Test
    void matchesPriorityQueueOnMixedOperations() {
        Random rnd = new Random(7);
        MinHeap h = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int step = 0; step < 20_000; step++) {
            if (expected.isEmpty() || rnd.nextInt(3) != 0) {
                int x = rnd.nextInt(1_000) - 500;
                h.insert(x);
                expected.add(x);
            } else {
                assertEquals(expected.peek().intValue(), h.peekMin());
                assertEquals(expected.poll().intValue(), h.extractMin());
            }
            assertEquals(expected.size(), h.size());
        }
    }

    @Test
    void extractMinReturnsNonDecreasingValues() {
        Random rnd = new Random(42);
        int n = 10_000;
        MinHeap h = new MinHeap();
        for (int i = 0; i < n; i++) {
            h.insert(rnd.nextInt());
        }
        int prev = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int v = h.extractMin();
            assertTrue(prev <= v, "order broken at call " + i);
            prev = v;
        }
        assertTrue(h.isEmpty());
    }

    @Test
    void emptyHeapThrowsIllegalState() {
        MinHeap h = new MinHeap();
        assertThrows(IllegalStateException.class, h::peekMin);
        assertThrows(IllegalStateException.class, h::extractMin);
        h.insert(1);
        h.extractMin();
        assertThrows(IllegalStateException.class, h::peekMin); // empty again after use
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    void oneElement() {
        MinHeap h = new MinHeap();
        h.insert(5);
        assertEquals(5, h.peekMin());
        assertEquals(1, h.size());
        assertEquals(5, h.extractMin());
        assertEquals(0, h.size());
    }

    @Test
    void duplicatesAndExtremeValues() {
        MinHeap h = new MinHeap();
        int[] in = {3, 3, 3, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 3};
        for (int x : in) {
            h.insert(x);
        }
        int[] out = {Integer.MIN_VALUE, 0, 3, 3, 3, 3, Integer.MAX_VALUE};
        for (int expected : out) {
            assertEquals(expected, h.extractMin());
        }
    }

    @Test
    void peekMinDoesNotRemove() {
        MinHeap h = new MinHeap();
        h.insert(4);
        h.insert(2);
        assertEquals(2, h.peekMin());
        assertEquals(2, h.peekMin());
        assertEquals(2, h.size());
    }

    @Test
    void growsPastInitialCapacity() {
        MinHeap h = new MinHeap();
        for (int i = 200; i > 0; i--) {
            h.insert(i);
        }
        assertEquals(1, h.peekMin());
        assertEquals(200, h.size());
        assertHeapProperty(h);
    }
}