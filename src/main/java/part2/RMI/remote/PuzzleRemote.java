package part2.RMI.remote;

import part2.RMI.listener.PuzzleListener;
import part2.RMI.puzzleboard.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface PuzzleRemote extends Remote {

    List<Tile> getTiles() throws RemoteException;

    void  setTiles(List<Tile> tiles) throws RemoteException;

    void addListener(PuzzleListener listener) throws RemoteException;

    void addListeners(Set<PuzzleListener> listeners) throws RemoteException;

    void setMainListener(PuzzleListener listener) throws RemoteException;

    void communicateEndGame() throws RemoteException;
}
