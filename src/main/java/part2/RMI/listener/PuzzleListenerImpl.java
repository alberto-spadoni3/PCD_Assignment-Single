package part2.RMI.listener;

import part2.RMI.NodeHandlerSingleton;
import part2.RMI.puzzleboard.Tile;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public class PuzzleListenerImpl implements PuzzleListener {

    @Override
    public void UpdatePuzzle(List<Tile> tiles) throws RemoteException {
        NodeHandlerSingleton.getInstance().updateLocalModel(tiles);
    }

    @Override
    public void UpdateListener(Set<PuzzleListener> listeners) throws RemoteException {
        NodeHandlerSingleton.getInstance().updateNodesListeners(listeners);
    }

    @Override
    public void endGame() throws RemoteException {
        NodeHandlerSingleton.getInstance().closeProgram();
    }
}
