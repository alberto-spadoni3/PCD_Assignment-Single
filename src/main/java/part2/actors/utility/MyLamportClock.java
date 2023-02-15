package part2.actors.utility;

import java.io.Serializable;

public class MyLamportClock implements Serializable {

    private final String peerAddress;
    private int value;

    public MyLamportClock(String peerAddress, int value) {
        this.peerAddress = peerAddress;
        this.value = value;
    }

    public MyLamportClock(String peerAddress) {
        this (peerAddress, 0);
    }

    public void increment() {
        value = value + 1;
    }

    public ClockCompareResult compareToClock(final MyLamportClock clock) {
        if (value > clock.getValue()) {
            return ClockCompareResult.GREATER;
        } else if (value < clock.getValue()) {
            return ClockCompareResult.LESS;
        } else {
            return comparePeerIds(clock);
        }
    }

    public int getValue() {
        return value;
    }

    public ClockCompareResult comparePeerIds(final MyLamportClock clock) {
        if(peerAddress.compareTo(clock.getPeerAddress()) > 0) {
            return ClockCompareResult.GREATER;
        } else if (peerAddress.compareTo(clock.getPeerAddress()) < 0) {
            return ClockCompareResult.LESS;
        }
        return ClockCompareResult.EQUAL;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public enum ClockCompareResult {
        GREATER,
        LESS,
        EQUAL
    }

}
