package io.kare;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Response {
    public final int responseCode;
    public final String data;

    public Response(int responseCode, String data) {
        this.responseCode = responseCode;
        this.data = data;
    }
}
