package io.kare.suggest.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kare.suggest.Logger;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Fetcher {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final int MAX_CONCURRENT_REQUESTS = (System.getProperty("kare.fetch.max-requests") == null) ?
            8 :
            Integer.parseInt(System.getProperty("kare.fetch.max-requests"));

    private final AtomicInteger dispatched = new AtomicInteger();
    private final AtomicBoolean error = new AtomicBoolean(false);
    private final String access;
    private final Http http = new Http();
    private final AtomicBoolean canProceed = new AtomicBoolean(true);
    private final AtomicBoolean searchCanProceed = new AtomicBoolean(true);

    private String errorMessage;

    public Fetcher() {
        this(null);
    }

    public Fetcher(String access) {
        String attempt = access == null ? System.getProperty("kare.api-key") : access;

        this.access = attempt == null ? "" : attempt;
    }

    // Fetches the URL. Blocks if necessary until API requests are available.
    public String fetch(String url) throws IOException {
        if (error.get()) {
            throw new IOException("A Stop The World error has occurred! Message: " + errorMessage);
        }


        boolean isSearch = isSearchUrl(url);
        String fixed = prepUrl(url);

        if (isSearch) {
            waitOnSearch();
        } else {
            waitOnNormal();
        }

        Logger.debug("Fetching URL: " + url);

        claimRequest();

        boolean claimRelinquished = false;
        String ret;
        try {
            ret = http.get(fixed);
        } catch (IOException ioe) {
            Logger.warn("Got an exception: " + ioe.getMessage());

            // Not found.
            if (ioe.getMessage().contains("404")) {
                throw new FileNotFoundException();

            // Out of requests.
            } else if (ioe.getMessage().contains("403")) {
                if (isSearch) {
                    searchCanProceed.set(false);
                } else {
                    canProceed.set(false);
                }

                while (!(isSearch ? isSearchReady() : isNormalReady())) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    Logger.debug("Waiting for API rate limit to refresh");
                }

                relinquishClaim();
                claimRelinquished = true;
                ret = fetch(url);

            // Restricted resource.
            } else if (ioe.getMessage().contains("422")) {
                throw ioe;
            } else {
                error.set(true);
                errorMessage = ioe.getMessage();
                throw ioe;
            }
        } finally {
            if (!claimRelinquished)
                relinquishClaim();
        }

        return ret;
    }

    private void claimRequest() {
        while (dispatched.getAndIncrement() > MAX_CONCURRENT_REQUESTS) {
            dispatched.getAndDecrement();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void relinquishClaim() {
        dispatched.getAndDecrement();
    }

    private void waitOnNormal() {
        while (!canProceed.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitOnSearch() {
        while (!searchCanProceed.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getApiKey() {
        return access == null ? System.getProperty("kare.api-key") : access;
    }

    private boolean isSearchReady() throws IOException {
        if (searchCanProceed.get()) {
            return true;
        }

        String rateLimit = http.get("https://api.github.com/rate_limit?" + getApiKey());

        JsonNode node = mapper.readTree(rateLimit);

        int value = node.path("resources")
                        .path("search")
                        .path("remaining").intValue();

        boolean result = value != 0;

        searchCanProceed.set(result);
        return result;
    }

    private boolean isNormalReady() throws IOException {
        if (canProceed.get()) {
            return true;
        }

        String rateLimit = http.get("https://api.github.com/rate_limit?" + getApiKey());

        JsonNode node = mapper.readTree(rateLimit);

        int value = node.path("resources")
                .path("core")
                .path("remaining").intValue();

        boolean result = value != 0;

        canProceed.set(result);
        return result;
    }

    private boolean isSearchUrl(String url) {
        return url.startsWith("search") || url.startsWith("/search");
    }

    private String prepUrl(String url) {
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        if (!url.startsWith("https://api.github.com")) {
            url = "https://api.github.com" + url;
        }

        if (!url.contains(getApiKey())) {
            url += url.contains("?") ? "&" : "?";
            url += getApiKey();
        }

        return url;
    }


}
