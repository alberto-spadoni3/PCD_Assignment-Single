package part1.actors.gui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ExplorerActor extends AbstractBehavior<RootActor.Command> {
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final List<ActorRef<RootActor.Command>> childActors;
    private final ActorRef<RootActor.Command> rootActorRef;
    private final AtomicInteger id;

    public static final class StartExploring implements RootActor.Command {
        public final File rootDir;
        public final String wordToFind;

        public StartExploring(File rootDir, String wordToFind) {
            this.rootDir = rootDir;
            this.wordToFind = wordToFind;
        }
    }

    public static final class WatchMe implements RootActor.Command {
        public final ActorRef<RootActor.Command> actorRef;

        public WatchMe(ActorRef<RootActor.Command> actorRef) {
            this.actorRef = actorRef;
        }
    }

    public static final class Terminated implements RootActor.Command {
        public final ActorRef<RootActor.Command> actorRef;

        public Terminated(ActorRef<RootActor.Command> actorRef) {
            this.actorRef = actorRef;
        }
    }

    private ExplorerActor(ActorContext<RootActor.Command> context, DocumentsCounter documentsCounter, TerminationFlag terminationFlag, ActorRef<RootActor.Command> rootActorRef) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.rootActorRef = rootActorRef;
        this.childActors = new LinkedList<>();
        this.id = new AtomicInteger();
    }

    public static Behavior<RootActor.Command> create(DocumentsCounter documentsCounter, TerminationFlag terminationFlag, ActorRef<RootActor.Command> rootActorRef) {
        return Behaviors.setup(context -> new ExplorerActor(context, documentsCounter, terminationFlag, rootActorRef));
    }

    @Override
    public Receive<RootActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartExploring.class, this::onStartExploring)
                .onMessage(Terminated.class, this::onChildrenTermination)
                .onMessage(WatchMe.class, this::watchActors)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }


    private void discover(File rootDir, StartExploring command) {
        for (File file : Objects.requireNonNull(rootDir.listFiles())) {
            if (this.terminationFlag.canProceed()) {
                if (file.isDirectory())
                    this.discover(file, command);
                else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    getContext()
                            .spawn(LoaderActor.create(documentsCounter,
                                                      terminationFlag,
                                                      getContext().getSelf()), "Loader-" + this.id.getAndIncrement(),
                                    DispatcherSelector.blocking())
                            .tell(new LoaderActor.LoadFile(file, command.wordToFind));
                    getContext().getLog().debug("Found -> " + file.getName());
                    this.documentsCounter.incrementDocumentsFound();
                }
            } else if (this.terminationFlag.isPaused())
                this.terminationFlag.waitToBeResumed();
            else
                break;
        }
    }

    private Behavior<RootActor.Command> onStartExploring(StartExploring command) {
        getContext().getLog().info("Filesystem navigation started");
        this.discover(command.rootDir, command);
        return Behaviors.same();
    }

    private Behavior<RootActor.Command> watchActors(WatchMe command) {
        this.childActors.add(command.actorRef);
        return Behaviors.same();
    }

    private Behavior<RootActor.Command> onChildrenTermination(Terminated command) {
        Behavior<RootActor.Command> behavior = Behaviors.same();
        this.childActors.remove(command.actorRef);
        if (childActors.isEmpty())
            behavior = Behaviors.stopped();
        return behavior;
    }

    private Behavior<RootActor.Command> onPostStop() {
        rootActorRef.tell(RootActor.EndComputation.INSTANCE);
        getContext().getLog().info(getContext().getSelf().path().name() + " stopped");
        return Behaviors.same();
    }
}
