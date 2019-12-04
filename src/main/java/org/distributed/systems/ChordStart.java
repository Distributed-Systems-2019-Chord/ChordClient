package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.sun.org.glassfish.external.statistics.Statistic;
import org.apache.commons.cli.*;
import org.distributed.systems.chord.actors.Node;

import org.apache.commons.cli.*;
import scala.concurrent.Future;

public class ChordStart {
    private static CommandLineParser parser = new DefaultParser();
    public static final int STANDARD_TIME_OUT = 1000;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system
        ActorRef node = system.actorOf(Props.create(Node.class)); // Create new actor: node

        // create Options object
        Options options = new Options();

        // add option "-getValue"
        options.addOption(Option.builder()
                .longOpt("get")
                .argName("ip" )
                .hasArg()
                .desc("Get value from node with specified IP")
                .build());

        //parse the options passed as command line arguments
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            //hasOptions checks if option is present or not
            if(cmd.hasOption("get")) {
                System.out.println("Trying to retrieve a value from ip: " +args[1]);
            } else if (cmd.hasOption("kill")) {
                node.tell("kill", ActorRef.noSender());
            }
            else {
                System.out.println("No command specified!");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


//        node.tell("JOIN", ActorRef.noSender()); // Send message

//        system.stop(node); // Quit node
//
//        system.terminate(); // Terminate application
    } 
}
