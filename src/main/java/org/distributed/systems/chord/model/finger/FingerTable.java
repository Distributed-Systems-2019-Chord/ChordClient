package org.distributed.systems.chord.model.finger;

import org.distributed.systems.ChordStart;

import java.util.List;

public class FingerTable {

    private List<Finger> fingerList;

    private int keySize;

    public FingerTable(List<Finger> fingerList, int keySize) {
        this.fingerList = fingerList;
        this.keySize = keySize;
    }

    public void addFinger(Finger finger) {
        fingerList.add(finger);
    }

    public List<Finger> getFingerList() {
        return fingerList;
    }

    public void setFingerList(List<Finger> fingerList) {
        this.fingerList = fingerList;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public FingerInterval calcInterval(int fingerTableIndex) {
        int startIndex = (int) (fingerTableIndex % ChordStart.AMOUNT_OF_KEYS);
        int endIndex = (int) (fingerTableIndex + 1 % ChordStart.AMOUNT_OF_KEYS);

        long beginInterval = getFingerList().get(startIndex).getStart();
        long endInterval = getFingerList().get(endIndex).getStart();
        return fingerList.get(startIndex).getInterval();
    }
}
