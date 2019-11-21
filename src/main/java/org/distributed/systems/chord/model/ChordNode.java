package org.distributed.systems.chord.model;

public class ChordNode {

    private long id;

    public ChordNode(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
