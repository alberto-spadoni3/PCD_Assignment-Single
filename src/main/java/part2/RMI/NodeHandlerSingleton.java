package part2.RMI;

import part2.RMI.listener.PuzzleListener;
import part2.RMI.listener.PuzzleListenerImpl;
import part2.RMI.local.PuzzleLocal;
import part2.RMI.local.PuzzleLocalImpl;
import part2.RMI.puzzleboard.Tile;
import part2.RMI.remote.PuzzleRemote;
import part2.RMI.remote.PuzzleRemoteImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NodeHandlerSingleton {
    private static NodeHandlerSingleton instance = null;

    private static final String remoteModelName = "PuzzleRemoteModel";
    private static final int PORT_REGISTRY = 31000;
    private static final int PORT_OBJECT = 0;

    private Registry registry;
    private PuzzleRemote remoteReference;
    private PuzzleLocal localReference;
    private PuzzleListener listener;
    private Set<PuzzleListener> nodesListeners;
    private boolean needCreation;

    private NodeHandlerSingleton() {
        try {
            registry = LocateRegistry.getRegistry(PORT_REGISTRY);
            listener = new PuzzleListenerImpl();
            nodesListeners = new HashSet<>();
            needCreation = !Arrays.asList(registry.list()).contains(remoteModelName);
        } catch (Exception e) {
            System.out.println("ERRORE: errore durante la creazione del NodeHandlerSingleton");
            e.printStackTrace();
        }
    }

    public static NodeHandlerSingleton getInstance() {
        if(instance == null) {
            throw new IllegalStateException("NodeHandlerSingleton non creato");
        }
        return instance;
    }

    public static void createInstance() {
        if(instance == null) {
            instance = new NodeHandlerSingleton();
        }
    }

    public void initialize() throws RemoteException, NotBoundException {
        if(needCreation) {
            createRemoteState();
        } else {
            downloadRemoteState();
        }
    }

    private void createRemoteState() throws RemoteException {
        System.out.println("Primo nodo: inizializzo il puzzle...");

        localReference = new PuzzleLocalImpl();
        remoteReference = new PuzzleRemoteImpl(localReference.getTiles());
        initListener();
        remoteReference.setMainListener(listener);
        bindRemoteReference();
        localReference.setVisible();
    }

    private void downloadRemoteState() throws RemoteException, NotBoundException {
        System.out.println("Nodi presenti: scarico lo stato del puzzle...");

        remoteReference = (PuzzleRemote) registry.lookup(remoteModelName);
        localReference = new PuzzleLocalImpl(remoteReference.getTiles());
        initListener();
        registry.rebind(remoteModelName, remoteReference);
        localReference.setVisible();
    }

    private void initListener() throws RemoteException {
        UnicastRemoteObject.exportObject(listener, PORT_OBJECT);
        remoteReference.addListener(listener);
    }

    private void bindRemoteReference() throws RemoteException {
        UnicastRemoteObject.exportObject(remoteReference, PORT_OBJECT);
        registry.rebind(remoteModelName, remoteReference);
    }

    public void updateNodesListeners(Set<PuzzleListener> listeners) {
        nodesListeners.clear();
        nodesListeners.addAll(listeners);
    }

    public void updateLocalModel(List<Tile> tiles) {
        localReference.setTiles(tiles);
    }

    public void handleSwap(List<Tile> tiles) {
        try {
            remoteReference.setTiles(tiles);
        } catch (RemoteException e) {
            try {
                System.out.println("Remote puzzle non disponibile, creazione di uno nuovo in corso...");

                remoteReference = new PuzzleRemoteImpl(tiles);
                bindRemoteReference();
                remoteReference.setMainListener(listener);
                remoteReference.addListeners(nodesListeners);
                remoteReference.setTiles(tiles);
            } catch (RemoteException ex) {
                System.out.println("ERRORE: impossibile creare un nuovo remote puzzle");
                ex.printStackTrace();
            }
        }
    }

    public void communicateEndGame() {
        try {
            remoteReference.communicateEndGame();
        } catch (RemoteException e) {
            System.out.println("ERRORE: impossibile disconnetere il main listener...");
            //e.printStackTrace();
        }
    }

    public void closeProgram() {
        System.exit(0);
    }
}
