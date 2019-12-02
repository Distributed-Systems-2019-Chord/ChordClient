package org.distributed.systems.chord.messaging;

public class FixFingers implements Command{

    private long start;
    private int index;
    public FixFingers(int index, long start){
        this.start = start;
        this.index = index;
    }

    public long getStart() {
        return start;
    }
    public int getIndex(){
        return this.index;
    }
}
