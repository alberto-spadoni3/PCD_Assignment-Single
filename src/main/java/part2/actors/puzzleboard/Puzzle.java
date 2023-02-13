package part2.actors.puzzleboard;

import akka.actor.ActorRef;
import part2.actors.utility.StringEnum;

import java.util.List;

public class Puzzle {

    private PuzzleBoard board;

    public Puzzle() { }

    public void initPuzzle(ActorRef actorRef) {
        //CHANGE ENUM VALUE TO CHANGE IMAGE AND PUZZLE'S DIMENSION
        board = new PuzzleBoard(actorRef, "Puzzle of " + actorRef.toString(), StringEnum.IMAGE1);
        board.setVisible(true);
        board.loadImage();
        board.createTiles();
        board.paintPuzzle(board.getPanel());
    }

    public void updatePuzzle(List<Tile> tiles) {
        board.setTiles(tiles);
        board.paintPuzzle(board.getPanel());
    }

    public PuzzleBoard getBoard() {
        return board;
    }
}
