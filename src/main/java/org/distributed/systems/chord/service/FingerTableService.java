package org.distributed.systems.chord.service;

import org.distributed.systems.chord.model.ChordNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FingerTableService {

    private final static List<ChordNode> successors = new ArrayList<>();

    static {
//        successors.add(new ChordNode(0L));
    }

    public Optional<ChordNode> getChordNode(long id) {
        return successors.stream()
                .filter(node -> node.getId() == id)
                .findFirst();
    }

    public void addSuccessor(ChordNode successor) {
        successors.add(successor);
    }


    public void removeSuccessor(ChordNode successor) {
        successors.remove(successor);
    }

    public List<ChordNode> chordNodes() {
        return successors;
    }


}
