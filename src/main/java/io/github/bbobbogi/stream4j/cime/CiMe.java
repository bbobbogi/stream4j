package io.github.bbobbogi.stream4j.cime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bbobbogi.stream4j.cime.types.CiMeChannelInfo;
import io.github.bbobbogi.stream4j.cime.types.CiMeLiveInfo;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * CiMe platform API client.
 *
 * <p>Provides read-only access to channel and live metadata and creates chat builders.
 *
 * @since 1.0.0
 */
public class CiMe {
    public static String API_URL = "https://ci.me/api/app";
    public static String JSON_URL = "https://ci.me/json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36";

    public boolean isDebug = false;

    private final OkHttpClient httpClient;
    private final Gson gson;

    CiMe(CiMeBuilder builder) {
        this.gson = new Gson();
        this.isDebug = builder.isDebugEnabled();

        if (builder.cookie == null || builder.cookie.isBlank()) {
            this.httpClient = SharedHttpClient.get();
        } else {
            this.httpClient = SharedHttpClient.newBuilder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request authorized = original.newBuilder()
                                .addHeader("Cookie", builder.cookie)
                                .build();
                        return chain.proceed(authorized);
                    })
                    .build();
        }
    }

    /**
     * Retrieves channel information for a CiMe channel slug.
     *
     * @param slug the channel slug
     * @return channel metadata
     * @throws IOException if the request fails or returns invalid data
     */
    public CiMeChannelInfo getChannel(String slug) throws IOException {
        Request request = defaultHeaders(JSON_URL + "/@" + slug + "/live")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("[CiMe] Failed to fetch channel: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[CiMe] Empty response body for channel");
            }

            String bodyString = body.string();
            if (isDebug) {
                System.out.println("[CiMe] Channel response: " + bodyString);
            }
            return gson.fromJson(bodyString, CiMeChannelInfo.class);
        }
    }

    /**
     * Retrieves current live information for a CiMe channel slug.
     *
     * @param slug the channel slug
     * @return live metadata
     * @throws IOException if the request fails or returns invalid data
     */
    public CiMeLiveInfo getLiveInfo(String slug) throws IOException {
        Request request = defaultHeaders(API_URL + "/channels/" + slug + "/live/viewer?isWatchingUhd=false")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("[CiMe] Failed to fetch live info: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[CiMe] Empty response body for live info");
            }

            String bodyString = body.string();
            if (isDebug) {
                System.out.println("[CiMe] Live info response: " + bodyString);
            }

            JsonObject json = JsonParser.parseString(bodyString).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("data");
            if (data == null) {
                throw new IOException("[CiMe] Missing data field in live info response");
            }
            return gson.fromJson(data, CiMeLiveInfo.class);
        }
    }

    /**
     * Creates a chat builder for the given channel slug.
     *
     * @param slug the channel slug
     * @return a new chat builder
     */
    public CiMeChatBuilder chat(String slug) {
        return new CiMeChatBuilder(slug);
    }

    private Request.Builder defaultHeaders(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", "https://ci.me")
                .addHeader("Referer", "https://ci.me/");
    }
}
