#!/usr/bin/env python3
"""Builds the PNG charts in results/plots/ from results/results.csv."""
import csv
import math
import os
from collections import defaultdict

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

RESULTS = "results/results.csv"
OUT = "results/plots"

STRUCT_STYLE = {
    "DynamicArray": dict(linestyle="-", marker="o"),
    "MyLinkedList": dict(linestyle="--", marker="s"),
    "MinHeap": dict(linestyle="-", marker="o"),
}
STRUCT_COLOR = {"DynamicArray": "tab:blue", "MyLinkedList": "tab:red", "MinHeap": "tab:green"}
METRIC_COLOR = {"steps": "tab:blue", "moves": "tab:orange", "comparisons": "tab:green"}
TITLES = {
    "W1": "W1 Random access (10 000 get calls)",
    "W2": "W2 Search (1 000 contains, half present)",
    "W3": "W3 Insert & remove (1 000 + 1 000 operations)",
    "W4": "W4 Priority processing (n inserts + n extractMin)",
}


def load():
    rows = defaultdict(lambda: defaultdict(list))  # (workload, variant) -> structure -> [(n, ...)]
    with open(RESULTS) as f:
        for r in csv.DictReader(f):
            rows[(r["workload"], r["variant"])][r["structure"]].append(
                (int(r["n"]), float(r["time_ms"]), int(r["steps"]), int(r["moves"]), int(r["comparisons"])))
    return rows


def style_axes(ax, ylabel, title):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n (number of elements)")
    ax.set_ylabel(ylabel)
    ax.set_title(title, fontsize=10)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=8)


def time_panel(ax, series, title):
    for struct, pts in series.items():
        pts = sorted(pts)
        ax.plot([p[0] for p in pts], [p[1] for p in pts], color=STRUCT_COLOR[struct],
                label=struct, **STRUCT_STYLE[struct])
    style_axes(ax, "median time (ms)", title)


def ops_panel(ax, series, title):
    for struct, pts in series.items():
        pts = sorted(pts)
        for idx, metric in ((2, "steps"), (3, "moves"), (4, "comparisons")):
            xs = [p[0] for p in pts if p[idx] > 0]
            ys = [p[idx] for p in pts if p[idx] > 0]
            if not xs:
                continue  # a counter that is always 0 cannot be drawn on a log axis
            ax.plot(xs, ys, color=METRIC_COLOR[metric], label=f"{struct}: {metric}",
                    linestyle=STRUCT_STYLE[struct]["linestyle"], marker=STRUCT_STYLE[struct]["marker"],
                    markersize=4)
    style_axes(ax, "operations (count, whole workload)", title)


def save(fig, name):
    os.makedirs(OUT, exist_ok=True)
    fig.tight_layout()
    fig.savefig(os.path.join(OUT, name), dpi=150)
    plt.close(fig)
    print("saved", os.path.join(OUT, name))


def main():
    rows = load()
    for w in ("W1", "W2", "W4"):
        series = rows[(w, "-")]
        fig, ax = plt.subplots(figsize=(6.2, 4.2))
        time_panel(ax, series, TITLES[w] + " - time")
        if w == "W4":  # reference curve n*log2(n), scaled to the last measurement
            pts = sorted(series["MinHeap"])
            n0, t0 = pts[-1][0], pts[-1][1]
            ns = [p[0] for p in pts]
            ax.plot(ns, [t0 * (n * math.log2(n)) / (n0 * math.log2(n0)) for n in ns], color="gray",
                    linestyle=":", label="reference n log n")
            ax.legend(fontsize=8)
        save(fig, f"{w.lower()}_time.png")
        fig, ax = plt.subplots(figsize=(6.8, 4.4))
        ops_panel(ax, series, TITLES[w] + " - counters\n(counters that are always 0 are not drawn)")
        ax.legend(fontsize=7)
        save(fig, f"{w.lower()}_ops.png")

    fig, axes = plt.subplots(1, 2, figsize=(11, 4.2), sharey=True)
    for ax, variant in zip(axes, ("head", "middle")):
        time_panel(ax, rows[("W3", variant)], f"W3 {variant} (index {'0' if variant == 'head' else 'n/2'}) - time")
    save(fig, "w3_time.png")
    fig, axes = plt.subplots(1, 2, figsize=(12, 4.4), sharey=True)
    for ax, variant in zip(axes, ("head", "middle")):
        ops_panel(ax, rows[("W3", variant)], f"W3 {variant} (index {'0' if variant == 'head' else 'n/2'}) - counters")
        ax.legend(fontsize=7)
    save(fig, "w3_ops.png")


if __name__ == "__main__":
    main()