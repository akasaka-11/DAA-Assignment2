package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DynamicArrayTest extends IntSequenceContract {
    @Override
    IntSequence create() {
        return new DynamicArray(2); // small capacity forces many resizes
    }

    @Test
    void invalidCapacityRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicArray(0));
    }

    @Test
    void resizingDoublesAndCopiesEveryElementOnce() {
        DynamicArray a = new DynamicArray(4);
        for (int i = 0; i < 4; i++) {
            a.add(i);
        }
        assertEquals(0, a.metrics().moves); // no growth yet
        a.add(4); // full -> grow 4 -> 8, copies 4 elements
        assertEquals(4, a.metrics().moves);
        for (int i = 5; i < 8; i++) {
            a.add(i); // fits into the doubled array
        }
        assertEquals(4, a.metrics().moves);
        a.add(8); // grow 8 -> 16, copies 8 more
        assertEquals(12, a.metrics().moves);
    }
}