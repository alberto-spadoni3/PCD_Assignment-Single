package part2.RMI.remote;

import part2.RMI.listener.PuzzleListener;
import part2.RMI.puzzleboard.Tile;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PuzzleRemoteImpl implements PuzzleRemote, Serializable {
    private final Set<PuzzleListener> listeners;
    private PuzzleListener mainListener;
    private List<Tile> tiles;

    public PuzzleRemoteImpl(List<Tile> tiles) {
        this.tiles = tiles;
        listeners = new HashSet<>();
    }

    @Override
    public synchronized List<Tile> getTiles() {
        return tiles;
    }

    @Override
    public synchronized void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
        runListeners(ListenerAction.UPDATE_PUZZLE);
    }

    @Override
    public synchronized void setMainListener(PuzzleListener listener) throws RemoteException {
        mainListener = listener;
    }

    @Override
    public synchronized void addListener(PuzzleListener listener) throws RemoteException {
        listeners.add(listener);
        runListeners(ListenerAction.UPDATE_LISTENERS);
    }

    @Override
    public synchronized void addListeners(Set<PuzzleListener> listeners) throws RemoteException {
        this.listeners.clear();
        this.listeners.addAll(listeners);
        runListeners(ListenerAction.UPDATE_LISTENERS);
    }

    @Override
    public void communicateEndGame() throws RemoteException {
        runListeners(ListenerAction.ENDGAME);
        mainListener.endGame();
    }

    private void runListeners(ListenerAction action) {
        Set<PuzzleListener> disconnectedListeners = new HashSet<>();
        for (PuzzleListener listener : listeners) {
            try {
                switch (action) {
                    case UPDATE_PUZZLE -> listener.updatePuzzle(tiles);
                    case UPDATE_LISTENERS -> listener.updateListener(listeners);
                    case ENDGAME -> {
                        if(!listener.equals(mainListener))
                            listener.endGame();
                    }
                }
            } catch (RemoteException e) {
                disconnectedListeners.add(listener);
            }
        }
        listeners.removeAll(disconnectedListeners);
    }

    private enum ListenerAction {
        UPDATE_PUZZLE,
        UPDATE_LISTENERS,
        ENDGAME
    }
}
