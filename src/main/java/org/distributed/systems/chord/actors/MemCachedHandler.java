package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import org.distributed.systems.chord.service.StorageService;

import java.io.Serializable;
import java.util.Arrays;

class MemCachedHandler extends AbstractActor {

    private StorageService storageService;
    private String previous = "";

    public MemCachedHandler() {
        this.storageService = StorageService.getInstance();
    }

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
                            
                            // Dummy handler
                            for (String line : lines) {
                                if (line.startsWith("get")) {
                                    String[] get_options = line.split(" ");
                                    String key = get_options[1];
                                    Serializable payload = this.storageService.get(key);
                                    Integer payload_length = payload.toString().length();
                                    ByteString getdataresp = ByteString.fromString(payload.toString() + "\r\n");
                                    // 99 is unique id
                                    ByteString getresp = ByteString.fromString("VALUE " + key + "  " + (payload_length) + " 99\r\n");

                                    getSender().tell(TcpMessage.write(getresp), getSelf());
                                    getSender().tell(TcpMessage.write(getdataresp), getSelf());
                                } else if (line.startsWith("set")) {
                                    // TODO: get payload
                                    ByteString resp = ByteString.fromString("\r\n");
                                    getSender().tell(TcpMessage.write(resp), getSelf());
                                } else {
                                    if (previous.startsWith("set")) {
                                        String[] set_options = previous.split(" ");
                                        this.storageService.put(set_options[1], line);
                                    }
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
