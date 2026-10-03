package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

abstract class IntSequenceContract {

    abstract IntSequence create();

    private static void assertSame(List<Integer> expected, IntSequence actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).intValue(), actual.get(i), "index " + i);
        }
    }

    @Test
    void randomOperationsMatchArrayList() {
        Random rnd = new Random(42);
        IntSequence s = create();
        List<Integer> expected = new ArrayList<>();
        for (int step = 0; step < 5_000; step++) {
            int op = rnd.nextInt(4);
            if (op == 0 || expected.isEmpty()) {
                int x = rnd.nextInt(100);
                s.add(x);
                expected.add(x);
            } else if (op == 1) {
                int idx = rnd.nextInt(expected.size() + 1);
                int x = rnd.nextInt(100);
                s.add(idx, x);
                expected.add(idx, x);
            } else if (op == 2) {
                int idx = rnd.nextInt(expected.size());
                assertEquals(expected.remove(idx).intValue(), s.remove(idx));
            } else {
                int x = rnd.nextInt(100);
                assertEquals(expected.contains(x), s.contains(x));
            }
            assertEquals(expected.size(), s.size());
        }
        assertSame(expected, s);
    }

    @Test
    void emptyStructure() {
        IntSequence s = create();
        assertEquals(0, s.size());
        assertFalse(s.contains(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(1, 5));
    }

    @Test
    void oneElement() {
        IntSequence s = create();
        s.add(7);
        assertEquals(1, s.size());
        assertEquals(7, s.get(0));
        assertTrue(s.contains(7));
        assertFalse(s.contains(8));
        assertEquals(7, s.remove(0));
        assertEquals(0, s.size());
        s.add(0, 9); // structure is reusable after becoming empty
        assertEquals(9, s.get(0));
    }

    @Test
    void duplicateValues() {
        IntSequence s = create();
        for (int i = 0; i < 5; i++) {
            s.add(3);
        }
        s.add(2, 4);
        assertEquals(6, s.size());
        assertEquals(4, s.get(2));
        assertEquals(3, s.remove(0));
        assertTrue(s.contains(3));
        for (int i = 0; i < 4; i++) {
            assertEquals(i == 1 ? 4 : 3, s.remove(0));
        }
        assertEquals(1, s.size());
        assertEquals(3, s.get(0));
    }

    @Test
    void firstAndLastIndex() {
        IntSequence s = create();
        for (int i = 0; i < 20; i++) {
            s.add(i); // crosses several array growths
        }
        assertEquals(0, s.get(0));
        assertEquals(19, s.get(19));
        s.add(0, -1);
        s.add(s.size(), 100); // insert at index == size appends
        assertEquals(-1, s.get(0));
        assertEquals(100, s.get(s.size() - 1));
        assertEquals(100, s.remove(s.size() - 1));
        assertEquals(-1, s.remove(0));
        assertEquals(19, s.get(s.size() - 1));
        s.add(19); // tail must still be valid after removing the last node
        assertEquals(19, s.get(s.size() - 1));
    }

    @Test
    void invalidIndexThrows() {
        IntSequence s = create();
        s.add(1);
        s.add(2);
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(2));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(3, 0));
        assertEquals(2, s.size()); // failed calls must not change the structure
    }

    @Test
    void largeFillKeepsOrder() {
        IntSequence s = create();
        for (int i = 0; i < 10_000; i++) {
            s.add(i * 2);
        }
        assertEquals(10_000, s.size());
        for (int i = 0; i < 10_000; i += 997) {
            assertEquals(i * 2, s.get(i));
        }
        assertTrue(s.contains(19_998));
        assertFalse(s.contains(19_999));
    }
}