package part2.actors.messages;

import part2.actors.utility.MyLamportClock;

public class LockMessages extends AbstractGeneralMessage {

    private final LockMessageType type;

    LockMessages(MyLamportClock clock, LockMessageType type) {
        super(clock);
        this.type = type;
    }

    public LockMessageType getType() {
        return type;
    }

    public enum LockMessageType {
        ASK,
        GRANT
    }
}
