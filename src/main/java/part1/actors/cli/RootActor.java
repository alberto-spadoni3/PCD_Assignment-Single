package part1.actors.cli;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.threads.cli.DocumentsCounter;

public class RootActor extends AbstractBehavior<RootActor.Command> {
    interface Command {}

    public enum StartComputation implements Command {
        INSTANCE
    }

    public enum EndComputation implements Command {
        INSTANCE
    }

    public static Behavior<Command> create(DocumentsCounter documentsCounter, String rootDir, String wordToFind) {
        return Behaviors.setup(context -> new RootActor(context, documentsCounter, rootDir, wordToFind));
    }

    private final DocumentsCounter documentsCounter;
    private final String rootDir;
    private final String wordToFind;
    private Long startTime;

    private RootActor(ActorContext<Command> context, DocumentsCounter documentsCounter, String rootDir, String wordToFind) {
        super(context);
        this.documentsCounter = documentsCounter;
        this.rootDir = rootDir;
        this.wordToFind = wordToFind;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(StartComputation.INSTANCE, this::startComputation)
                .onMessageEquals(EndComputation.INSTANCE, this::onActorsTermination)
                .build();
    }

    private Behavior<Command> onActorsTermination() {
        getContext().getLog().info("Documents found: " + documentsCounter.getDocumentsFound());
        getContext().getLog().info("Documents analyzed: " + documentsCounter.getDocumentsAnalyzed());
        getContext().getLog().info("Word occurrences: " + documentsCounter.getWordOccurrences());
        getContext().getLog().info("Duration: " + (System.currentTimeMillis() - startTime));
        return Behaviors.stopped();
    }

    private Behavior<Command> startComputation() {
        getContext().spawn(ExplorerActor.create(documentsCounter, getContext().getSelf())
                           , "Explorer")
                    .tell(new ExplorerActor.StartExploring(rootDir, wordToFind));
        startTime = System.currentTimeMillis();
        return Behaviors.same();
    }
}
