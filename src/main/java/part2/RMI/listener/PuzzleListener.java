package part2.RMI.listener;

import part2.RMI.puzzleboard.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface PuzzleListener extends Remote {

    void updatePuzzle(List<Tile> tiles) throws RemoteException;

    void updateListener(Set<PuzzleListener> listeners) throws RemoteException;

    void endGame() throws RemoteException;

}
