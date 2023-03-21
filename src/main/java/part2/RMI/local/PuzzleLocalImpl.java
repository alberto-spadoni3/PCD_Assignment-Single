package part2.RMI.local;

import part2.RMI.puzzleboard.PuzzleBoard;
import part2.RMI.puzzleboard.Tile;
import part2.actors.utility.StringEnum;

import java.util.List;

public class PuzzleLocalImpl implements PuzzleLocal {
    private final PuzzleBoard board;
    private final StringEnum imagePuzzle = StringEnum.IMAGE1;

    public PuzzleLocalImpl() {
        board = new PuzzleBoard("My Puzzle", imagePuzzle);
        board.createTiles();
        board.paintPuzzle(board.getPanel());
    }

    public PuzzleLocalImpl(List<Tile> tiles) {
        board = new PuzzleBoard("My Puzzle", imagePuzzle);
        board.setTiles(tiles);
        board.paintPuzzle(board.getPanel());
    }

    @Override
    public List<Tile> getTiles() {
        return board.getTiles();
    }

    @Override
    public void setTiles(List<Tile> tiles) {
        board.setTiles(tiles);
        board.paintPuzzle(board.getPanel());
    }

    @Override
    public void setVisible() {
        board.setVisible(true);
    }
}
