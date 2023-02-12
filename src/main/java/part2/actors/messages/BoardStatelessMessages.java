package part2.actors.messages;

import part2.actors.utility.MyLamportClock;

public class BoardStatelessMessages extends AbstractGeneralMessage{

    private final BoardStatelessMessageType type;
    BoardStatelessMessages(MyLamportClock clock, BoardStatelessMessageType type) {
        super(clock);
        this.type = type;
    }

    public BoardStatelessMessageType getType() {
        return type;
    }

    public enum BoardStatelessMessageType {
        INIT_BOARD_REQ,
        UPDATE_BOARD_ACK
    }
}
