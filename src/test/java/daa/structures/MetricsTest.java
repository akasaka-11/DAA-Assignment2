package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MetricsTest {

    private static DynamicArray filledArray(int n) {
        DynamicArray a = new DynamicArray();
        for (int i = 0; i < n; i++) {
            a.add(i);
        }
        a.metrics().reset();
        return a;
    }

    private static MyLinkedList filledList(int n) {
        MyLinkedList l = new MyLinkedList();
        for (int i = 0; i < n; i++) {
            l.add(i);
        }
        l.metrics().reset();
        return l;
    }

    @Test
    void getCostsOneStepInArrayAndIndexPlusOneInList() {
        DynamicArray a = filledArray(100);
        MyLinkedList l = filledList(100);
        a.get(70);
        l.get(70);
        assertEquals(1, a.metrics().steps);
        assertEquals(71, l.metrics().steps);
    }

    @Test
    void containsAbsentScansEverything() {
        DynamicArray a = filledArray(50);
        MyLinkedList l = filledList(50);
        assertFalse(a.contains(-1));
        assertFalse(l.contains(-1));
        assertEquals(50, a.metrics().steps);
        assertEquals(50, a.metrics().comparisons);
        assertEquals(50, l.metrics().steps);
        assertEquals(50, l.metrics().comparisons);
    }

    @Test
    void headInsertShiftsEveryElementInArrayButTouchesTwoLinksInList() {
        DynamicArray a = filledArray(100);
        MyLinkedList l = filledList(100);
        a.add(0, -1);
        l.add(0, -1);
        assertEquals(100, a.metrics().moves);
        assertEquals(2, l.metrics().moves);
        assertEquals(0, l.metrics().steps); // no traversal needed at the head
    }

    @Test
    void middleOperationsAreNeverReportedAsZeroForTheList() {
        MyLinkedList l = filledList(100);
        l.add(50, -1);
        assertTrue(l.metrics().steps > 0);
        assertTrue(l.metrics().moves > 0);
        l.metrics().reset();
        l.remove(50);
        assertTrue(l.metrics().steps > 0);
        assertTrue(l.metrics().moves > 0);
    }

    @Test
    void middleInsertCostsRoughlyHalfTheSizeInBothStructures() {
        DynamicArray a = filledArray(100);
        MyLinkedList l = filledList(100);
        a.add(50, -1);
        l.add(50, -1);
        assertEquals(50, a.metrics().moves); // elements 50..99 shifted
        assertEquals(50, l.metrics().steps); // nodes 0..49 visited
        assertEquals(2, l.metrics().moves);
    }

    @Test
    void heapOperationsCountComparisons() {
        MinHeap h = new MinHeap();
        for (int i = 10; i > 0; i--) {
            h.insert(i); // descending input: every insert bubbles up
        }
        assertTrue(h.metrics().comparisons > 0);
        assertTrue(h.metrics().moves > 0);
        h.metrics().reset();
        h.extractMin();
        assertTrue(h.metrics().comparisons > 0);
    }
}