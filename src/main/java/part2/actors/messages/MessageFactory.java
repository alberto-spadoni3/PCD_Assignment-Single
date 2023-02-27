package part2.actors.messages;

import part2.actors.puzzleboard.Tile;
import part2.actors.utility.MyLamportClock;

import java.util.List;

public class MessageFactory {
    private MessageFactory() {}

    public static LockMessages createAskMessage(MyLamportClock clock) {
        return new LockMessages(clock, LockMessages.LockMessageType.ASK);
    }

    public static LockMessages createGrantMessage(MyLamportClock clock) {
        return new LockMessages(clock, LockMessages.LockMessageType.GRANT);
    }

    public static BoardStatelessMessages createInitBoardReq(MyLamportClock clock) {
        return new BoardStatelessMessages(clock, BoardStatelessMessages.BoardStatelessMessageType.INIT_BOARD_REQ);
    }

    public static BoardStatefulMessage createInitBoardAck(MyLamportClock clock, List<Tile> tiles) {
        return new BoardStatefulMessage(clock, tiles,
                BoardStatefulMessage.BoardStatefulMessageType.INIT_BOARD_ACK);
    }

    public static BoardStatefulMessage createUpdateBoard(MyLamportClock clock, List<Tile> tiles) {
        return new BoardStatefulMessage(clock, tiles,
                BoardStatefulMessage.BoardStatefulMessageType.UPDATE_BOARD);
    }

    public static BoardStatelessMessages createBoardUpdated(MyLamportClock clock) {
        return new BoardStatelessMessages(clock, BoardStatelessMessages.BoardStatelessMessageType.BOARD_UPDATED);
    }

    public static SwapTilesMessage createSwapTilesMessage() {
        return new SwapTilesMessage();
    }
}
