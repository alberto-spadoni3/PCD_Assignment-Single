package part1.threads.cli;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of parameters.");
            System.exit(-1);
        }

        File rootDirectory = new File(args[0]);
        String wordToSearch = args[1].toLowerCase();

        System.out.println("SOLUTION BASED ON THREADS APPROACH");

        Master m = new Master(rootDirectory, wordToSearch);
        m.start();
        try {
            m.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
