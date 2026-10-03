package daa.structures;

import daa.metrics.Metrics;


public final class MinHeap {
    private static final int DEFAULT_CAPACITY = 16;

    private int[] a;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        a = new int[DEFAULT_CAPACITY];
    }

    public void insert(int x) {
        if (size == a.length) {
            grow();
        }
        int i = size++;
        long steps = 0;
        long moves = 0;
        long comparisons = 0;
        while (i > 0) {
            int p = (i - 1) >>> 1;
            int pv = a[p];
            steps++;
            comparisons++;
            if (pv <= x) {
                break;
            }
            a[i] = pv; // parent moves down into the hole
            moves++;
            i = p;
        }
        a[i] = x;
        metrics.steps += steps;
        metrics.moves += moves;
        metrics.comparisons += comparisons;
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.steps++;
        return a[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        int min = a[0];
        metrics.steps++;
        size--;
        if (size > 0) {
            int last = a[size];
            metrics.steps++;
            siftDown(0, last);
        }
        return min;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Metrics metrics() {
        return metrics;
    }

    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = a[i];
        }
        return copy;
    }


    private void siftDown(int start, int x) {
        int n = size;
        int i = start;
        long steps = 0;
        long moves = 0;
        long comparisons = 0;
        while (true) {
            int c = 2 * i + 1;
            if (c >= n) {
                break;
            }
            int cv = a[c];
            steps++;
            if (c + 1 < n) {
                int rv = a[c + 1];
                steps++;
                comparisons++;
                if (rv < cv) {
                    c++;
                    cv = rv;
                }
            }
            comparisons++;
            if (x <= cv) {
                break;
            }
            a[i] = cv; // smaller child moves up into the hole
            moves++;
            i = c;
        }
        a[i] = x;
        metrics.steps += steps;
        metrics.moves += moves;
        metrics.comparisons += comparisons;
    }

    private void grow() {
        if (a.length > Integer.MAX_VALUE / 2) {
            throw new IllegalStateException("capacity limit reached");
        }
        int[] bigger = new int[a.length * 2];
        long copied = 0;
        for (int i = 0; i < size; i++) {
            bigger[i] = a[i];
            copied++;
        }
        a = bigger;
        metrics.steps += copied;
        metrics.moves += copied;
    }
}