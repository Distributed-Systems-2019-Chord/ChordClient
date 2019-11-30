package org.distributed.systems.chord.messaging;

public class FindPredecessor implements Command {
    private final long id;

    private final int index;

    public FindPredecessor(long id, int index) {
        this.id = id;
        this.index = index;
    }

    public long getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }
}
