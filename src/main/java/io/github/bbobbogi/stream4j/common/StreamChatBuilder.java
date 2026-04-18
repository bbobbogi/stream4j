package io.github.bbobbogi.stream4j.common;

import io.github.bbobbogi.stream4j.chzzk.Chzzk;
import io.github.bbobbogi.stream4j.chzzk.ChzzkBuilder;
import io.github.bbobbogi.stream4j.chzzk.ChzzkChatBuilder;
import io.github.bbobbogi.stream4j.cime.CiMeChatBuilder;
import io.github.bbobbogi.stream4j.soop.SOOPChatBuilder;
import io.github.bbobbogi.stream4j.toonation.ToonationChatBuilder;
import io.github.bbobbogi.stream4j.youtube.YouTubeChatBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builder for creating a {@link StreamChat} with multiple platform inputs.
 *
 * <p>The builder collects platform targets, listeners, and connection options,
 * then creates one unified client.
 *
 * @since 1.0.0
 */
public class StreamChatBuilder {

    private static final Pattern CHZZK_URL = Pattern.compile("(?:m\\.)?chzzk\\.naver\\.com");
    private static final Pattern CIME_URL = Pattern.compile("ci\\.me");
    private static final Pattern SOOP_URL = Pattern.compile("sooplive\\.co\\.kr");
    private static final Pattern YOUTUBE_URL = Pattern.compile("youtube\\.com|youtu\\.be");
    private static final Pattern TOONATION_URL = Pattern.compile("toon\\.at");

    final List<String> chzzkChannelIds = new ArrayList<>();
    final List<String> cimeSlugs = new ArrayList<>();
    final List<String> soopStreamerIds = new ArrayList<>();
    final List<String> toonationAlertboxKeys = new ArrayList<>();
    final List<String> youtubeVideoIds = new ArrayList<>();
    final List<StreamChatEventListener> listeners = new ArrayList<>();

    Chzzk chzzk;
    boolean autoReconnect = true;
    boolean debug = false;
    boolean ignoreBroadcastEnd = false;
    String soopId;
    String soopPassword;
    long youtubePollIntervalMs = 5000;

    /**
     * Adds a channel input and auto-detects the platform from the input value.
     *
     * <p>Default behavior: platform auto-detection is enabled. This is optional.
     *
     * @param input channel URL or platform identifier
     * @return this builder for chaining
     * @throws IllegalArgumentException if the platform cannot be detected
     */
    public StreamChatBuilder add(String input) {
        if (input == null || input.isBlank()) return this;
        String trimmed = input.trim();

        DonationPlatform detected = detectPlatform(trimmed);
        if (detected == null) {
            throw new IllegalArgumentException("Cannot detect platform from: " + trimmed
                    + ". Use add(input, DonationPlatform) to specify.");
        }
        return add(trimmed, detected);
    }

    /**
     * Adds a channel input for the specified platform.
     *
     * <p>Default behavior: no explicit platform is set. This method is optional
     * and used when auto-detection is not desired.
     *
     * @param input channel URL or platform identifier
     * @param platform target platform for the input
     * @return this builder for chaining
     */
    public StreamChatBuilder add(String input, DonationPlatform platform) {
        if (input == null || input.isBlank()) return this;
        String trimmed = input.trim();

        switch (platform) {
            case CHZZK -> chzzkChannelIds.add(ChzzkChatBuilder.resolveChannelId(trimmed));
            case CIME -> cimeSlugs.add(CiMeChatBuilder.resolveSlug(trimmed));
            case SOOP -> soopStreamerIds.add(SOOPChatBuilder.resolveStreamerId(trimmed));
            case TOONATION -> toonationAlertboxKeys.add(ToonationChatBuilder.resolveAlertboxKey(trimmed));
            case YOUTUBE -> {
                YouTubeChatBuilder.ResolvedInput resolved = YouTubeChatBuilder.resolveInput(trimmed);
                youtubeVideoIds.add(resolved.getId());
            }
        }
        return this;
    }

    /**
     * Adds a Chzzk channel.
     *
     * <p>Default value: no Chzzk channel is added. Optional.
     *
     * @param channelId Chzzk channel ID or resolvable Chzzk URL
     * @return this builder for chaining
     */
    public StreamChatBuilder addChzzk(String channelId) {
        return add(channelId, DonationPlatform.CHZZK);
    }

    /**
     * Adds a CiMe channel.
     *
     * <p>Default value: no CiMe channel is added. Optional.
     *
     * @param channelSlug CiMe channel slug or resolvable CiMe URL
     * @return this builder for chaining
     */
    public StreamChatBuilder addCiMe(String channelSlug) {
        return add(channelSlug, DonationPlatform.CIME);
    }

    /**
     * Adds a SOOP channel.
     *
     * <p>Default value: no SOOP channel is added. Optional.
     *
     * @param streamerId SOOP streamer ID or resolvable SOOP URL
     * @return this builder for chaining
     */
    public StreamChatBuilder addSoop(String streamerId) {
        return add(streamerId, DonationPlatform.SOOP);
    }

    /**
     * Adds a Toonation alert box source.
     *
     * <p>Default value: no Toonation source is added. Optional.
     *
     * @param alertboxKey Toonation alert box key or resolvable Toonation URL
     * @return this builder for chaining
     */
    public StreamChatBuilder addToonation(String alertboxKey) {
        return add(alertboxKey, DonationPlatform.TOONATION);
    }

    /**
     * Adds a YouTube live source.
     *
     * <p>Default value: no YouTube source is added. Optional.
     *
     * @param videoId YouTube video ID or resolvable YouTube URL
     * @return this builder for chaining
     */
    public StreamChatBuilder addYouTube(String videoId) {
        return add(videoId, DonationPlatform.YOUTUBE);
    }

    /**
     * Sets the Chzzk API client used for Chzzk connections.
     *
     * <p>Default value: if not set, a default {@link Chzzk} instance is created
     * during {@link #build()} when Chzzk channels were added. Optional.
     *
     * @param chzzk Chzzk client instance
     * @return this builder for chaining
     */
    public StreamChatBuilder withChzzk(Chzzk chzzk) {
        this.chzzk = chzzk;
        return this;
    }

    /**
     * Adds a unified event listener.
     *
     * <p>Default value: no listener is registered. Optional.
     *
     * @param listener listener to receive unified stream events
     * @return this builder for chaining
     */
    public StreamChatBuilder withListener(StreamChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Sets whether platform clients should automatically reconnect.
     *
     * <p>Default value: {@code true}. Optional.
     *
     * @param autoReconnect whether automatic reconnect is enabled
     * @return this builder for chaining
     */
    public StreamChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Enables debug logging mode for the unified client.
     *
     * <p>Default value: disabled ({@code false}). Optional.
     *
     * @return this builder for chaining
     */
    public StreamChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    public StreamChatBuilder withIgnoreBroadcastEnd(boolean ignoreBroadcastEnd) {
        this.ignoreBroadcastEnd = ignoreBroadcastEnd;
        return this;
    }

    /**
     * Sets SOOP login credentials.
     *
     * <p>Default value: credentials are not set. Optional and only needed for
     * SOOP features requiring authentication.
     *
     * @param soopId SOOP account ID
     * @param soopPassword SOOP account password
     * @return this builder for chaining
     */
    public StreamChatBuilder withSoopCredentials(String soopId, String soopPassword) {
        this.soopId = soopId;
        this.soopPassword = soopPassword;
        return this;
    }

    /**
     * Sets the polling interval for YouTube chat updates.
     *
     * <p>Default value: {@code 5000} milliseconds. Optional.
     *
     * @param ms polling interval in milliseconds
     * @return this builder for chaining
     */
    public StreamChatBuilder withYouTubePollInterval(long ms) {
        this.youtubePollIntervalMs = ms;
        return this;
    }

    /**
     * Builds a {@link StreamChat} from the configured options.
     *
     * <p>At least one channel source should be added before building. If Chzzk
     * channels were added and no Chzzk client was provided, a default client is
     * created automatically.
     *
     * @return a configured stream chat instance
     */
    public StreamChat build() {
        if (chzzk == null && !chzzkChannelIds.isEmpty()) {
            chzzk = new ChzzkBuilder().build();
        }
        return new StreamChat(this);
    }

    static DonationPlatform detectPlatform(String input) {
        if (CHZZK_URL.matcher(input).find()) return DonationPlatform.CHZZK;
        if (CIME_URL.matcher(input).find()) return DonationPlatform.CIME;
        if (SOOP_URL.matcher(input).find()) return DonationPlatform.SOOP;
        if (YOUTUBE_URL.matcher(input).find()) return DonationPlatform.YOUTUBE;
        if (TOONATION_URL.matcher(input).find()) return DonationPlatform.TOONATION;
        return null;
    }
}
