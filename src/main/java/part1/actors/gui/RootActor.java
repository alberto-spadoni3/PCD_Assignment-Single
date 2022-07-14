package part1.actors.gui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;

public class RootActor extends AbstractBehavior<RootActor.Command> {
    interface Command {}

    public enum StartComputation implements Command {
        INSTANCE
    }

    public enum EndComputation implements Command {
        INSTANCE
    }

    public enum StartUpdatingGui implements Command {
        INSTANCE
    }

    public static Behavior<Command> create(DocumentsCounter documentsCounter, TerminationFlag terminationFlag, File rootDir, String wordToFind, View view) {
        return Behaviors.setup(context -> new RootActor(context, documentsCounter, terminationFlag, rootDir, wordToFind, view));
    }

    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final File rootDir;
    private final String wordToFind;
    private long startTime;
    private final View view;
    private final ActorRef<RootActor.Command> GUIUpdater;

    private RootActor(ActorContext<Command> context, DocumentsCounter documentsCounter, TerminationFlag terminationFlag, File rootDir, String wordToFind, View view) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.rootDir = rootDir;
        this.wordToFind = wordToFind;
        this.view = view;
        this.GUIUpdater = context.spawn(
                GUIUpdaterActor.create(view,
                        documentsCounter,
                        terminationFlag)
                , "GUI-updater");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(StartComputation.INSTANCE, this::startComputation)
                .onMessageEquals(EndComputation.INSTANCE, this::onActorsTermination)
                .onMessageEquals(StartUpdatingGui.INSTANCE, this::startUpdatingGUI)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().info("Guardian stopped");
        return Behaviors.same();
    }

    private Behavior<Command> onActorsTermination() {
        long duration = System.currentTimeMillis() - startTime;

        view.setComputationDuration(duration);
        view.computationDone();

        getContext().getLog().info("Documents found: " + documentsCounter.getDocumentsFound());
        getContext().getLog().info("Documents analyzed: " + documentsCounter.getDocumentsAnalyzed());
        getContext().getLog().info("Word occurrences: " + documentsCounter.getWordOccurrences());
        getContext().getLog().info("Duration: " + duration);

        // need this to terminate the GUIUpdater actor and, then, the whole actorSystem
        this.terminationFlag.stop();
        return Behaviors.stopped();
    }

    private Behavior<Command> startComputation() {
        getContext().spawn(ExplorerActor.create(documentsCounter, terminationFlag, getContext().getSelf())
                           , "Explorer")
                    .tell(new ExplorerActor.StartExploring(rootDir, wordToFind));

        getContext().getSelf().tell(StartUpdatingGui.INSTANCE);

        startTime = System.currentTimeMillis();
        return Behaviors.same();
    }

    private Behavior<Command> startUpdatingGUI() {
        this.GUIUpdater.tell(GUIUpdaterActor.UpdateGUI.INSTANCE);
        return Behaviors.same();
    }
}
