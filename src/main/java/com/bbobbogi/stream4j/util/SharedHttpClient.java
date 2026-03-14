package com.bbobbogi.stream4j.util;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 프로젝트 전체에서 공유하는 OkHttpClient입니다.
 * <p>
 * Dispatcher(maxRequests=1024)와 ConnectionPool을 공유하여
 * 다수의 동시 연결을 효율적으로 관리합니다.
 * <p>
 * 인증이 필요한 경우 {@link #newBuilder()}로 파생 클라이언트를 생성하세요.
 * 파생 클라이언트는 Dispatcher와 ConnectionPool을 공유합니다.
 * <p>
 * 커스텀 설정이 필요한 경우 {@link #configure(HttpClientConfig)}를 사용하세요.
 * 설정은 반드시 {@link #get()} 호출 전에 적용되어야 합니다.
 */
public final class SharedHttpClient {

    private static final AtomicReference<HttpClientConfig> CONFIG = new AtomicReference<>(new HttpClientConfig());
    private static volatile OkHttpClient INSTANCE;
    private static final Object LOCK = new Object();

    private SharedHttpClient() {
    }

    /**
     * SharedHttpClient의 설정을 지정합니다.
     * <p>
     * 이 메서드는 {@link #get()} 호출 전에 호출되어야 합니다.
     * 이미 클라이언트가 초기화된 후에는 설정 변경이 무시됩니다.
     *
     * @param config 적용할 설정
     * @throws IllegalStateException 이미 클라이언트가 초기화된 경우
     */
    public static void configure(HttpClientConfig config) {
        if (INSTANCE != null) {
            throw new IllegalStateException("SharedHttpClient is already initialized. configure() must be called before get().");
        }
        CONFIG.set(config);
    }

    /**
     * 현재 설정을 반환합니다.
     *
     * @return 현재 설정 (읽기 전용 용도)
     */
    public static HttpClientConfig getConfig() {
        return CONFIG.get();
    }

    /**
     * 공유 OkHttpClient 인스턴스를 반환합니다.
     * 최초 호출 시 클라이언트가 초기화됩니다.
     *
     * @return 공유 OkHttpClient
     */
    public static OkHttpClient get() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = createClient(CONFIG.get());
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 공유 클라이언트에서 파생된 빌더를 반환합니다.
     * Dispatcher와 ConnectionPool을 공유하면서 인터셉터 등을 추가할 수 있습니다.
     *
     * @return OkHttpClient.Builder
     */
    public static OkHttpClient.Builder newBuilder() {
        return get().newBuilder();
    }

    private static OkHttpClient createClient(HttpClientConfig config) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(config.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(config.getMaxRequestsPerHost());

        String userAgent = config.getUserAgent();

        return new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("User-Agent", userAgent)
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    /**
     * 테스트 용도로 클라이언트를 리셋합니다.
     * 프로덕션 코드에서는 사용하지 마세요.
     */
    static void resetForTesting() {
        synchronized (LOCK) {
            INSTANCE = null;
            CONFIG.set(new HttpClientConfig());
        }
    }
}
