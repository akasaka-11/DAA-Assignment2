package daa.structures;

class MyLinkedListTest extends IntSequenceContract {
    @Override
    IntSequence create() {
        return new MyLinkedList();
    }
}