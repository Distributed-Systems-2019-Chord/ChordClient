package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;

import java.util.Arrays;

class MemCachedHandler extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Tcp.Received.class,
                        msg -> {
                            final ByteString data = msg.data();
                            String request = data.decodeString("utf-8");

                            // Process each line that is passed:
                            String[] lines = request.split("\r\n");
                            System.out.println(Arrays.toString(lines));

                            String previous = "";
                            // Dummy handler
                            for (String line : lines) {
                                if (line.startsWith("get")) {
                                    ByteString getdataresp = ByteString.fromString("PAYLOAD\r\n");
                                    // 99 is unique id
                                    ByteString getresp = ByteString.fromString("VALUE  key " + (getdataresp.length() - 2) + " 99\r\n");
                                    getSender().tell(TcpMessage.write(getresp), getSelf());
                                    getSender().tell(TcpMessage.write(getdataresp), getSelf());
                                } else if (line.startsWith("set")) {
                                    // TODO: get payload
                                    ByteString resp = ByteString.fromString("\r\n");
                                    getSender().tell(TcpMessage.write(resp), getSelf());
                                } else {
                                    // parse other
                                }
                                previous = line;
                            }

                            ByteString end = ByteString.fromString("END\r\n");
                            getSender().tell(TcpMessage.write(end), getSelf());
                            System.out.println(" Handeled Req");

                        })
                .match(
                        Tcp.ConnectionClosed.class,
                        msg -> {
                            getContext().stop(getSelf());
                        })
                .build();
    }
}
