package org.distributed.systems.chord.model;

import java.io.Serializable;

public class ChordNode implements Serializable {

    private long id;

    private String ip;

    private int port;

    public ChordNode(long id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public ChordNode(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
