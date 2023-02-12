package part2.actors.handlers;

import akka.actor.Address;
import part2.actors.Peer;
import part2.actors.messages.BoardStatefulMessage;
import part2.actors.messages.BoardStatelessMessages;
import part2.actors.messages.LockMessages;
import part2.actors.messages.MessageFactory;
import part2.actors.ClusterSingleton;
import part2.actors.utility.MyLamportClock;

public class PuzzleHandler {

    private final Peer myPeer;
    private final LockHandler myLockHandler;


    public PuzzleHandler() {
        myPeer = new Peer();
        myLockHandler = new LockHandler(myPeer);
    }

    public void peerJoiningHandler(Address address) {
        myPeer.log(address.toString() + " si e' collegato al cluster");

        if (myPeer.isMyInitialization(address)) {
            myPeer.initLamportClock();
            myLockHandler.getLocksAndInitialize();
        } else {
            myPeer.addPeer(address);
            myLockHandler.peerJoining(address);
        }
    }

    public void peerUnreachableHandler(Address address) {
        myPeer.log(address.toString() + " non raggiungibile, lo elimino dal cluster");

        ClusterSingleton.getInstance().downActor(address);
    }

    public void peerLeavingHandler(Address address) {
        myPeer.log(address.toString() + " si e' disconnesso");

        myLockHandler.peerLeaving(address);
    }

    public void lockMessageHandler(Address address, LockMessages msg) {
        switch (msg.getType()) {
            case ASK -> myLockHandler.lockRequestReceived(address, msg);
            case GRANT -> myLockHandler.lockAckReceived(address, msg);
        }
    }

    public void boardStatefullMessageHandler(Address address, BoardStatefulMessage message) {
        manageClock(message.getClock());
        switch (message.getType()) {
            case INIT_BOARD_ACK -> {
                if(!myPeer.isInitialized()) {
                    myPeer.initialize();
                    myPeer.updatePuzzle(message.getTiles());
                }
                myLockHandler.operationAckReceived(address);
            }
            case UPDATE_BOARD_REQ -> {
                myPeer.updatePuzzle(message.getTiles());
                ClusterSingleton.getInstance()
                        .getActorFromAddress(address)
                        .tell(MessageFactory.createUpdateBoardAck(myPeer.getClock()), ClusterSingleton.getInstance().getSelf());
            }
        }
    }

    public void boardStatelessMessageHandler(Address address, BoardStatelessMessages message) {
        manageClock(message.getClock());
        switch (message.getType()) {
            case INIT_BOARD_REQ -> {
                myPeer.initializePeer(address);
                ClusterSingleton.getInstance().getActorFromAddress(address)
                        .tell(MessageFactory.createInitBoardAck(myPeer.getClock(), myPeer.getPuzzleBoard().getListOfTiles()),
                                ClusterSingleton.getInstance().getSelf());
            }
            case UPDATE_BOARD_ACK -> myLockHandler.operationAckReceived(address);
        }
    }

    public void updateBoard() {
        myLockHandler.getLocksAndUpdate();
    }

    private void manageClock(MyLamportClock clock) {
        myPeer.updateClock(clock);
        myPeer.incrementClock();
    }
}
