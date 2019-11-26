package org.distributed.systems.chord.util;

import akka.actor.ActorContext;
import akka.actor.ActorSelection;
import com.typesafe.config.Config;
import org.distributed.systems.chord.model.ChordNode;

public class Util {

    public static String getIp(Config conf) {
        return conf.getString("akka.remote.artery.canonical.hostname");
    }

    public static int getPort(Config conf) {
        return conf.getInt("akka.remote.artery.canonical.port");
    }

    public static ActorSelection getActorRef(ActorContext context, ChordNode node) {
        return context.actorSelection("akka://ChordNetwork@" + node.getIp() + ":" + node.getPort() + "/user/ChordActor");
    }
}
