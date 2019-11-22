package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.io.Serializable;
import java.util.List;

public class FingerTable {

    public static class Get implements Command, Serializable {

        private String hash;

        public Get(String hash) {
            this.hash = hash;
        }

        public String getHash() {
            return hash;
        }
    }


    public static class Reply implements Response {

        public final List<ChordNode> successors;

        public Reply(List<ChordNode> successors) {
            this.successors = successors;
        }
    }
}
