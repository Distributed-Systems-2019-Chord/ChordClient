package org.distributed.systems;

import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.cli.*;
import org.distributed.systems.chord.actors.Node;

import org.apache.commons.cli.*;
public class ChordStart {
    private static CommandLineParser parser = new DefaultParser();

    public static final int STANDARD_TIME_OUT = 1000;
    // FIXME 160 according to sha-1 but this is the max_length of a java long..
    public static final int M = 64;

    public static void main(String[] args) {
        // Create actor system
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
            } else {
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
