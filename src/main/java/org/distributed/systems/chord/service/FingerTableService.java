package org.distributed.systems.chord.service;

import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;
import org.distributed.systems.chord.util.IHashUtil;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.util.ArrayList;
import java.util.List;

public class FingerTableService {

    private FingerTable fingerTable;

    private ChordNode predecessor;

    private IHashUtil hashUtil = new HashUtil();

    public FingerTableService() {
        this.fingerTable = new FingerTable(new ArrayList<>(), 0);
    }

    public long startFinger(long nodeId, int fingerTableIndex) {
        return (long) ((nodeId + Math.pow(2, (fingerTableIndex - 1))) % ChordStart.AMOUNT_OF_KEYS);
    }

    public void setFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }

    public ChordNode calcSuccessor() {
        return new ChordNode(hashUtil.hash("SomeRandomValue"));
    }

    public void handleFingerTableMessage(org.distributed.systems.chord.messaging.FingerTable.Reply getFingerTableMessage) {
        //TODO
//        getFingerTableMessage.successors;
//        getFingerTableMessage.predecessor;

    }

    public FingerTable initFingerTable(ChordNode node) {
        FingerTable table = new FingerTable(new ArrayList<>(ChordStart.m), 0);
        for (int i = 1; i <= ChordStart.m; i++) {
            long startFinger = startFinger(node.getId(), i);
            long endFinger = startFinger(node.getId(), i + 1);
            FingerInterval interval = calcInterval(startFinger, endFinger);
            table.addFinger(new Finger(startFinger, interval, calcSuccessor()));
        }
        return table;
    }

    public FingerTable initFingerTableCentral(ChordNode centralNode) {
        FingerTable table = new FingerTable(new ArrayList<>(ChordStart.m), 0);
        for (int i = 1; i <= ChordStart.m; i++) {
            long startFinger = startFinger(centralNode.getId(), i);
            long endFinger = startFinger(centralNode.getId(), i + 1);
            FingerInterval interval = calcInterval(startFinger, endFinger);
            table.addFinger(new Finger(startFinger, interval, centralNode));
        }
        return table;
    }


    public FingerInterval calcInterval(long start1, long start2) {
        long startIndex = start1 % ChordStart.AMOUNT_OF_KEYS;
        long endIndex = start2 % ChordStart.AMOUNT_OF_KEYS;

        return new FingerInterval(startIndex, endIndex);
    }

    public List<Finger> getFingers() {
        return this.fingerTable.getFingerList();
    }

    public ChordNode findPredecessor() {

        return predecessor;
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

        return predecessor; //predecessor.successor
    }

}
