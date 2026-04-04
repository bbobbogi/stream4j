package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating {@link SOOP} API clients.
 *
 * <p>Supports optional authenticated sessions for restricted streams.
 *
 * @since 1.0.0
 */
public class SOOPBuilder extends PlatformApiBuilder<SOOP, SOOPBuilder> {
    private static final String LOGIN_API = "https://login.sooplive.co.kr/app/LoginAction.php";

    OkHttpClient httpClient;

    /**
     * Configures SOOP account credentials for authenticated API access.
     *
     * <p>Default is no authentication. Authentication is optional but required for
     * streams that enforce login, age verification, or subscription restrictions.
     *
     * @param soopId SOOP account ID
     * @param soopPassword SOOP account password
     * @return this builder for chaining
     * @throws IOException if login fails or the login response cannot be parsed
     */
    public SOOPBuilder withCredentials(String soopId, String soopPassword) throws IOException {
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
        return self();
    }

    /**
     * Builds a {@link SOOP} client with the configured options.
     *
     * @return a new SOOP API client instance
     */
    @Override
    public SOOP build() {
        return new SOOP(this);
    }

    static class InMemoryCookieJar implements CookieJar {
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
                if (c.matches(url)) {
                    matched.add(c);
                }
            }
            return matched;
        }
    }
}
