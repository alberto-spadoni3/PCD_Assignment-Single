package part2.actors.handlers;

import akka.actor.Address;
import part2.actors.messages.*;
import part2.actors.utility.MyLamportClock;
import part2.actors.Peer;
import part2.actors.ClusterSingleton;
import part2.actors.utility.OperationEnum;

import java.util.HashSet;
import java.util.Set;

import static part2.actors.utility.MyLamportClock.ClockCompareResult.LESS;

public class LockHandler {
    private final Peer myPeer;
    private final Set<Address> requestQueue;
    private final Set<Address> grantQueue;
    private final Set<Address> ackQueue;
    private boolean wantLock;
    private boolean hasLock;
    private MyLamportClock myRequestClock;
    private OperationEnum operationToExecute;

    public LockHandler(Peer peer) {
        myPeer = peer;
        requestQueue = new HashSet<>();
        grantQueue = new HashSet<>();
        ackQueue = new HashSet<>();
        wantLock = false;
        hasLock = false;
        operationToExecute = OperationEnum.IDLE;
    }

    public void getLocksAndInitialize() {
        if (myPeer.isAlone()) {
            myPeer.log("inizializzo il puzzle");

            myPeer.initialize();
        } else {
            operationToExecute = OperationEnum.INIT;
            manageLockAcquisition();
        }
    }

    public void getLocksAndUpdate() {
        if (!myPeer.isAlone()) {
            operationToExecute = OperationEnum.UPDATE;
            manageLockAcquisition();
        }
    }

    private void manageLockAcquisition() {
        myPeer.log("devo aggiornare il puzzle. Invio richiesta lock");

        wantLock = true;
        myPeer.incrementClock();
        myRequestClock = myPeer.getClock();
        myPeer.getAllPeers().forEach(this::askLockTo);
    }

    public void peerJoining(Address actorAddress) {
        if (needLock()) {
            myPeer.log("si è unito " + actorAddress.toString() + ". Invio richiesta lock");

            askLockTo(actorAddress);
        }
    }

    private void askLockTo(Address actorAddress) {
        ClusterSingleton.getInstance().getActorFromAddress(actorAddress)
                .tell(MessageFactory.createAskMessage(myRequestClock), ClusterSingleton.getInstance().getSelf());
    }

    private void grantLockTo(Address actorAddress) {
        ClusterSingleton.getInstance().getActorFromAddress(actorAddress)
                .tell(MessageFactory.createGrantMessage(myPeer.getClock()), ClusterSingleton.getInstance().getSelf());
    }

    public void lockRequestReceived(Address actorAddress, MyLamportClock msgClock) {
        manageClock(msgClock);
        if (hasLock || (wantLock && myRequestClock.compareToClock(msgClock).equals(LESS))) {
            myPeer.log("ricevuta richiesta del lock da " + actorAddress.toString() + ". METTO IN CODA LA RICHIESTA");

            requestQueue.add(actorAddress);
        } else {
            myPeer.log("ricevuta richiesta del lock da " + actorAddress.toString() + ". INVIO ACK");

            grantLockTo(actorAddress);
        }
    }

    public void lockGrantReceived(Address actorAddress, MyLamportClock msgClock) {
        manageClock(msgClock);
        if (needLock()) {
            myPeer.log("ACK ricevuto da " + actorAddress.toString());

            grantQueue.add(actorAddress);
            if (allAckReceived(grantQueue, myPeer.getAllPeers())) {
                executeCriticalSection();
            }
        }
    }

    private void executeCriticalSection() {
        myPeer.log("ricevuti tutti gli ACK. Entro in sezione critica");

        myPeer.incrementClock();
        grantQueue.clear();
        hasLock = true;
        switch (operationToExecute) {
            case INIT -> myPeer.getInitializedPeers().forEach(node -> ClusterSingleton.getInstance()
                    .getActorFromAddress(node)
                    .tell(MessageFactory.createInitBoardReq(myPeer.getClock()),
                            ClusterSingleton.getInstance().getSelf()));
            case UPDATE -> myPeer.getInitializedPeers().forEach(node -> ClusterSingleton.getInstance()
                    .getActorFromAddress(node)
                    .tell(MessageFactory.createUpdateBoard(myPeer.getClock(), myPeer.getPuzzleTiles()),
                            ClusterSingleton.getInstance().getSelf()));
        }
    }

    public void ackReceived(Address actorAddress) {
        if (hasLock) {
            ackQueue.add(actorAddress);
            checkAckQueue(false);
        }
    }

    private void releaseLock() {
        myPeer.log("rilascio il lock");

        myPeer.incrementClock();
        hasLock = false;
        wantLock = false;
        requestQueue.forEach(this::grantLockTo);
        requestQueue.clear();
        grantQueue.clear();
        operationToExecute = OperationEnum.IDLE;
    }

    public void peerLeaving(Address actorAddress) {
        requestQueue.remove(actorAddress);
        myPeer.removePeer(actorAddress);

        if (needLock()) {
            grantQueue.remove(actorAddress);
            if (myPeer.getInitializedPeers().isEmpty()) {
                if (operationToExecute == OperationEnum.INIT) {
                    myPeer.initialize();
                }
                releaseLock();
            } else if (allAckReceived(grantQueue, myPeer.getAllPeers())) {
                executeCriticalSection();
            }
        }
        if (hasLock) {
            ackQueue.remove(actorAddress);
            if(myPeer.getInitializedPeers().isEmpty() && !myPeer.isInitialized()) {
                myPeer.initialize();
            }
            checkAckQueue(myPeer.getAllPeers().isEmpty());
        }
    }

    private void checkAckQueue(boolean peerLeavingCondition) {
        if (allAckReceived(ackQueue, myPeer.getInitializedPeers()) || peerLeavingCondition) {
            ackQueue.clear();
            releaseLock();
        }
    }

    private boolean allAckReceived(Set<Address> s1, Set<Address> s2) {
        return s1.size() == s2.size();
    }

    private void manageClock(MyLamportClock clock) {
        myPeer.updateClock(clock);
        myPeer.incrementClock();
    }

    private boolean needLock() {
        return wantLock && !hasLock;
    }
}
