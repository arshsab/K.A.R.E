package io.kare;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static void main(String... args) throws IOException, InterruptedException {
        System.getProperties().load(new FileInputStream(args[0]));

        Kare kare = new Kare();

        while (!Thread.interrupted()) {
            Logger.important("Starting an update.");
            kare.update();
            Logger.important("Completed an update. Sleeping for 1 / 2 hour before next update.");
            Thread.sleep(1800 * 1000);
        }
    }
}
