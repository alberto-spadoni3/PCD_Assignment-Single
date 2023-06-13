package part1.actors.cli;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ExplorerActor extends AbstractBehavior<RootActor.Command> {
    private final DocumentsCounter documentsCounter;
    private final List<ActorRef<RootActor.Command>> childActors;
    private final ActorRef<RootActor.Command> rootActorRef;
    private final TerminationFlag terminationFlag;
    private final AtomicInteger id;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

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

    private ExplorerActor(ActorContext<RootActor.Command> context, DocumentsCounter documentsCounter, ActorRef<RootActor.Command> rootActorRef) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.rootActorRef = rootActorRef;
        this.childActors = new LinkedList<>();
        this.id = new AtomicInteger();
        this.GUIVersion = false;
        this.terminationFlag = null;
    }

    private ExplorerActor(ActorContext<RootActor.Command> context, DocumentsCounter documentsCounter, ActorRef<RootActor.Command> rootActorRef, TerminationFlag terminationFlag) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.rootActorRef = rootActorRef;
        this.childActors = new LinkedList<>();
        this.id = new AtomicInteger();
        this.GUIVersion = true;
        this.terminationFlag = terminationFlag;
    }

    public static Behavior<RootActor.Command> create(DocumentsCounter documentsCounter, ActorRef<RootActor.Command> rootActorRef) {
        return Behaviors.setup(context -> new ExplorerActor(context, documentsCounter, rootActorRef));
    }

    public static Behavior<RootActor.Command> createGUI(DocumentsCounter documentsCounter, ActorRef<RootActor.Command> rootActorRef, TerminationFlag terminationFlag) {
        return Behaviors.setup(context -> new ExplorerActor(context, documentsCounter, rootActorRef, terminationFlag));
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
        if (this.GUIVersion) {
            for (File file : Objects.requireNonNull(rootDir.listFiles())) {
                if (this.terminationFlag.canProceed()) {
                    if (file.isDirectory())
                        this.discover(file, command);
                    else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        getContext()
                                .spawn(LoaderActor.createGUI(documentsCounter, getContext().getSelf(), terminationFlag), "Loader-" + this.id.getAndIncrement(),
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
        } else {
            for (File file : Objects.requireNonNull(rootDir.listFiles())) {
                if (file.isDirectory())
                    this.discover(file, command);
                else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    getContext()
                            .spawn(LoaderActor.create(documentsCounter, getContext().getSelf()), "Loader-" + this.id.getAndIncrement(),
                                    DispatcherSelector.blocking())
                            .tell(new LoaderActor.LoadFile(file, command.wordToFind));
                    getContext().getLog().debug("Found -> " + file.getName());
                    this.documentsCounter.incrementDocumentsFound();
                }
            }
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
        this.childActors.remove(command.actorRef);
        return childActors.isEmpty() ? Behaviors.stopped() : Behaviors.same();
    }

    private Behavior<RootActor.Command> onPostStop() {
        rootActorRef.tell(RootActor.EndComputation.INSTANCE);
        getContext().getLog().info(getContext().getSelf().path().name() + " stopped");
        return Behaviors.same();
    }

}
