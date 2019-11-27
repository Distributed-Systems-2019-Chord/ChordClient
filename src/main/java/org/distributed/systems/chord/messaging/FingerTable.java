package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.io.Serializable;
import java.util.List;

public class FingerTable {

    public static class Get implements Command, Serializable {

        private long hash;

        public Get(long hash) {
            this.hash = hash;
        }

        public long getHash() {
            return hash;
        }
    }


    public static class Reply implements Response, Serializable {

        public final List<ChordNode> successors;
        public final ChordNode predecessor;

        public Reply(List<ChordNode> successors, ChordNode predecessor) {
            this.successors = successors;
            this.predecessor = predecessor;
        }
    }
}
