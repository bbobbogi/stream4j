package io.github.bbobbogi.stream4j.util;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shared {@link okhttp3.OkHttpClient} singleton for the entire library.
 *
 * <p>All platform clients share one Dispatcher (maxRequests=1024) and
 * ConnectionPool for efficient concurrent connection management.
 *
 * <p>Use {@link #newBuilder()} to create derived clients that share the
 * same Dispatcher and ConnectionPool while adding interceptors.
 *
 * <p>Custom settings can be applied via {@link #configure(HttpClientConfig)}
 * before the first call to {@link #get()}.
 *
 * @since 1.0.0
 */
public final class SharedHttpClient {

    private static final AtomicReference<HttpClientConfig> CONFIG = new AtomicReference<>(new HttpClientConfig());
    private static volatile OkHttpClient INSTANCE;
    private static final Object LOCK = new Object();

    private SharedHttpClient() {
    }

    /**
     * Applies custom configuration before the client is initialized.
     *
     * <p>Must be called before the first call to {@link #get()}.
     *
     * @param config configuration to apply
     * @throws IllegalStateException if the client is already initialized
     */
    public static void configure(HttpClientConfig config) {
        if (INSTANCE != null) {
            throw new IllegalStateException("SharedHttpClient is already initialized. configure() must be called before get().");
        }
        CONFIG.set(config);
    }

    /**
     * Returns the current configuration.
     *
     * @return current configuration (read-only)
     */
    public static HttpClientConfig getConfig() {
        return CONFIG.get();
    }

    /**
     * Returns the shared {@link okhttp3.OkHttpClient} instance.
     * The client is lazily initialized on first call.
     *
     * @return shared OkHttpClient
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
     * Returns a builder derived from the shared client.
     * The derived client shares Dispatcher and ConnectionPool.
     *
     * @return a new OkHttpClient.Builder
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
     * Resets the client for testing purposes.
     * Do not use in production code.
     */
    static void resetForTesting() {
        synchronized (LOCK) {
            INSTANCE = null;
            CONFIG.set(new HttpClientConfig());
        }
    }
}
