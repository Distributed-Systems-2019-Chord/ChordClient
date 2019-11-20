package org.distributed.systems.chord.model;

public class Successor {

    private final String id;

    public Successor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
