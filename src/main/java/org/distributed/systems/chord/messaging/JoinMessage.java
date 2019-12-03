package org.distributed.systems.chord.messaging;

import akka.actor.ActorRef;

public class JoinMessage {

        public static class JoinRequest implements Command {

            public long requestorKey;
            public ActorRef requestor;

            public JoinRequest(ActorRef requestor, long requestorKey) {
                this.requestor = requestor;
                this.requestorKey = requestorKey;
            }
        }

        public static  class JoinConfirmationRequest implements Command {

            public long newPredecessorKey;
            public ActorRef newPredecessor;
            public long newSucessorKey;
            public ActorRef newSucessor;

            public JoinConfirmationRequest(ActorRef newPredecessor, long newPredecessorKey, ActorRef newSucessor, long newSucessorKey) {
                this.newPredecessorKey = newPredecessorKey;
                this.newPredecessor = newPredecessor;
                this.newSucessorKey = newSucessorKey;
                this.newSucessor = newSucessor;
            }

        }

        public static  class JoinConfirmationReply implements Command {
            public boolean accepted;

            public JoinConfirmationReply(boolean accepted) {
                this.accepted = accepted;
            }
        }

        public static class JoinReply implements Response {

            public boolean accepted;
            public ActorRef predecessor;
            public long predecessorId;
            public ActorRef sucessor;
            public long sucessorId;

            public JoinReply(ActorRef predecessor, ActorRef sucessor, boolean accepted) {
                this(predecessor, sucessor, accepted, 0,0);
            }

            public JoinReply(ActorRef predecessor, ActorRef sucessor, boolean accepted, long predecessorId, long sucessorId) {
                this.accepted = accepted;
                this.predecessor = predecessor;
                this.predecessorId = predecessorId;
                this.sucessor = sucessor;
                this.sucessorId = sucessorId;
            }
        }
}
