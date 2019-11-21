package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class NodeLeaveMessage implements Serializable {

    private long nodeId;

    public NodeLeaveMessage(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return nodeId;
    }

}
