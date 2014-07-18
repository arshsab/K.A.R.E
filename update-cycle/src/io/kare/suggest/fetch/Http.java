package io.kare.suggest.fetch;


import java.io.*;
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

    public HttpResponse get(String url) throws IOException {

        HttpURLConnection conn;
        if (proxy == null) {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } else {
            conn = (HttpURLConnection) new URL(url).openConnection(proxy);
        }

        int code = conn.getResponseCode();

        InputStream stream = code < 400 ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String data = br.lines()
                    .map(s -> s + "\n")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();

            return new HttpResponse(code, data);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}