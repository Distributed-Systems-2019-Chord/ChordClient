package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class NodeJoinMessage implements Serializable {

    private long nodeId;

    private String remoteAddress;

    public NodeJoinMessage(long nodeId, String remoteAddress) {
        this.nodeId = nodeId;
        this.remoteAddress = remoteAddress;
    }

    public long getNodeId() {
        return nodeId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
