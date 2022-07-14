package part1.actors.gui;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import part1.actors.gui.RootActor;
import part1.threads.cli.DocumentsCounter;
import part1.threads.gui.InputListener;
import part1.threads.gui.TerminationFlag;
import part1.threads.gui.View;

import java.io.File;

public class Controller implements InputListener {
    private final View view;
    private final TerminationFlag terminationFlag;
    private ActorSystem<RootActor.Command> actorSystem = null;

    public Controller(View view) {
        this.view = view;
        terminationFlag = new TerminationFlag();
    }

    @Override
    public void started(File rootDirectory, String wordToFind) {
        this.terminationFlag.reset();

        this.actorSystem = ActorSystem.create(
                RootActor.create(
                        new DocumentsCounter(),
                        terminationFlag,
                        rootDirectory,
                        wordToFind,
                        view),
                "gardian");

        actorSystem.tell(RootActor.StartComputation.INSTANCE);
    }

    @Override
    public void paused() {
        this.terminationFlag.pause();
    }

    @Override
    public void resumed() {
        this.terminationFlag.resume();
        this.actorSystem.tell(RootActor.StartUpdatingGui.INSTANCE);
    }

    @Override
    public void stopped() {
        this.terminationFlag.stop();
    }
}
