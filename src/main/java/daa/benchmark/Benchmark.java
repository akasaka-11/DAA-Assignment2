package daa.benchmark;

import daa.benchmark.Workloads.Data;
import daa.benchmark.Workloads.Sample;
import daa.structures.DynamicArray;
import daa.structures.MyLinkedList;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;


public final class Benchmark {
    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int WARMUP = 5;
    static final int RUNS = 5;

    private Benchmark() {}

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);

        System.out.println("JIT warm-up pass...");
        warmUp(1_000, 40);
        warmUp(10_000, 3);

        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(outDir.resolve("results.csv")))) {
            csv.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");
            Supplier<DynamicArray> arr = DynamicArray::new;
            Supplier<MyLinkedList> lst = MyLinkedList::new;
            for (int n : SIZES) {
                Data d = new Data(n);
                System.out.println("n = " + n);
                row(csv, "W1", "-", "DynamicArray", n, measure(() -> Workloads.w1(arr.get(), d)));
                row(csv, "W1", "-", "MyLinkedList", n, measure(() -> Workloads.w1(lst.get(), d)));
                row(csv, "W2", "-", "DynamicArray", n, measure(() -> Workloads.w2(arr.get(), d)));
                row(csv, "W2", "-", "MyLinkedList", n, measure(() -> Workloads.w2(lst.get(), d)));
                row(csv, "W3", "head", "DynamicArray", n, measure(() -> Workloads.w3(arr.get(), d, true)));
                row(csv, "W3", "head", "MyLinkedList", n, measure(() -> Workloads.w3(lst.get(), d, true)));
                row(csv, "W3", "middle", "DynamicArray", n, measure(() -> Workloads.w3(arr.get(), d, false)));
                row(csv, "W3", "middle", "MyLinkedList", n, measure(() -> Workloads.w3(lst.get(), d, false)));
                row(csv, "W4", "-", "MinHeap", n, measure(() -> Workloads.w4(d)));
            }
        }
        System.out.println("wrote " + outDir.resolve("results.csv") + " (checksum " + Workloads.sink + ")");
    }

    /** Runs every workload on both structures so the JVM compiles all hot loops before anything is measured. */
    private static void warmUp(int n, int rounds) {
        Data d = new Data(n);
        for (int round = 0; round < rounds; round++) {
            Workloads.w1(new DynamicArray(), d);
            Workloads.w1(new MyLinkedList(), d);
            Workloads.w2(new DynamicArray(), d);
            Workloads.w2(new MyLinkedList(), d);
            Workloads.w3(new DynamicArray(), d, true);
            Workloads.w3(new MyLinkedList(), d, true);
            Workloads.w3(new DynamicArray(), d, false);
            Workloads.w3(new MyLinkedList(), d, false);
            Workloads.w4(d);
        }
    }

    private interface Case {
        Sample run();
    }

    /** WARMUP discarded runs, then RUNS measured runs; returns the sample holding the median time. */
    private static Sample measure(Case c) {
        for (int i = 0; i < WARMUP; i++) {
            c.run();
        }
        Sample[] samples = new Sample[RUNS];
        for (int i = 0; i < RUNS; i++) {
            samples[i] = c.run();
            if (samples[i].steps() != samples[0].steps() || samples[i].moves() != samples[0].moves()
                    || samples[i].comparisons() != samples[0].comparisons()) {
                throw new IllegalStateException("counters differ between runs");
            }
        }
        long[] times = new long[RUNS];
        for (int i = 0; i < RUNS; i++) {
            times[i] = samples[i].nanos();
        }
        Arrays.sort(times);
        long median = times[RUNS / 2];
        Sample last = samples[RUNS - 1];
        return new Sample(median, last.steps(), last.moves(), last.comparisons());
    }

    private static void row(PrintWriter csv, String workload, String variant, String structure, int n, Sample s) {
        csv.println(String.format(Locale.ROOT, "%s,%s,%s,%d,%.6f,%d,%d,%d",
                workload, variant, structure, n, s.nanos() / 1e6, s.steps(), s.moves(), s.comparisons()));
        csv.flush();
    }
}