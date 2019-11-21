package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class PutValueMessage implements Serializable {

    private String key;
    private Serializable value;

    public PutValueMessage(String key, Serializable value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Serializable getValue() {
        return value;
    }
}
