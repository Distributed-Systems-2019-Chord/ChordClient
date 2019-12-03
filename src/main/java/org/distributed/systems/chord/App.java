package org.distributed.systems.chord;


import org.apache.commons.cli.*;

public class App
{
    private static CommandLineParser parser = new DefaultParser();

    // Example call java App.java -getValue 127.0.0.1
    public static void main( String[] args ) throws ParseException {
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
        CommandLine cmd = parser.parse(options, args);

        //hasOptions checks if option is present or not
        if(cmd.hasOption("get")) {
            System.out.println("Trying to retrieve a value from ip: " +args[1]);
        } else {
            System.out.println("No command specified!");
        }
    }
}
