package part2.actors;

import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.actor.ActorRef;
import akka.actor.ActorContext;
import part2.actors.utility.StringEnum;

public class ClusterSingleton {

    private static ClusterSingleton instance = null;

    private final Cluster cluster;
    private final ActorRef self;
    private final ActorContext context;

    private ClusterSingleton(Cluster cluster, ActorRef self, ActorContext context) {
        this.cluster = cluster;
        this.self = self;
        this.context = context;
    }

    public static ClusterSingleton initialize(Cluster cluster, ActorRef self, ActorContext context) {
        if (instance == null) {
            instance = new ClusterSingleton(cluster, self, context);
        }
        return instance;
    }

    public static ClusterSingleton getInstance() {
        return instance;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public ActorRef getSelf() {
        return self;
    }

    public ActorContext getContext() {
        return context;
    }

    public void downActor(Address address) {
        getCluster().down(address);
    }

    public Address getMyAddress() {
        return getCluster().remotePathOf(getSelf()).address();
    }

    public ActorSelection getActorFromAddress(Address address) {
        return getContext().actorSelection(address.toString() + "/user/" + StringEnum.ACTOR);
    }
}
