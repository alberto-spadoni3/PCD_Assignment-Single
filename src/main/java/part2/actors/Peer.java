package part2.actors;

import akka.actor.Address;
import part2.actors.puzzleboard.Puzzle;
import part2.actors.puzzleboard.Tile;
import part2.actors.utility.MyLamportClock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static part2.actors.utility.MyLamportClock.ClockCompareResult.LESS;

public class Peer {
    private final Set<Address> initializedPeer;
    private final Set<Address> uninitializedPeer;
    private final Address myAddress;
    private MyLamportClock myLamportClock;
    private final Puzzle myPuzzle;
    private boolean initialized;

    public Peer() {
        initializedPeer = new HashSet<>();
        uninitializedPeer = new HashSet<>();
        myAddress = ClusterSingleton.getInstance()
                .getCluster()
                .remotePathOf(ClusterSingleton.getInstance().getContext().sender())
                .address();
        initialized = false;
        myPuzzle = new Puzzle();
    }

    public void log(String msg) {
        System.out.println(myAddress.toString() + " :: " + msg);
    }

    public boolean isMyInitialization(Address actorAddress) {
        return myAddress.equals(actorAddress);
    }

    public void addPeer(Address peer) {
        if (initialized) {
            uninitializedPeer.add(peer);
        } else {
            initializedPeer.add(peer);
        }
    }

    public void removePeer(Address peer) {
        uninitializedPeer.remove(peer);
        initializedPeer.remove(peer);
    }

    public Set<Address> getAllPeers() {
        Set<Address> temp = new HashSet<>(initializedPeer);
        temp.addAll(uninitializedPeer);
        return temp;
    }

    public Set<Address> getInitializedPeers() {
        return initializedPeer;
    }

    public boolean isAlone() {
        return initializedPeer.isEmpty() && uninitializedPeer.isEmpty();
    }

    public void initialize() {
        initialized = true;
        myPuzzle.initPuzzle(ClusterSingleton.getInstance().getSelf());
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initializePeer(Address actorAddress) {
        uninitializedPeer.remove(actorAddress);
        initializedPeer.add(actorAddress);
    }

    public List<Tile> getPuzzleTiles() {
        return myPuzzle.getBoard().getTiles();
    }

    public void updatePuzzle(List<Tile> tiles) {
        myPuzzle.updatePuzzle(tiles);
    }

    public void initLamportClock() {
        myLamportClock = new MyLamportClock(myAddress.toString());
    }

    public void incrementClock() {
        myLamportClock.increment();
    }

    public MyLamportClock getClock() {
        return myLamportClock;
    }

    public void updateClock(MyLamportClock newClock) {
        if (myLamportClock.compareToClock(newClock).equals(LESS)) {
            myLamportClock = newClock;
        }
    }
}
