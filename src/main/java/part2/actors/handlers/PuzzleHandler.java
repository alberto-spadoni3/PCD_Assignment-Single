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

    public void peerJoiningHandler(Address actorAddress) {
        myPeer.log(actorAddress.toString() + " si e' collegato al cluster");

        if (myPeer.isMyInitialization(actorAddress)) {
            myPeer.initLamportClock();
            myLockHandler.getLocksAndInitialize();
        } else {
            myPeer.addPeer(actorAddress);
            myLockHandler.peerJoining(actorAddress);
        }
    }

    public void peerUnreachableHandler(Address actorAddress) {
        myPeer.log(actorAddress.toString() + " non raggiungibile, Lo elimino dal cluster");

        ClusterSingleton.getInstance().downActor(actorAddress);
    }

    public void peerLeavingHandler(Address actorAddress) {
        myPeer.log(actorAddress.toString() + " si e' disconnesso");

        myLockHandler.peerLeaving(actorAddress);
    }

    public void lockMessageHandler(Address actorAddress, LockMessages msg) {
        switch (msg.getType()) {
            case ASK -> myLockHandler.lockRequestReceived(actorAddress, msg.getClock());
            case GRANT -> myLockHandler.lockGrantReceived(actorAddress, msg.getClock());
        }
    }

    public void boardStatefulMessageHandler(Address senderAddress, BoardStatefulMessage message) {
        manageClock(message.getClock());
        switch (message.getType()) {
            case INIT_BOARD_ACK -> {
                if (!myPeer.isInitialized()) {
                    myPeer.initialize();
                    myPeer.updatePuzzle(message.getTiles());
                }
                myLockHandler.ackReceived(senderAddress);
            }
            case UPDATE_BOARD -> {
                myPeer.updatePuzzle(message.getTiles());
                ClusterSingleton.getInstance()
                        .getActorFromAddress(senderAddress)
                        .tell(MessageFactory.createBoardUpdated(myPeer.getClock()), ClusterSingleton.getInstance().getSelf());
            }
        }
    }

    public void boardStatelessMessageHandler(Address senderAddress, BoardStatelessMessages message) {
        manageClock(message.getClock());
        switch (message.getType()) {
            case INIT_BOARD_REQ -> {
                myPeer.initializePeer(senderAddress);
                ClusterSingleton.getInstance().getActorFromAddress(senderAddress)
                        .tell(MessageFactory.createInitBoardAck(myPeer.getClock(), myPeer.getPuzzleTiles()),
                                ClusterSingleton.getInstance().getSelf());
            }
            case BOARD_UPDATED -> myLockHandler.ackReceived(senderAddress);
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
