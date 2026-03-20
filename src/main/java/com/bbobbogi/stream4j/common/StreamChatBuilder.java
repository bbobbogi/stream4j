package com.bbobbogi.stream4j.common;

import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.ChzzkBuilder;
import com.bbobbogi.stream4j.chzzk.chat.ChzzkChatBuilder;
import com.bbobbogi.stream4j.cime.CiMeChatBuilder;
import com.bbobbogi.stream4j.soop.SOOPChatBuilder;
import com.bbobbogi.stream4j.toonation.ToonationChatBuilder;
import com.bbobbogi.stream4j.youtube.YouTubeChatBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    String soopId;
    String soopPassword;
    long youtubePollIntervalMs = 5000;

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

    public StreamChatBuilder addChzzk(String channelId) {
        return add(channelId, DonationPlatform.CHZZK);
    }

    public StreamChatBuilder addCiMe(String channelSlug) {
        return add(channelSlug, DonationPlatform.CIME);
    }

    public StreamChatBuilder addSoop(String streamerId) {
        return add(streamerId, DonationPlatform.SOOP);
    }

    public StreamChatBuilder addToonation(String alertboxKey) {
        return add(alertboxKey, DonationPlatform.TOONATION);
    }

    public StreamChatBuilder addYouTube(String videoId) {
        return add(videoId, DonationPlatform.YOUTUBE);
    }

    public StreamChatBuilder withChzzk(Chzzk chzzk) {
        this.chzzk = chzzk;
        return this;
    }

    public StreamChatBuilder withListener(StreamChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    public StreamChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public StreamChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    public StreamChatBuilder withSoopCredentials(String soopId, String soopPassword) {
        this.soopId = soopId;
        this.soopPassword = soopPassword;
        return this;
    }

    public StreamChatBuilder withYouTubePollInterval(long ms) {
        this.youtubePollIntervalMs = ms;
        return this;
    }

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
