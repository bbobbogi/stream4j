package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.soop.types.SOOPChannelInfo;
import io.github.bbobbogi.stream4j.soop.types.SOOPLiveInfo;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * SOOP (formerly AfreecaTV) platform API client.
 *
 * <p>Provides channel and live status queries and creates chat clients.
 *
 * @since 1.0.0
 */
public class SOOP {
    private static final String LIVE_DETAIL_API = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=";

    public boolean isDebug;

    private final OkHttpClient httpClient;

    SOOP(SOOPBuilder builder) {
        this.httpClient = builder.httpClient != null ? builder.httpClient : SharedHttpClient.get();
        this.isDebug = builder.isDebugEnabled();
    }

    /**
     * Retrieves live broadcast metadata for the given streamer.
     *
     * @param streamerId SOOP streamer ID
     * @return live information resolved from the SOOP live detail API
     * @throws IOException if the request fails or the response is invalid
     */
    public SOOPLiveInfo getLiveInfo(String streamerId) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("bid", streamerId)
                .add("type", "live")
                .add("pwd", "")
                .add("player_type", "html5")
                .add("stream_type", "common")
                .add("quality", "HD")
                .add("mode", "landing")
                .add("from_api", "0")
                .add("is_revive", "false")
                .build();

        Request request = new Request.Builder()
                .url(LIVE_DETAIL_API + streamerId)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("[SOOP] Failed to fetch live info: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[SOOP] Empty response body for live detail");
            }

            String bodyString = body.string();
            JsonObject root = JsonParser.parseString(bodyString).getAsJsonObject();
            JsonObject channel = root.has("CHANNEL") && root.get("CHANNEL").isJsonObject()
                    ? root.getAsJsonObject("CHANNEL")
                    : null;

            if (channel == null) {
                throw new IOException("[SOOP] Missing CHANNEL in live detail response");
            }

            String chatDomain = getAsString(channel, "CHDOMAIN");
            String chatNo = getAsString(channel, "CHATNO");
            int result = getAsInt(channel, "RESULT");
            String nickname = getAsString(channel, "BJNICK");
            String title = getAsString(channel, "TITLE");
            String broadcastNo = getAsString(channel, "BNO");
            int minTier = getAsInt(channel, "P_MIN_TIER");

            if (isDebug) {
                System.out.println("[SOOP] Live detail loaded - bno=" + broadcastNo + ", nick=" + nickname + ", title=" + title);
            }

            boolean online = chatDomain != null && chatNo != null && !chatNo.isEmpty();
            return new SOOPLiveInfo(online, result, streamerId, nickname, title, broadcastNo, minTier);
        }
    }

    /**
     * Retrieves basic channel information for the given streamer.
     *
     * @param streamerId SOOP streamer ID
     * @return channel information including display name and live state
     * @throws IOException if live information lookup fails
     */
    public SOOPChannelInfo getChannel(String streamerId) throws IOException {
        SOOPLiveInfo liveInfo = getLiveInfo(streamerId);
        return new SOOPChannelInfo(streamerId, liveInfo.getNickname(), liveInfo.isLive());
    }

    /**
     * Checks whether the given streamer is currently live.
     *
     * @param streamerId SOOP streamer ID
     * @return {@code true} if the streamer is live, otherwise {@code false}
     * @throws IOException if live information lookup fails
     */
    public boolean isLive(String streamerId) throws IOException {
        return getLiveInfo(streamerId).isOnline();
    }

    /**
     * Creates a chat builder for the given streamer.
     *
     * @param streamerId SOOP streamer ID or supported SOOP URL
     * @return a new chat builder for chat and donation connection
     */
    public SOOPChatBuilder chat(String streamerId) {
        return new SOOPChatBuilder(streamerId);
    }

    private static String getAsString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int getAsInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
