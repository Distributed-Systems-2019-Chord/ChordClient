package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class GetValueResponseMessage implements Serializable {

    private Serializable val;

    public GetValueResponseMessage(Serializable val) {
        this.val = val;
    }

    public Serializable getVal() {
        return val;
    }
}
