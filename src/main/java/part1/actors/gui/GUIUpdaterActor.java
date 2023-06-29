package part1.actors.gui;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import part1.actors.cli.RootActor;
import part1.common.TerminationFlag;
import part1.common.View;
import part1.threads.cli.DocumentsCounter;

import java.util.List;

public class GUIUpdaterActor extends AbstractBehavior<RootActor.Command> {
    public enum UpdateGUI implements RootActor.Command {
        INSTANCE;
    }

    private final View view;
    private final DocumentsCounter documentsCounter;
    private final TerminationFlag terminationFlag;

    private GUIUpdaterActor(ActorContext<RootActor.Command> context, View view, DocumentsCounter documentsCounter, TerminationFlag terminationFlag) {
        super(context);
        this.view = view;
        this.documentsCounter = documentsCounter;
        this.terminationFlag = terminationFlag;
    }

    public static Behavior<RootActor.Command> create(View view, DocumentsCounter documentsCounter, TerminationFlag terminationFlag) {
        return Behaviors.setup(context -> new GUIUpdaterActor(context, view, documentsCounter, terminationFlag));
    }

    @Override
    public Receive<RootActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(UpdateGUI.INSTANCE, this::updateGUI)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<RootActor.Command> onPostStop() {
        // Updating the view for the last time before stopping the actor
        view.update(documentsCounter.getDocumentsFound(),
                    documentsCounter.getDocumentsAnalyzed(),
                    documentsCounter.getWordOccurrences());
        return Behaviors.same();
    }

    private Behavior<RootActor.Command> updateGUI() {
        while (terminationFlag.canProceed()) {
            List<Integer> temp = this.documentsCounter.getDocFoundAnalyzedAndWordOccurences();
            this.view.update(temp.get(0), temp.get(1), temp.get(2));
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
        }

        return Behaviors.same();
    }
}
