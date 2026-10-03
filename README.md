# DAA Assignment 2 - In-Memory Workload Engine

`DynamicArray`, `MyLinkedList` and `MinHeap` implemented from scratch (primitive `int`, no `java.util` collections in `src/main`), with operation counters, JUnit 5 tests, a benchmark of four workloads and a report.

## Requirements

- JDK 17 or newer
- Maven 3.8+ (or IntelliJ IDEA, which includes Maven)
- Python 3 with `matplotlib` (only for drawing the plots)

## Build and test

```bash
mvn clean package      # compiles, runs all JUnit 5 tests, builds the jar
mvn test               # tests only
```

In IntelliJ: Maven panel -> Lifecycle -> clean, package; or right-click `src/test/java` -> Run 'All Tests'.

## Run the benchmark (creates results/results.csv)

```bash
mvn -q compile exec:exec
```

In IntelliJ: run `daa.benchmark.Benchmark` (`main`) with VM options `-Xms512m -Xmx512m`.

It uses `new Random(42)`, warms up the JIT, runs every case 5 times after 5 discarded warm-up runs and saves the median time. It writes `results/results.csv` with columns `workload,variant,structure,n,time_ms,steps,moves,comparisons` (variant is `head`/`middle` for W3 and `-` otherwise). The run takes about 20 seconds. Counters are identical on every machine; times depend on the machine.

## Draw the plots (creates results/plots/*.png)

```bash
pip install matplotlib
python scripts/plot.py
```

## Counting rules

| Counter | Meaning |
|---|---|
| steps | one array cell read, or one list node visited (starting at the head) |
| moves | one element shifted or copied in an array (shifts and resize copies), or one link update in a list (head, tail, `next`) |
| comparisons | one comparison of two stored elements |

The counters are incremented inside the methods. In W1-W3 only the operation phase is timed and counted; in W4 the inserts and the extractMin calls are counted together.

## Project layout

```
src/main/java/daa/structures   DynamicArray, MyLinkedList, MinHeap, IntSequence
src/main/java/daa/metrics      Metrics (steps / moves / comparisons)
src/main/java/daa/benchmark    Workloads (W1-W4), Benchmark (runner, median, CSV)
src/test/java/daa/structures   JUnit 5 tests
results/results.csv            benchmark output
results/plots/                 PNG charts
scripts/plot.py                chart generator
REPORT.md                      complexity table, loop invariant proofs, plots, discussion
```

## Git workflow

Branches `feature/metrics`, `feature/array`, `feature/list`, `feature/heap` are merged into `main`; `main` holds only working code and is tagged `v1.0`.