package part2.actors.messages;

import part2.actors.utility.MyLamportClock;
import part2.actors.puzzleboard.Tile;

import java.util.List;

public class BoardStatefulMessage extends AbstractPuzzleMessage{

    private final BoardStatefulMessageType type;
    BoardStatefulMessage(MyLamportClock clock, List<Tile> tiles, BoardStatefulMessageType type) {
        super(clock, tiles);
        this.type = type;
    }

    public BoardStatefulMessageType getType() {
        return type;
    }

    public enum BoardStatefulMessageType {
        INIT_BOARD_ACK,
        UPDATE_BOARD
    }
}
