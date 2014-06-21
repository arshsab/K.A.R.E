package io.kare.suggest.fetch;

/**
 * @author arshsab
 * @since 06 2014
 */

public class HttpResponse {
    public final int responseCode;
    public final String response;

    public HttpResponse(int responseCode, String response) {
        this.responseCode = responseCode;
        this.response = response;
    }
}
