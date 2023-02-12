package part2.actors.messages;

import part2.actors.utility.MyLamportClock;
import part2.actors.puzzleboard.Tile;

import java.util.List;

public abstract class AbstractPuzzleMessage extends AbstractGeneralMessage {

    private final List<Tile> tiles;

    public AbstractPuzzleMessage(MyLamportClock clock, List<Tile> tiles) {
        super(clock);
        this.tiles = tiles;
    }

    public List<Tile> getTiles() {
        return tiles;
    }
}
