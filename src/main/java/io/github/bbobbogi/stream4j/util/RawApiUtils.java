package io.github.bbobbogi.stream4j.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Utility methods for raw API requests.
 *
 * @apiNote This is an internal API and may change without notice.
 * @since 1.0.0
 */
public class RawApiUtils {
    /**
     * Prevents instantiation of this utility class.
     */
    private RawApiUtils() {
    }

    /**
     * Creates a {@link Request.Builder} for an HTTP GET request.
     *
     * @param url target URL
     * @return {@link Request.Builder} instance
     */
    public static Request.Builder httpGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .get();
    }

    /**
     * Executes an HTTP request and returns the response as a {@link JsonObject}.
     *
     * @param httpClient {@link OkHttpClient} instance
     * @param request request to execute
     * @param isDebug whether debug mode is enabled
     * @return response JSON object
     * @throws IOException if the request fails
     */
    public static JsonObject getRawJson(OkHttpClient httpClient, Request request, boolean isDebug) throws IOException {
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            assert body != null;
            String bodyString = body.string();
            //\System.out.println("BD: " + bodyString);

            return JsonParser.parseString(bodyString).getAsJsonObject();
        } else {
            System.err.println(response);
            if (isDebug) {
                ResponseBody body = response.body();
                if (body != null) {
                    String bodyString = body.string();
                    System.out.println("BD: " + bodyString);
                }
            }
            throw new IOException("Response was not successful!");
        }
    }

    /**
     * Executes an HTTP request and returns the {@code content} field as a {@link JsonElement}.
     *
     * @param httpClient {@link OkHttpClient} instance
     * @param request request to execute
     * @param isDebug whether debug mode is enabled
     * @return response {@code content} JSON element
     * @throws IOException if the request fails
     */
    public static JsonElement getContentJson(OkHttpClient httpClient, Request request, boolean isDebug) throws IOException {
        return getRawJson(httpClient, request, isDebug).get("content");
    }
}
