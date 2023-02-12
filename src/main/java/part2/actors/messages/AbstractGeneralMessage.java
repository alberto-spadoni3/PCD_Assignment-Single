package part2.actors.messages;

import part2.actors.utility.MyLamportClock;

import java.io.Serializable;

public abstract class AbstractGeneralMessage implements Serializable {

    private final MyLamportClock clock;

    public AbstractGeneralMessage(MyLamportClock clock) {
        this.clock = clock;
    }

    public MyLamportClock getClock() {
        return clock;
    }
}
