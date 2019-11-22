package org.distributed.systems.chord.messaging;

import java.io.Serializable;

public class KeyValue {

    public static class Put implements Command, Serializable {
        public final String key;
        public final Serializable value;


        public Put(String key, Serializable value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Get implements Command, Serializable {
        public final String key;

        public Get(String key) {
            this.key = key;
        }
    }

    public static class Reply implements Response {

        public final Serializable value;

        public Reply(Serializable value) {
            this.value = value;
        }
    }
}
