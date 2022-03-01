package part1.reactive.cli;

import java.io.File;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of parameters.");
            System.exit(-1);
        }

        File rootDirectory = new File(args[0]);
        String wordToSearch = args[1];

        System.out.println("SOLUTION BASED ON REACTIVE PROGRAMMING");

        Master master = new Master(rootDirectory, wordToSearch);
        Executors.newSingleThreadExecutor().execute(master);
    }
}
