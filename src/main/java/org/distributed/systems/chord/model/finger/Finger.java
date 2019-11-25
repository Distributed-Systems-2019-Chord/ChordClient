package org.distributed.systems.chord.model.finger;

import org.distributed.systems.chord.model.ChordNode;

public class Finger {

    private long start; // key

    private FingerInterval interval;

    private ChordNode succ; // node ref

    public Finger(long start, FingerInterval interval, ChordNode succ) {
        this.start = start;
        this.interval = interval;
        this.succ = succ;
    }

    public long getStart() {
        return start;
    }

    public FingerInterval getInterval() {
        return interval;
    }

    public ChordNode getSucc() {
        return succ;
    }
}
