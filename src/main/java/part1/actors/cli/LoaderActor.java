package part1.actors.cli;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import part1.threads.cli.Document;
import part1.threads.cli.DocumentsCounter;
import part1.common.TerminationFlag;

import java.io.File;
import java.io.IOException;

public class LoaderActor extends AbstractBehavior<RootActor.Command> {
    private final PDFTextStripper stripper;
    private final StringBuilder documentText;
    private final DocumentsCounter documentsCounter;
    private final ActorRef<RootActor.Command> parentActorRef;
    private final TerminationFlag terminationFlag;
    private final boolean GUIVersion;    // 0 = CLI - 1 = GUI

    public static final class LoadFile implements RootActor.Command {
        public final File fileName;
        public final String wordToFind;

        public LoadFile(File fileName, String wordToFind) {
            this.fileName = fileName;
            this.wordToFind = wordToFind;
        }
    }

    public enum ChildTerminated implements RootActor.Command {
        INSTANCE
    }

    public static Behavior<RootActor.Command> create(DocumentsCounter documentsCounter, ActorRef<RootActor.Command> parentActorRef) {
        return Behaviors.setup(context -> new LoaderActor(context, documentsCounter, parentActorRef));
    }

    public static Behavior<RootActor.Command> createGUI(DocumentsCounter documentsCounter, ActorRef<RootActor.Command> parentActorRef, TerminationFlag terminationFlag) {
        return Behaviors.setup(context -> new LoaderActor(context, documentsCounter, parentActorRef, terminationFlag));
    }

    private LoaderActor(ActorContext<RootActor.Command> context, DocumentsCounter documentsCounter, ActorRef<RootActor.Command> parentActorRef) throws IOException {
        super(context);
        this.documentsCounter = documentsCounter;
        this.parentActorRef = parentActorRef;
        this.stripper = new PDFTextStripper();
        this.documentText = new StringBuilder();
        this.GUIVersion = false;
        this.terminationFlag = null;
    }

    private LoaderActor(ActorContext<RootActor.Command> context, DocumentsCounter documentsCounter, ActorRef<RootActor.Command> parentActorRef, TerminationFlag terminationFlag) throws IOException {
        super(context);
        this.documentsCounter = documentsCounter;
        this.parentActorRef = parentActorRef;
        this.stripper = new PDFTextStripper();
        this.documentText = new StringBuilder();
        this.GUIVersion = true;
        this.terminationFlag = terminationFlag;
    }

    @Override
    public Receive<RootActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(LoadFile.class, this::loadDocument)
                .onMessageEquals(ChildTerminated.INSTANCE, this::onChildTermination)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<RootActor.Command> loadDocument(LoadFile command) {
        this.parentActorRef.tell(new ExplorerActor.WatchMe(getContext().getSelf()));
        getContext().getLog().info("Loading file: " + command.fileName.getName());
        try {
            PDDocument documentLoaded = PDDocument.load(command.fileName);
            AccessPermission permission = documentLoaded.getCurrentAccessPermission();
            if (this.GUIVersion) {
                if (this.terminationFlag.isNotStopped() && permission.canExtractContent()) {
                    if (this.terminationFlag.isPaused())
                        this.terminationFlag.waitToBeResumed();

                    for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);
                        documentText.append(stripper.getText(documentLoaded));
                    }
                    Document doc = new Document(command.fileName.getName(), documentText.toString());
                    getContext()
                            .spawn(AnalyzerActor.createGUI(command.wordToFind,
                                            documentsCounter,
                                            getContext().getSelf(),
                                            terminationFlag)
                                    , "Analyzer-" + this.getID())
                            .tell(new AnalyzerActor.AnalyzeDocument(doc));
                }
            } else {
                if (permission.canExtractContent()) {
                    for (int i = 1; i <= documentLoaded.getNumberOfPages(); i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);
                        documentText.append(stripper.getText(documentLoaded));
                    }
                    Document doc = new Document(command.fileName.getName(), documentText.toString());
                    getContext()
                            .spawn(AnalyzerActor.create(command.wordToFind,
                                            documentsCounter,
                                            getContext().getSelf())
                                    , "Analyzer-" + this.getID())
                            .tell(new AnalyzerActor.AnalyzeDocument(doc));
                }
            }
            documentLoaded.close();
            documentText.delete(0, documentText.length());
        } catch (IOException e) {
            getContext().getLog().error("Something went wrong with PDF " + command.fileName);
        }
        return Behaviors.same();
    }

    private Behavior<RootActor.Command> onPostStop() {
        getContext().getLog().debug(getContext().getSelf().path().name() + " stopped");
        return Behaviors.same();
    }

    private Behavior<RootActor.Command> onChildTermination() {
        this.parentActorRef.tell(new ExplorerActor.Terminated(getContext().getSelf()));
        return Behaviors.stopped();
    }

    private String getID() {
        return getContext().getSelf().path().name().split("-")[1];
    }
}
