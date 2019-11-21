package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class GetFingerTableMessage implements Serializable {

    private String hash;

    public GetFingerTableMessage(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
