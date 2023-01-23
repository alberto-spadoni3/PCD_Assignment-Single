package part1.actors.cli;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.actors.gui.GUIUpdaterActor;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;
import part1.common.View;

import java.io.File;

public class RootActor extends AbstractBehavior<RootActor.Command> {
    public interface Command {}

    public enum StartComputation implements Command {
        INSTANCE
    }

    public enum EndComputation implements Command {
        INSTANCE
    }

    public enum StartUpdatingGui implements Command {
        INSTANCE
    }

    public static Behavior<Command> create(DocumentsCounter documentsCounter, File rootDir, String wordToFind) {
        return Behaviors.setup(context -> new RootActor(context, documentsCounter, rootDir, wordToFind));
    }

    public static Behavior<Command> createGUI(DocumentsCounter documentsCounter, File rootDir, String wordToFind, TerminationFlag terminationFlag, View view) {
        return Behaviors.setup(context -> new RootActor(context, documentsCounter, rootDir, wordToFind, terminationFlag, view));
    }

    private final DocumentsCounter documentsCounter;
    private final File rootDir;
    private final String wordToFind;
    private long startTime;
    private final TerminationFlag terminationFlag;
    private final View view;
    private final ActorRef<RootActor.Command> GUIUpdater;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    private RootActor(ActorContext<Command> context, DocumentsCounter documentsCounter, File rootDir, String wordToFind) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.rootDir = rootDir;
        this.wordToFind = wordToFind;
        this.GUIVersion = false;
        this.terminationFlag = null;
        this.view = null;
        this.GUIUpdater = null;
    }

    private RootActor(ActorContext<Command> context, DocumentsCounter documentsCounter, File rootDir, String wordToFind, TerminationFlag terminationFlag, View view) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.rootDir = rootDir;
        this.wordToFind = wordToFind;
        this.GUIVersion = true;
        this.terminationFlag = terminationFlag;
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

    private Behavior<Command> onActorsTermination() {
        long duration = System.currentTimeMillis() - startTime;
        getContext().getLog().info("Done in: " + duration + " ms");
        getContext().getLog().info("Documents found: " + documentsCounter.getDocumentsFound());
        getContext().getLog().info("Word occurrences: " + documentsCounter.getWordOccurrences());

        if(this.GUIVersion) {
            view.setComputationDuration(duration);
            view.computationDone();
            this.terminationFlag.stop();
        }

        return Behaviors.stopped();
    }

    private Behavior<Command> startComputation() {
        if (this.GUIVersion) {
            getContext().spawn(ExplorerActor.createGUI(documentsCounter, getContext().getSelf(), terminationFlag)
                            , "Explorer")
                    .tell(new ExplorerActor.StartExploring(rootDir, wordToFind));

            getContext().getSelf().tell(StartUpdatingGui.INSTANCE);

            startTime = System.currentTimeMillis();
            return Behaviors.same();
        } else {
            getContext().spawn(ExplorerActor.create(documentsCounter, getContext().getSelf())
                            , "Explorer")
                    .tell(new ExplorerActor.StartExploring(rootDir, wordToFind));
            startTime = System.currentTimeMillis();
            return Behaviors.same();
        }
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().info("Guardian stopped");
        return Behaviors.same();
    }

    private Behavior<Command> startUpdatingGUI() {
        this.GUIUpdater.tell(GUIUpdaterActor.UpdateGUI.INSTANCE);
        return Behaviors.same();
    }
}
