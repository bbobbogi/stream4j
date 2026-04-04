package io.github.bbobbogi.stream4j.util;

import java.util.concurrent.TimeUnit;

/**
 * SharedHttpClient의 설정을 담는 클래스입니다.
 * <p>
 * 애플리케이션 시작 시 {@link SharedHttpClient#configure(HttpClientConfig)}를
 * 호출하여 설정을 적용할 수 있습니다. 설정은 최초 {@link SharedHttpClient#get()}
 * 호출 전에 적용되어야 합니다.
 */
public final class HttpClientConfig {

    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0";
    private long connectTimeoutMs = TimeUnit.SECONDS.toMillis(10);
    private long readTimeoutMs = TimeUnit.SECONDS.toMillis(10);
    private int maxRequests = 1024;
    private int maxRequestsPerHost = 1024;

    public HttpClientConfig() {}

    /**
     * User-Agent 헤더 값을 설정합니다.
     *
     * @param userAgent User-Agent 문자열
     * @return this
     */
    public HttpClientConfig userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 연결 타임아웃을 밀리초 단위로 설정합니다.
     *
     * @param ms 타임아웃 (밀리초)
     * @return this
     */
    public HttpClientConfig connectTimeoutMs(long ms) {
        this.connectTimeoutMs = ms;
        return this;
    }

    /**
     * 읽기 타임아웃을 밀리초 단위로 설정합니다.
     *
     * @param ms 타임아웃 (밀리초)
     * @return this
     */
    public HttpClientConfig readTimeoutMs(long ms) {
        this.readTimeoutMs = ms;
        return this;
    }

    /**
     * 최대 동시 요청 수를 설정합니다.
     *
     * @param maxRequests 최대 요청 수
     * @return this
     */
    public HttpClientConfig maxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
        return this;
    }

    /**
     * 호스트당 최대 동시 요청 수를 설정합니다.
     *
     * @param maxRequestsPerHost 호스트당 최대 요청 수
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
