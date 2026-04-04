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

    public SOOPChatBuilder(String streamerId) {
        this.streamerId = resolveStreamerId(streamerId);
    }

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

    public SOOPChatBuilder withChatListener(SOOPChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    public SOOPChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public SOOPChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    public SOOPChatBuilder withMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
        return this;
    }

    public SOOPChatBuilder withReconnectDelay(long reconnectDelayMs) {
        this.reconnectDelayMs = reconnectDelayMs;
        return this;
    }

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
