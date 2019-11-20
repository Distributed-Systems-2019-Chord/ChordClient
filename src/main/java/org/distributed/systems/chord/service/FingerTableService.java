package org.distributed.systems.chord.service;

import org.distributed.systems.chord.model.Successor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FingerTableService {

    private final static List<Successor> successors = new ArrayList<>();

    static {
        successors.add(new Successor("0"));
    }

    public Optional<Successor> getSuccessor(String id) {
        return successors.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public void createUser(Successor successor) {
        successors.add(successor);
    }

    public List<Successor> getSuccessors() {
        return successors;
    }


}
