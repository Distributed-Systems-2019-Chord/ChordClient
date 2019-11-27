package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.util.List;

public class FingerTable {

    public static class Get implements Command {

        private long hash;

        public Get(long hash) {
            this.hash = hash;
        }

        public long getHash() {
            return hash;
        }
    }


    public static class Reply implements Response {

        public final List<ChordNode> successors;
        public final ChordNode predecessor;

        public Reply(List<ChordNode> successors, ChordNode predecessor) {
            this.successors = successors;
            this.predecessor = predecessor;
        }
    }

    public static class GetPredecessor implements Command {

        private final long nodeId;

        public GetPredecessor(long nodeId) {
            this.nodeId = nodeId;
        }

        public long getId() {
            return nodeId;
        }
    }

    public static class GetPredecessorReply implements Response {

        private final ChordNode predecessor;

        public GetPredecessorReply(ChordNode predecessor) {
            this.predecessor = predecessor;
        }

        public ChordNode getChordNode() {
            return this.predecessor;
        }
    }

    public static class GetSuccessor implements Command {

        private Long id;

        public GetSuccessor(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    public static class GetSuccessorReply implements Response {

        private final ChordNode successor;

        public GetSuccessorReply(ChordNode successor) {
            this.successor = successor;
        }

        public ChordNode getChordNode() {
            return this.successor;
        }
    }
}
