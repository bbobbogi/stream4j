package io.github.bbobbogi.stream4j.chzzk;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import org.jetbrains.annotations.NotNull;
import io.github.bbobbogi.stream4j.chzzk.exception.ChannelNotExistsException;
import io.github.bbobbogi.stream4j.chzzk.exception.NotExistsException;
import io.github.bbobbogi.stream4j.chzzk.exception.NotLoggedInException;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkFollowingStatusResponse;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkUser;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkChannelInfo;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkChannelEmotePackData;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkChannelFollowingData;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkChannelRules;
import io.github.bbobbogi.stream4j.chzzk.types.*;
import io.github.bbobbogi.stream4j.chzzk.types.ChzzkRecommendationChannels;
import io.github.bbobbogi.stream4j.util.RawApiUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Chzzk (CHZZK) API client class.
 *
 * @since 1.0.0
 */
public class Chzzk {
    /**
     * CHZZK API URL
     */
    public static String API_URL = "https://api.chzzk.naver.com";

    /**
     * NAVER game API URL.
     */
    public static String GAME_API_URL = "https://comm-api.game.naver.com/nng_main";

    /**
     * Whether debug mode is enabled.
     */
    public boolean isDebug = false;

    private String nidAuth;
    private String nidSession;
    private boolean isAnonymous;

    private OkHttpClient httpClient;
    private Gson gson;

    Chzzk(ChzzkBuilder chzzkBuilder) {
        this.nidAuth = chzzkBuilder.nidAuth;
        this.nidSession = chzzkBuilder.nidSession;
        this.isAnonymous = chzzkBuilder.isAnonymous;
        this.gson = new Gson();

        if (chzzkBuilder.isAnonymous) {
            httpClient = SharedHttpClient.get();
        } else {
            httpClient = SharedHttpClient.newBuilder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request authorized = original.newBuilder()
                                .addHeader("Cookie",
                                        "NID_AUT=" + chzzkBuilder.nidAuth + "; " +
                                        "NID_SES=" + chzzkBuilder.nidSession)
                                .build();
                        return chain.proceed(authorized);
                    })
                    .build();
        }
    }

    /**
     * Get this {@link Chzzk} logged in.
     *
     * @return {@code true} if logged in, otherwise {@code false}
     */
    public boolean isLoggedIn() {
        return !isAnonymous;
    }

    /**
     * Get new an instance of {@link ChzzkChat} with this {@link Chzzk}.
     *
     * @param channelId channel ID
     * @return a {@link ChzzkChatBuilder} instance
     */
    public ChzzkChatBuilder chat(String channelId) {
        return new ChzzkChatBuilder(this, channelId);
    }

    /**
     * Returns the HTTP client.
     *
     * @return an {@link OkHttpClient} instance
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void close() {
        // 공유 Dispatcher/ConnectionPool은 종료하지 않음 (SharedHttpClient 관리)
        // 파생 클라이언트의 캐시만 정리
        if (httpClient != null && httpClient != SharedHttpClient.get()) {
            httpClient.connectionPool().evictAll();
            try {
                if (httpClient.cache() != null) {
                    httpClient.cache().close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Get {@link ChzzkChannelInfo} by the channel id.
     *
     * @param channelId ID of {@link ChzzkChannelInfo} that to get.
     * @return {@link ChzzkChannelInfo} to get
     * @throws IOException if the request to API failed
     * @throws ChannelNotExistsException if the channel doesn't exists
     */
    public ChzzkChannelInfo getChannel(String channelId) throws IOException, ChannelNotExistsException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v1/channels/" + channelId).build(),
                isDebug);

        ChzzkChannelInfo channel = gson.fromJson(
                contentJson,
                ChzzkChannelInfo.class);
        if (channel.getChannelId() == null) {
            throw new ChannelNotExistsException("The channel does not exists!");
        }

        return channel;
    }

    /**
     * Get {@link ChzzkLiveInfo} by the channel id.
     * @param channelId ID of {@link ChzzkChannelInfo}
     * @return {@link ChzzkLiveInfo} of the channel
     * @throws IOException if the request to API failed
     */
    public @NotNull ChzzkLiveInfo getLiveStatus(@NotNull String channelId) throws IOException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/polling/v2/channels/" + channelId + "/live-status").build(),
                isDebug);

        return gson.fromJson(contentJson, ChzzkLiveInfo.class);
    }

    /**
     * Get {@link ChzzkLiveDetail} by the channel id.
     * @param channelId ID of {@link ChzzkChannelInfo}
     * @return {@link ChzzkLiveDetail} of the channel
     * @throws IOException if the request to API failed
     */
    public @NotNull ChzzkLiveDetail getLiveDetail(@NotNull String channelId) throws IOException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v2/channels/" + channelId + "/live-detail").build(),
                isDebug);

        return gson.fromJson(contentJson, ChzzkLiveDetail.class);
    }

    /**
     * Get channel's {@link ChzzkChannelRules} by the channel id.
     *
     * @param channelId ID of {@link ChzzkChannelInfo}
     * @return {@link ChzzkChannelRules} of the channel
     * @throws IOException        if the request to API failed
     * @throws NotExistsException if the channel doesn't exists or the rules of the channel doesn't available
     */
    public ChzzkChannelRules getChannelChatRules(String channelId) throws IOException, NotExistsException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v1/channels/" + channelId + "/chat-rules").build(),
                isDebug);

        ChzzkChannelRules rules = gson.fromJson(
                contentJson,
                ChzzkChannelRules.class);

        if (rules.getUpdatedDate() == null) {
            throw new NotExistsException("The channel or rules of the channel does not exists!");
        }

        return rules;
    }

    /**
     * Gets emote pack data for the channel.
     *
     * @param channelId channel ID
     * @return {@link ChzzkChannelEmotePackData} emote pack data
     * @throws IOException if the API request fails
     */
    public ChzzkChannelEmotePackData getChannelEmotePackData(String channelId) throws IOException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v1/channels/" + channelId + "/emoji-packs").build(),
                isDebug);
        ChzzkChannelEmotePackData emoticons = null;
        List<JsonElement> emoteElements =  contentJson.getAsJsonObject().asMap().get("subscriptionEmojiPacks").getAsJsonArray().asList();
        for (JsonElement emoteElement : emoteElements) {
            if (emoteElement.getAsJsonObject().asMap().get("emojiPackId").getAsString().equals("\""+channelId+ "\"")) {
                continue;
            }
            emoticons= gson.fromJson(
                    emoteElement,
                    ChzzkChannelEmotePackData.class);
        }
        return emoticons;
    }

    /**
     * Get following status about channel.
     *
     * @param channelId ID of {@link ChzzkChannelInfo} to get following status
     * @return user's {@link ChzzkChannelFollowingData} of the channel
     * @throws IOException if the request to API failed
     * @throws NotLoggedInException if this {@link Chzzk} didn't log in
     * @throws ChannelNotExistsException if the channel doesn't exists
     */
    public ChzzkChannelFollowingData getFollowingStatus(String channelId) throws IOException, NotLoggedInException, ChannelNotExistsException {
        if (isAnonymous) {
            throw new NotLoggedInException("Can't get following status without logging in!");
        }

        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v1/channels/" + channelId + "/follow").build(),
                isDebug);

        ChzzkFollowingStatusResponse followingDataResponse = gson.fromJson(
                contentJson,
                ChzzkFollowingStatusResponse.class);

        if (followingDataResponse.channel.getChannelId() == null) {
            throw new NotExistsException("The channel does not exists!");
        }

        return followingDataResponse.channel.getPersonalData().getFollowing();
    }

    /**
     * Get {@link ChzzkRecommendationChannels}
     *
     * @return recommendation channels - {@link ChzzkRecommendationChannels}
     * @throws IOException if the request to API failed
     */
    public ChzzkRecommendationChannels getRecommendationChannels() throws IOException {
        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(API_URL + "/service/v1/home/recommendation-channels").build(),
                isDebug);

        ChzzkRecommendationChannels channels = gson.fromJson(
                contentJson,
                ChzzkRecommendationChannels.class);

        return channels;
    }

    /**
     * Get {@link ChzzkUser} that the {@link Chzzk} logged in.
     *
     * @return {@link ChzzkUser} that current logged in
     * @throws IOException if the request to API failed
     * @throws NotLoggedInException if this {@link Chzzk} didn't log in
     */
    public ChzzkUser getLoggedUser() throws IOException, NotLoggedInException {
        if (isAnonymous) {
            throw new NotLoggedInException("Can't get information of logged user without logging in!");
        }

        JsonElement contentJson = RawApiUtils.getContentJson(
                httpClient,
                RawApiUtils.httpGetRequest(GAME_API_URL + "/v1/user/getUserStatus").build(),
                isDebug);

        ChzzkUser user = gson.fromJson(
                contentJson,
                ChzzkUser.class);

        return user;
    }
}
