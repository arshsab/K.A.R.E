package io.kare.suggest;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Logger {
    private static boolean debug;

    static {
        String debug = System.getProperty("debug");

        Logger.debug = debug != null && Boolean.parseBoolean(debug);
    }

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

    public static void debug(String str) {
        if (debug)
            System.out.println("[DEBUG]: " + str);
    }
}
