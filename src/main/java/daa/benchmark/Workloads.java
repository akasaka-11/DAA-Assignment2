package daa.benchmark;

import daa.structures.IntSequence;
import daa.structures.MinHeap;
import java.util.Random;


final class Workloads {
    static final long SEED = 42;
    static final int GETS = 10_000;
    static final int QUERIES = 1_000;
    static final int EDITS = 1_000;

    /** One measurement: wall time of the operation phase plus the counters of that phase. */
    record Sample(long nanos, long steps, long moves, long comparisons) {}

    /** Accumulates results so the JIT cannot discard the measured calls as dead code. */
    static long sink;

    static final class Data {
        final int n;
        final int[] values;     // fill data
        final int[] getIndexes; // W1
        final int[] queries;    // W2: even positions present, odd positions absent
        final int[] inserts;    // W3

        Data(int n) {
            this.n = n;
            Random rnd = new Random(SEED);
            values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = rnd.nextInt(1_000_000);
            }
            getIndexes = new int[GETS];
            for (int i = 0; i < GETS; i++) {
                getIndexes[i] = rnd.nextInt(n);
            }
            queries = new int[QUERIES];
            for (int i = 0; i < QUERIES; i++) {
                queries[i] = (i % 2 == 0) ? values[rnd.nextInt(n)] : -1 - i; // values are >= 0, so negatives are absent
            }
            inserts = new int[EDITS];
            for (int i = 0; i < EDITS; i++) {
                inserts[i] = rnd.nextInt(1_000_000);
            }
        }
    }

    private static void fill(IntSequence s, int[] values) {
        for (int v : values) {
            s.add(v);
        }
        s.metrics().reset();
    }

    private static Sample sample(long nanos, daa.metrics.Metrics m) {
        return new Sample(nanos, m.steps, m.moves, m.comparisons);
    }

    /** W1 - Random Access: 10 000 get(index) calls with random indexes. */
    static Sample w1(IntSequence s, Data d) {
        fill(s, d.values);
        long acc = 0;
        long t0 = System.nanoTime();
        for (int idx : d.getIndexes) {
            acc += s.get(idx);
        }
        long elapsed = System.nanoTime() - t0;
        sink += acc;
        return sample(elapsed, s.metrics());
    }

    /** W2 - Search: 1 000 contains(x) queries, half present and half absent. */
    static Sample w2(IntSequence s, Data d) {
        fill(s, d.values);
        int found = 0;
        long t0 = System.nanoTime();
        for (int q : d.queries) {
            if (s.contains(q)) {
                found++;
            }
        }
        long elapsed = System.nanoTime() - t0;
        if (found != QUERIES / 2) {
            throw new IllegalStateException("expected " + QUERIES / 2 + " hits but got " + found);
        }
        sink += found;
        return sample(elapsed, s.metrics());
    }

    /** W3 - Insert and Remove: 1 000 insertions then 1 000 removals at index 0 (head) or n/2 (middle). */
    static Sample w3(IntSequence s, Data d, boolean head) {
        fill(s, d.values);
        int index = head ? 0 : d.n / 2;
        long acc = 0;
        long t0 = System.nanoTime();
        for (int v : d.inserts) {
            s.add(index, v);
        }
        for (int i = 0; i < EDITS; i++) {
            acc += s.remove(index);
        }
        long elapsed = System.nanoTime() - t0;
        if (s.size() != d.n) {
            throw new IllegalStateException("size changed: " + s.size());
        }
        sink += acc;
        return sample(elapsed, s.metrics());
    }

    /** W4 - Priority Processing: insert n values, then n extractMin() calls that must be non-decreasing. */
    static Sample w4(Data d) {
        MinHeap h = new MinHeap();
        int prev = Integer.MIN_VALUE;
        long acc = 0;
        long t0 = System.nanoTime();
        for (int v : d.values) {
            h.insert(v);
        }
        for (int i = 0; i < d.n; i++) {
            int v = h.extractMin();
            if (v < prev) {
                throw new IllegalStateException("extractMin order broken at call " + i);
            }
            prev = v;
            acc += v;
        }
        long elapsed = System.nanoTime() - t0;
        sink += acc;
        return sample(elapsed, h.metrics());
    }
}