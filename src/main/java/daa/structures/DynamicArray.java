package daa.structures;

import daa.metrics.Metrics;


public final class DynamicArray implements IntSequence {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        data = new int[initialCapacity];
    }

    @Override
    public void add(int x) {
        if (size == data.length) {
            grow();
        }
        data[size++] = x;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        if (size == data.length) {
            grow();
        }
        long shifted = 0;
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1]; // one cell read + one element moved
            shifted++;
        }
        data[index] = x;
        size++;
        metrics.steps += shifted;
        metrics.moves += shifted;
    }

    @Override
    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        int removed = data[index];
        long shifted = 0;
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1]; // one cell read + one element moved
            shifted++;
        }
        size--;
        metrics.steps += 1 + shifted; // 1 read for the removed element
        metrics.moves += shifted;
        return removed;
    }

    @Override
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        metrics.steps++;
        return data[index];
    }

    @Override
    public boolean contains(int x) {
        long steps = 0;
        long comparisons = 0;
        boolean found = false;
        for (int i = 0; i < size; i++) {
            steps++;
            comparisons++;
            if (data[i] == x) {
                found = true;
                break;
            }
        }
        metrics.steps += steps;
        metrics.comparisons += comparisons;
        return found;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private void grow() {
        if (data.length > Integer.MAX_VALUE / 2) {
            throw new IllegalStateException("capacity limit reached");
        }
        int[] bigger = new int[data.length * 2];
        long copied = 0;
        for (int i = 0; i < size; i++) {
            bigger[i] = data[i];
            copied++;
        }
        data = bigger;
        metrics.steps += copied;
        metrics.moves += copied;
    }
}