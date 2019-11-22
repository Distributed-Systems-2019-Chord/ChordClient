package org.distributed.systems.chord.service;

import akka.actor.ActorRef;

public class FingerTableService {

    private ActorRef successor;
    private ActorRef last;

    public ActorRef getSuccessor() {
        return successor;
    }

    public void setSuccessor(ActorRef successor) {
        if(this.last == null){
            this.last = successor;
            this.successor = successor;
        } else{

        }

    }

}
