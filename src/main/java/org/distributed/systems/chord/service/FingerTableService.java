package org.distributed.systems.chord.service;

import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;

import java.util.ArrayList;

public class FingerTableService {

    private FingerTable fingerTable;

    private ChordNode predecessor;

    public FingerTableService() {
        this.fingerTable = new FingerTable(new ArrayList<>(), 0);
    }

    public long startFinger(long nodeId, int fingerTableIndex) {
        return (long) ((nodeId + Math.pow(2, (fingerTableIndex - 1))) % Math.pow(2, ChordStart.m));
    }

    public ChordNode calcSuccessor() {
        return new ChordNode(21L, "", 1);
    }

    public FingerTable initFingerTable(ChordNode node) {
        FingerTable table = new FingerTable(new ArrayList<>((int) ChordStart.m), 0);
        for (int i = 0; i < ChordStart.m; i++) {
            long startFinger = startFinger(node.getId(), i);
            FingerInterval interval = calcInterval((int) startFinger);
            table.addFinger(new Finger(startFinger, interval, calcSuccessor()));
        }
        return table;
    }


    public FingerInterval calcInterval(int fingerTableIndex) {
        int startIndex = (int) (fingerTableIndex % ChordStart.AMOUNT_OF_KEYS);
        int endIndex = (int) (fingerTableIndex + 1 % ChordStart.AMOUNT_OF_KEYS);

//        long beginInterval = table.getFingerList().get(startIndex).getStart();
//        long endInterval = table.getFingerList().get(endIndex).getStart();
        return new FingerInterval(startIndex, endIndex);
    }

    public ChordNode getSuccessor() {
        return fingerTable.getFingerList().get(0).getSucc();
    }

    public ChordNode getPredecessor() {
        return predecessor;
    }

    public void setSuccessor(ChordNode successor) {
//        this.fingerTable.addFinger(0, new Finger());
    }

    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
    }

    private void findSuccessor(String id) {

    }

    private void findPredecessor(String id) {

    }

    private ChordNode closestPreceedingFinger(String id) {
//        for (int i = amountOfEntries; i > 0; i--) {
//            if ()
//              return
//        }

        return predecessor; // predecessor.successor
    }

}
