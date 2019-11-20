package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class NodeJoinMessage implements Serializable {

    private String nodeId;

    public NodeJoinMessage(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
