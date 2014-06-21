package io.kare.suggest.fetch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.*;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Http {
    private static final Proxy proxy;

    static {
        final String host = System.getProperty("fetch.proxy.host");
        final String port = System.getProperty("fetch.proxy.port");

        if (host == null || port == null) {
            proxy = null;
        } else {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
        }
    }

    public String get(String url) throws IOException {

        HttpURLConnection conn;
        if (proxy == null) {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } else {
            conn = (HttpURLConnection) new URL(url).openConnection(proxy);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        try {
            String data = br.lines().map(s -> s + "\n")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();

            return data;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}