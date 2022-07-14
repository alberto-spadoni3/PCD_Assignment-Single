package part1.actors.gui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.TerminationFlag;

public class AnalyzerActor extends AbstractBehavior<RootActor.Command> {
    private final String wordToFind;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;
    private final ActorRef<RootActor.Command> parentActorRef;

    public static final class AnalyzeDocument implements RootActor.Command {
        public final Document document;

        public AnalyzeDocument(Document document) {
            this.document = document;
        }
    }

    private AnalyzerActor(ActorContext<RootActor.Command> context, String wordToFind, DocumentsCounter documentsCounter, TerminationFlag terminationFlag, ActorRef<RootActor.Command> parentActorRef) {
        super(context);
        this.wordToFind = wordToFind;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
        this.parentActorRef = parentActorRef;
    }

    public static Behavior<RootActor.Command> create(String wordToFind, DocumentsCounter documentsCounter, TerminationFlag terminationFlag, ActorRef<RootActor.Command> rootActorRef) {
        return Behaviors.setup(context -> new AnalyzerActor(context, wordToFind, documentsCounter, terminationFlag, rootActorRef));
    }

    @Override
    public Receive<RootActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AnalyzeDocument.class, this::analyzeDocument)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<RootActor.Command> analyzeDocument(AnalyzeDocument command) {
        getContext().getLog().info("Analyzing " + command.document.getName());
        if (this.terminationFlag.isNotStopped()) {
            if (this.terminationFlag.isPaused())
                this.terminationFlag.waitToBeResumed();

            String[] words = this.splitText(command.document.getContent());
            for (String word : words) {
                if (wordToFind.equals(word.toLowerCase())) {
                    documentsCounter.incrementWordOccurrence();
                    getContext().getLog().info("Word " + wordToFind + " found inside " + command.document.getName());
                    break;
                }
            }
            documentsCounter.incrementDocumentsAnalyzed();
        }
        return Behaviors.stopped();
    }

    private String[] splitText(String textToSplit) {
        String regex = "[\\x{201D}\\x{201C}\\s'\", ?.@;:!-]+";
        return textToSplit.split(regex);
    }

    private Behavior<RootActor.Command> onPostStop() {
        this.parentActorRef.tell(LoaderActor.ChildTerminated.INSTANCE);
        getContext().getLog().debug(getContext().getSelf().path().name() + " stopped");
        return this;
    }
}
