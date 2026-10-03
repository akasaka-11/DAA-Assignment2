package daa.structures;

import daa.metrics.Metrics;


public final class MyLinkedList implements IntSequence {
    private static final class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int x) {
        Node node = new Node(x);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        metrics.moves += 2;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        if (index == size) {
            add(x);
            return;
        }
        Node node = new Node(x);
        if (index == 0) {
            node.next = head; // link 1
            head = node;      // link 2
        } else {
            Node prev = head;
            long steps = 1;
            for (int k = 0; k < index - 1; k++) {
                prev = prev.next;
                steps++;
            }
            node.next = prev.next; // link 1
            prev.next = node;      // link 2
            metrics.steps += steps;
        }
        size++;
        metrics.moves += 2;
    }

    @Override
    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        int removed;
        long moves = 0;
        if (index == 0) {
            removed = head.value;
            head = head.next;
            moves++;
            if (head == null) {
                tail = null;
                moves++;
            }
            metrics.steps++;
        } else {
            Node prev = head;
            long steps = 1;
            for (int k = 0; k < index - 1; k++) {
                prev = prev.next;
                steps++;
            }
            Node target = prev.next;
            steps++; // visit the node being removed
            removed = target.value;
            prev.next = target.next;
            moves++;
            if (target == tail) {
                tail = prev;
                moves++;
            }
            metrics.steps += steps;
        }
        size--;
        metrics.moves += moves;
        return removed;
    }

    @Override
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        Node cur = head;
        long steps = 1;
        for (int k = 0; k < index; k++) {
            cur = cur.next;
            steps++;
        }
        metrics.steps += steps;
        return cur.value;
    }

    @Override
    public boolean contains(int x) {
        long steps = 0;
        long comparisons = 0;
        boolean found = false;
        for (Node cur = head; cur != null; cur = cur.next) {
            steps++;
            comparisons++;
            if (cur.value == x) {
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
}