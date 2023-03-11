package part2.actors;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import part2.actors.utility.StringEnum;

import java.util.*;

public class Main {
    private static final int PORT = 31000;
    private static final int MAX_NODES = 6;

    public static void main(String[] args) {
        int port = PORT;
        int activeNodes = 0;
        boolean foundPort = false;
        List<String> nodes = new ArrayList<>();
        Map<String, Object> newConf;

        nodes.add("akka://" + StringEnum.SYSTEM + "@127.0.0.1:" + port);

        while (!foundPort && activeNodes < MAX_NODES) {
            try {
                newConf = new HashMap<>();
                newConf.put("akka.remote.artery.canonical.port", port);
                newConf.put("akka.cluster.seed-nodes", nodes);

                Config myConfig = ConfigFactory.parseMap(newConf).withFallback(ConfigFactory.load());

                ActorSystem mySystem = ActorSystem.create(StringEnum.SYSTEM.toString(), myConfig);
                mySystem.actorOf(Props.create(PuzzleCluster.class), StringEnum.ACTOR.toString());

                foundPort = true;
            } catch (Exception e) {
                System.out.println("La porta " + port + " e' gia' in uso, verra' quindi provata la successiva");
                port++;
                nodes.add("akka://" + StringEnum.SYSTEM + "@127.0.0.1:" + port);
                activeNodes += 1;
            }
        }

        if (activeNodes >= MAX_NODES) {
            System.out.println("Raggiunto il numero massimo di nodi...");
        }
    }
}
