package cz.zcu.kiv.nlp.ir.trec;
import cz.zcu.kiv.nlp.ir.trec.utils.Messages;

/**
 * Hlavní třída aplikace. Obsahuje spustitelný bod.
 * @author Zdeněk Častorál
 */
public class App {

    /**
     * Vstupní bod aplikace.
     * @param args argumenty příkazové řádky
     */
    public static void main(String args[]) {
        initialization();
        run();
        exit();
    }

    /**
     * Úvodní výpis informací o aplikaci.
     */
    private static void initialization() {
        System.out.print(Messages.HEADER.getText());
        System.out.print(Messages.USAGE.getText());
    }

    /**
     * Metoda sloužící ke spuštění shellu.
     */
    private static void run() {
        new Shell().run();
    }

    /**
     * Vypíše informaci o ukončení aplikace a následně aplikaci ukončí.
     */
    private static void exit() {
        System.out.print(Messages.EXIT.getText());
        System.exit(0);
    }

}
