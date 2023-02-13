package part2.actors.handlers;

import akka.actor.Address;
import part2.actors.messages.*;
import part2.actors.utility.MyLamportClock;
import part2.actors.Peer;
import part2.actors.ClusterSingleton;
import part2.actors.utility.CSopEnum;

import java.util.HashSet;
import java.util.Set;


public class LockHandler {

    private final Peer myPeer;
    private final Set<Address> requestQueue;
    private final Set<Address> lockAckQueue;
    private final Set<Address> opAckQueue;
    //private int operationCounter;
    //private int ackCounter;
    private boolean wantLock;
    private boolean hasLock;
    private MyLamportClock myRequestClock;
    private CSopEnum myCSoperation;

    public LockHandler(Peer peer) {
        myPeer = peer;
        requestQueue = new HashSet<>();
        lockAckQueue = new HashSet<>();
        opAckQueue = new HashSet<>();
        wantLock = false;
        hasLock = false;
        myCSoperation = CSopEnum.DEFAULT;

        //operationCounter = 0;
        //ackCounter = 0;
    }

    public void getLocksAndInitialize() {
        if(myPeer.isAlone()) {
            myPeer.log("inizializzo il puzzle");

            myPeer.initialize();
        } else {
            myCSoperation = CSopEnum.INIT;
            manageLockAcquisition();
        }
    }

    public void getLocksAndUpdate() {
        if (!myPeer.isAlone()) {
            myCSoperation = CSopEnum.UPDATE;
            manageLockAcquisition();
        }
    }

    private void manageLockAcquisition () {
        myPeer.log("devo aggiornare il puzzle. Invio richiesta lock");

        wantLock = true;
        myPeer.incrementClock();
        myRequestClock = myPeer.getClock();
        myPeer.getAllPeers().forEach(node -> ClusterSingleton.getInstance()
                .getActorFromAddress(node)
                .tell(MessageFactory.createAskMessage(myRequestClock), ClusterSingleton.getInstance().getSelf()));
    }

    private void releaseLock() {
        myPeer.log("rilascio il lock");

        myPeer.incrementClock();
        hasLock = false;
        wantLock = false;
        requestQueue.forEach(node -> ClusterSingleton.getInstance()
                .getActorFromAddress(node)
                .tell(MessageFactory.createGrantMessage(this.myPeer.getClock()), ClusterSingleton.getInstance().getSelf()));
        requestQueue.clear();
        lockAckQueue.clear();
        //ackCounter = 0;
        myCSoperation = CSopEnum.DEFAULT;
    }

    public void peerJoining(Address address) {
        if(needLock()) {
            myPeer.log("si è unito " + address.toString() + ". Invio richiesta lock");

            ClusterSingleton.getInstance().getActorFromAddress(address)
                    .tell(MessageFactory.createAskMessage(myRequestClock), ClusterSingleton.getInstance().getSelf());
        }
    }

    public void peerLeaving(Address address) {
        requestQueue.remove(address);
        myPeer.removePeer(address);

        if(needLock()) {
            lockAckQueue.remove(address);
            //ackCounter -= - 1;
            if(myPeer.requireInit()) {
                if(myCSoperation == CSopEnum.INIT) {
                    myPeer.initialize();
                }
                releaseLock();
            } else if (lockAckQueue.size() /*ackCounter*/ == myPeer.getAllPeers().size()) {
                executeCriticalSection();
            }
        }
        if(hasLock) {
            opAckQueue.remove(address);
            //operationCounter -= 1;
            if(myPeer.getAllPeers().isEmpty() || opAckQueue.size() /*operationCounter*/ == myPeer.getInitializedPeers().size()) {
                opAckQueue.clear();
                //operationCounter = 0;
                releaseLock();
            }
        }
    }

    public void lockRequestReceived(Address address, LockMessages message) {
        manageClock(message.getClock());
        if(hasLock || (wantLock && myRequestClock.compareClocks(message.getClock()) == MyLamportClock.ClockCompareResult.LESS)) {
            myPeer.log("ricevuta richiesta del lock da " + address.toString() + ". Metto in coda la richiesta");

            requestQueue.add(address);
        } else {
            myPeer.log("ricevuta richiesta del lock da " + address.toString() + ". Invio ACK");

            ClusterSingleton.getInstance().getActorFromAddress(address)
                    .tell(MessageFactory.createGrantMessage(myPeer.getClock()), ClusterSingleton.getInstance().getSelf());
        }
    }

    public void lockAckReceived(Address address, LockMessages message) {
        manageClock(message.getClock());
        if(needLock()) {
            myPeer.log("ACK ricevuto da " + address.toString());

            lockAckQueue.add(address);
            //ackCounter += 1;
            if(lockAckQueue.size() /*ackCounter*/ == myPeer.getAllPeers().size()) {
                executeCriticalSection();
            }
        }
    }

    private void manageClock(MyLamportClock clock) {
        myPeer.updateClock(clock);
        myPeer.incrementClock();
    }

    private void executeCriticalSection() {
        myPeer.log("ricevuti tutti gli ACK. Entro in sezione critica");

        myPeer.incrementClock();
        lockAckQueue.clear();
        //ackCounter = 0;
        hasLock = true;
        switch (myCSoperation) {
            case INIT -> myPeer.getInitializedPeers().forEach(node -> ClusterSingleton.getInstance()
                    .getActorFromAddress(node)
                    .tell(MessageFactory.createInitBoardReq(myPeer.getClock()), ClusterSingleton.getInstance().getSelf()));
            case UPDATE -> myPeer.getInitializedPeers().forEach(node -> ClusterSingleton.getInstance()
                    .getActorFromAddress(node)
                    .tell(MessageFactory.createUpdateBoardReq(myPeer.getClock(), myPeer.getPuzzleBoard().getTiles()),
                            ClusterSingleton.getInstance().getSelf()));
        }
    }

    public void operationAckReceived(Address address) {
        if(hasLock) {
            opAckQueue.add(address);
            //operationCounter += 1;
            if(opAckQueue.size()/*operationCounter*/ == myPeer.getInitializedPeers().size()) {
                opAckQueue.clear();
                //operationCounter = 0;
                releaseLock();
            }
        }
    }

    private boolean needLock(){
        return wantLock && !hasLock;
    }
}
