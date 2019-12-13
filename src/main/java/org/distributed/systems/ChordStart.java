package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.cli.*;

import org.distributed.systems.chord.actors.Statistics;

public class ChordStart {
    private static CommandLineParser parser = new DefaultParser();
    public static final int STANDARD_TIME_OUT = 1000;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system
        ActorRef node = system.actorOf(Props.create(Statistics.class)); // Create new actor: node

        // create Options object
        Options options = new Options();

        // add option "-getValue"
        options.addOption(Option.builder()
                .longOpt("get")
                .argName("ip" )
                .hasArg()
                .desc("Get value from node with specified IP")
                .build());

        // add option "-killValue"
        options.addOption(Option.builder()
                .longOpt("kill")
                .argName("amount" )
                .hasArg()
                .desc("kill random node")
                .build());

        // add option "-killbtach"
        options.addOption(Option.builder()
                .longOpt("killbatch")
                .argName("amount" )
                .hasArg()
                .desc("kill x random nodes")
                .build());

        // add option "-killbtach"
        options.addOption(Option.builder()
                .longOpt("getaveragehops")
                .argName("amount" )
                .hasArg()
                .desc("getAverageHops for x keys")
                .build());

        //parse the options passed as command line arguments
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            //hasOptions checks if option is present or not
            if(cmd.hasOption("get")) {
                System.out.println("Trying to retrieve a value from ip: " +args[1]);
            } else if (cmd.hasOption("kill")) {
                System.out.println("!!! killing random node!!!");
                node.tell("kill", ActorRef.noSender());
//                TODO handle int argument that will specify the maount of nodes to kill
            }else if (cmd.hasOption("killbatch")) {
                System.out.println("!!! killing random nodes in batch!!!");
                node.tell("killbatch", ActorRef.noSender());
//                TODO handle int argument that will specify the maount of nodes to kill
            }else if (cmd.hasOption("getAverageHops")) {
                System.out.println("!!!getting average hop time for x nodes!!!");
                node.tell("getAverageHops", ActorRef.noSender());
//                TODO handle int argument that will specify the maount of nodes to kill
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
