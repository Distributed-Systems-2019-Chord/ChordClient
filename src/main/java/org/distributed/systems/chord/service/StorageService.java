package org.distributed.systems.chord.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StorageService {

    private static final StorageService instance = new StorageService();
    private Map<String, Serializable> valueStore;

    public static StorageService getInstance() {
        return instance;
    }

    private StorageService() {
        this.valueStore = new HashMap<>();
    }

    public void put(String key, Serializable value) {
        this.valueStore.put(key, value);
    }

    public Serializable get(String key) {
        return this.valueStore.get(key);
    }

}
