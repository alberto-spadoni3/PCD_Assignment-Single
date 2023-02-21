package part2.RMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Main {

    //LEGGI SOTTO
    //LEGGI SOTTO
    //LANCIA rmiregistry DAL PATH PCD_Assignment-Single\build\classes\java\main
    //LEGGI SOPRA
    //LEGGI SOPRA

    public static void main(String[] args) {
        NodeHandlerSingleton.createInstance();
        try {
            NodeHandlerSingleton.getInstance().initialize();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}
