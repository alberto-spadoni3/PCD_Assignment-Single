package part1.eventloop.cli;

import io.vertx.core.Vertx;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of parameters.");
            System.exit(-1);
        }

        String rootDirectory = args[0];
        String wordToSearch = args[1];

        System.out.println("SOLUTION BASED ON EVENT-LOOP ASYNCHRONOUS PROGRAMMING");

        Vertx vertx = Vertx.vertx();
        setMessageCodec(vertx);
        vertx.deployVerticle(new Master(wordToSearch, rootDirectory));
    }

    // Method used to allow ArrayList object to be sent and received on the event bus
    public static void setMessageCodec(Vertx vertx) {
        vertx.eventBus().registerDefaultCodec(ArrayList.class,
                new GenericCodec<>(ArrayList.class));
    }
}

