package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class GetValueMessage implements Serializable {

    private String key;

    public GetValueMessage(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
