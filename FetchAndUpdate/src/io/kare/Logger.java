package io.kare;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Logger {
    public static void important(String str) {
        System.out.println("[IMPORTANT]: " + str);
    }

    public static void fatal(String str) {
        System.out.println("[FATAL]: " + str);
    }

    public static void info(String str) {
        System.out.println("[INFO]: " + str);
    }

    public static void warn(String str) {
        System.out.println("[WARN]: " + str);
    }
}
