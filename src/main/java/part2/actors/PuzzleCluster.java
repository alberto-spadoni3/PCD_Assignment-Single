package part2.actors;

import akka.actor.AbstractActor;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.MemberEvent;
import part2.actors.messages.SwapTilesMessage;
import part2.actors.handlers.PuzzleHandler;
import part2.actors.messages.BoardStatefulMessage;
import part2.actors.messages.BoardStatelessMessages;
import part2.actors.messages.LockMessages;

public class PuzzleCluster extends AbstractActor {
    private final Cluster cluster = Cluster.get(getContext().getSystem());
    private PuzzleHandler myPuzzleHandler;

    //Effettuato al primo avvio dell'Attore e quando crasha e quindi deve essere riavviato
    @Override
    public void preStart() {
        ClusterSingleton.initialize(cluster, getSelf(), getContext());
        cluster.subscribe(getSelf(),
                ClusterEvent.initialStateAsEvents(),
                MemberEvent.class,
                UnreachableMember.class);
        myPuzzleHandler = new PuzzleHandler();
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Crash dell'attore dovuto a:" + reason.getMessage() + " - FINE");
        super.postRestart(reason);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Nuovo nodo
                .match(MemberUp.class, memberUp -> myPuzzleHandler
                        .peerJoiningHandler(memberUp.member().address()))

                // Nodo non raggiungibile
                .match(UnreachableMember.class, unreachableMember -> myPuzzleHandler
                        .peerUnreachableHandler(unreachableMember.member().address()))

                // Nodo rimosso
                .match(MemberRemoved.class, memberRemoved -> myPuzzleHandler
                        .peerLeavingHandler(memberRemoved.member().address()))

                // GESTIONE LOCK
                .match(LockMessages.class, lockMessages -> myPuzzleHandler
                        .lockMessageHandler(getSenderAddress(), lockMessages))

                // GESTIONE MESSAGGIO CHE CONTIENE IL PUZZLE
                .match(BoardStatefulMessage.class, boardStatefulMessage -> myPuzzleHandler
                        .boardStatefulMessageHandler(getSenderAddress(), boardStatefulMessage))

                // GESTIONE MESSAGGIO CHE NON CONTIENE IL PUZZLE
                .match(BoardStatelessMessages.class, boardStatelessMessages -> myPuzzleHandler
                        .boardStatelessMessageHandler(getSenderAddress(), boardStatelessMessages))

                // SWAP TESSERE
                .match(SwapTilesMessage.class, swapTilesMessage -> myPuzzleHandler
                        .updateBoard())

                .build();
    }

    private Address getSenderAddress() {
        return ClusterSingleton
                .getInstance()
                .getCluster()
                .remotePathOf(ClusterSingleton.getInstance().getContext().sender())
                .address();
    }
}
