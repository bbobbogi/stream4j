package xyz.r2turntrue.chzzk4j.util;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;

/**
 * 프로젝트 전체에서 공유하는 OkHttpClient입니다.
 * <p>
 * Dispatcher(maxRequests=1024)와 ConnectionPool을 공유하여
 * 다수의 동시 연결을 효율적으로 관리합니다.
 * <p>
 * 인증이 필요한 경우 {@link #newBuilder()}로 파생 클라이언트를 생성하세요.
 * 파생 클라이언트는 Dispatcher와 ConnectionPool을 공유합니다.
 */
public final class SharedHttpClient {

    private static final OkHttpClient INSTANCE;

    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1024);
        dispatcher.setMaxRequestsPerHost(1024);
        INSTANCE = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    private SharedHttpClient() {
    }

    /**
     * 공유 OkHttpClient 인스턴스를 반환합니다.
     *
     * @return 공유 OkHttpClient
     */
    public static OkHttpClient get() {
        return INSTANCE;
    }

    /**
     * 공유 클라이언트에서 파생된 빌더를 반환합니다.
     * Dispatcher와 ConnectionPool을 공유하면서 인터셉터 등을 추가할 수 있습니다.
     *
     * @return OkHttpClient.Builder
     */
    public static OkHttpClient.Builder newBuilder() {
        return INSTANCE.newBuilder();
    }
}
