package org.distributed.systems.chord.messaging;

import akka.actor.ActorRef;

import java.io.Serializable;

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

        public final ActorRef successor;
        public final ActorRef predecessor;

        public Reply(ActorRef successor, ActorRef predecessor) {
            this.successor = successor;
            this.predecessor = predecessor;
        }
    }
}
