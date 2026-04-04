package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for creating {@link SOOPChat} instances.
 *
 * <p>Accepts a raw streamer ID or supported SOOP URL as input.
 *
 * @since 1.0.0
 */
public class SOOPChatBuilder {

    private static final String LOGIN_API = "https://login.sooplive.co.kr/app/LoginAction.php";
    private static final Pattern SOOP_PLAY_URL_PATTERN = Pattern.compile("^(?:https?://)?play\\.sooplive\\.co\\.kr/([^/?#]+)(?:/.*)?$");
    private static final Pattern SOOP_STATION_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.)?sooplive\\.co\\.kr/station/([^/?#]+)(?:/.*)?$");

    private final String streamerId;
    private final ArrayList<SOOPChatEventListener> listeners = new ArrayList<>();
    private boolean autoReconnect = true;
    private boolean debug = false;
    private int maxReconnectAttempts = 5;
    private long reconnectDelayMs = 5000;
    private OkHttpClient httpClient;

    /**
     * Creates a builder for the target streamer.
     *
     * @param streamerId SOOP streamer ID or supported SOOP URL
     */
    public SOOPChatBuilder(String streamerId) {
        this.streamerId = resolveStreamerId(streamerId);
    }

    /**
     * Resolves a SOOP streamer ID from an ID or SOOP URL.
     *
     * @param input raw streamer ID or SOOP URL
     * @return normalized streamer ID, or {@code null} if input is {@code null}
     */
    public static String resolveStreamerId(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        Matcher playMatcher = SOOP_PLAY_URL_PATTERN.matcher(trimmed);
        if (playMatcher.matches()) {
            return playMatcher.group(1);
        }

        Matcher stationMatcher = SOOP_STATION_URL_PATTERN.matcher(trimmed);
        if (stationMatcher.matches()) {
            return stationMatcher.group(1);
        }

        return trimmed;
    }

    /**
     * Registers a chat event listener.
     *
     * <p>Default is no listener.
     *
     * @param listener listener to receive chat events
     * @return this builder for chaining
     */
    public SOOPChatBuilder withChatListener(SOOPChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Configures automatic reconnection after disconnection.
     *
     * <p>Default is {@code true}.
     *
     * @param autoReconnect whether automatic reconnect is enabled
     * @return this builder for chaining
     */
    public SOOPChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Enables debug logging output.
     *
     * <p>Default is disabled.
     *
     * @return this builder for chaining
     */
    public SOOPChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * Configures the maximum reconnect attempt count.
     *
     * <p>Default is {@code 5}.
     *
     * @param maxReconnectAttempts maximum number of reconnect attempts
     * @return this builder for chaining
     */
    public SOOPChatBuilder withMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
        return this;
    }

    /**
     * Configures the base reconnect delay in milliseconds.
     *
     * <p>Default is {@code 5000} ms.
     *
     * @param reconnectDelayMs reconnect delay in milliseconds
     * @return this builder for chaining
     */
    public SOOPChatBuilder withReconnectDelay(long reconnectDelayMs) {
        this.reconnectDelayMs = reconnectDelayMs;
        return this;
    }

    /**
     * Configures SOOP login credentials for authenticated chat access.
     *
     * <p>Default is no authentication. Authentication is optional but required for
     * some restricted streams.
     *
     * @param soopId SOOP account ID
     * @param soopPassword SOOP account password
     * @return this builder for chaining
     * @throws IOException if login fails or the login response cannot be parsed
     */
    public SOOPChatBuilder withCredentials(String soopId, String soopPassword) throws IOException {
        CookieJar cookieJar = new InMemoryCookieJar();
        OkHttpClient loginClient = SharedHttpClient.newBuilder()
                .cookieJar(cookieJar)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("szWork", "login")
                .add("szType", "json")
                .add("szUid", soopId)
                .add("szPassword", soopPassword)
                .add("isSaveId", "false")
                .add("szScriptVar", "oLoginRet")
                .add("szAction", "")
                .add("isLoginRetain", "N")
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_API)
                .post(formBody)
                .build();

        try (Response response = loginClient.newCall(request).execute()) {
            if (response.body() != null) {
                String body = response.body().string();
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    int result = json.has("RESULT") ? json.get("RESULT").getAsInt() : -1;
                    if (result != 1) {
                        throw new IOException("[SOOP] Login failed: RESULT=" + result);
                    }
                } catch (com.google.gson.JsonSyntaxException e) {
                    throw new IOException("[SOOP] Login response parse error: " + body);
                }
            }
        }

        this.httpClient = loginClient;
        return this;
    }

    /**
     * Builds a {@link SOOPChat} instance with current configuration.
     *
     * @return configured SOOP chat client
     */
    public SOOPChat build() {
        SOOPChat chat = new SOOPChat(streamerId, autoReconnect, debug, maxReconnectAttempts, reconnectDelayMs, httpClient);
        for (SOOPChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }
        return chat;
    }

    private static class InMemoryCookieJar implements CookieJar {
        private final List<Cookie> store = new ArrayList<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            store.removeIf(c -> cookies.stream().anyMatch(nc -> nc.name().equals(c.name()) && nc.domain().equals(c.domain())));
            store.addAll(cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> matched = new ArrayList<>();
            for (Cookie c : store) {
                if (c.matches(url)) matched.add(c);
            }
            return matched;
        }
    }
}
