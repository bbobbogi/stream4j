package io.github.bbobbogi.stream4j.util;

import java.util.concurrent.TimeUnit;

/**
 * Configuration holder for {@link SharedHttpClient}.
 * <p>
 * Apply settings during application startup by calling
 * {@link SharedHttpClient#configure(HttpClientConfig)}.
 * Configuration must be applied before the first call to
 * {@link SharedHttpClient#get()}.
 *
 * @since 1.0.0
 */
public final class HttpClientConfig {

    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0";
    private long connectTimeoutMs = TimeUnit.SECONDS.toMillis(10);
    private long readTimeoutMs = TimeUnit.SECONDS.toMillis(10);
    private int maxRequests = 1024;
    private int maxRequestsPerHost = 1024;

    public HttpClientConfig() {}

    /**
     * Configures the {@code User-Agent} header value.
     * <p>
     * Default value:
     * {@code Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0}.
     *
     * @param userAgent User-Agent string
     * @return this
     */
    public HttpClientConfig userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Configures the connection timeout in milliseconds.
     * <p>
     * Default value: {@code 10000} ms (10 seconds).
     *
     * @param ms timeout in milliseconds
     * @return this
     */
    public HttpClientConfig connectTimeoutMs(long ms) {
        this.connectTimeoutMs = ms;
        return this;
    }

    /**
     * Configures the read timeout in milliseconds.
     * <p>
     * Default value: {@code 10000} ms (10 seconds).
     *
     * @param ms timeout in milliseconds
     * @return this
     */
    public HttpClientConfig readTimeoutMs(long ms) {
        this.readTimeoutMs = ms;
        return this;
    }

    /**
     * Configures the maximum number of concurrent requests.
     * <p>
     * Default value: {@code 1024}.
     *
     * @param maxRequests maximum concurrent requests
     * @return this
     */
    public HttpClientConfig maxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
        return this;
    }

    /**
     * Configures the maximum number of concurrent requests per host.
     * <p>
     * Default value: {@code 1024}.
     *
     * @param maxRequestsPerHost maximum concurrent requests per host
     * @return this
     */
    public HttpClientConfig maxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
        return this;
    }

    public String getUserAgent() { return userAgent; }
    public long getConnectTimeoutMs() { return connectTimeoutMs; }
    public long getReadTimeoutMs() { return readTimeoutMs; }
    public int getMaxRequests() { return maxRequests; }
    public int getMaxRequestsPerHost() { return maxRequestsPerHost; }
}
