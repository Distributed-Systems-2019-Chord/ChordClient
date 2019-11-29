package org.distributed.systems.chord.model.finger;

public class FingerInterval {

    private final long startKey;
    private final long endKey;

    public FingerInterval(long startKey, long endKey) {
        this.startKey = startKey;
        this.endKey = endKey;
    }

    public long getStartKey() {
        return startKey;
    }

    public long getEndKey() {
        return endKey;
    }
}
