package part1.actors.cli;

import akka.actor.typed.ActorSystem;
import part1.threads.cli.DocumentsCounter;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of parameters.");
            System.exit(-1);
        }

        String rootDirectory = args[0];
        String wordToFind = args[1];

        System.out.println("SOLUTION BASED ON THE ACTORS APPROACH");

        ActorSystem<RootActor.Command> actorSystem = ActorSystem.create(RootActor.create(new DocumentsCounter(), new File(rootDirectory), wordToFind), "guardian");

        actorSystem.tell(RootActor.StartComputation.INSTANCE);
    }
}
