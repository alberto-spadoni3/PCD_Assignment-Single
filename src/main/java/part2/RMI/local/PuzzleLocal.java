package part2.RMI.local;

import part2.RMI.puzzleboard.Tile;

import java.util.List;

public interface PuzzleLocal {

    List<Tile> getTiles();

    void setTiles(List<Tile> tiles);

    void setVisible();

}
