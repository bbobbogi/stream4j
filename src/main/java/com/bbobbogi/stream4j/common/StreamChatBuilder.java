package com.bbobbogi.stream4j.common;

import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.ChzzkBuilder;

import java.util.ArrayList;
import java.util.List;

public class StreamChatBuilder {

    final List<String> chzzkChannelIds = new ArrayList<>();
    final List<String> cimeSlugs = new ArrayList<>();
    final List<String> soopStreamerIds = new ArrayList<>();
    final List<String> toonationAlertboxKeys = new ArrayList<>();
    final List<String> youtubeVideoIds = new ArrayList<>();
    final List<StreamChatEventListener> listeners = new ArrayList<>();

    Chzzk chzzk;
    boolean autoReconnect = true;
    boolean debug = false;

    public StreamChatBuilder addChzzk(String channelId) {
        chzzkChannelIds.add(channelId);
        return this;
    }

    public StreamChatBuilder addCiMe(String channelSlug) {
        cimeSlugs.add(channelSlug);
        return this;
    }

    public StreamChatBuilder addSoop(String streamerId) {
        soopStreamerIds.add(streamerId);
        return this;
    }

    public StreamChatBuilder addToonation(String alertboxKey) {
        toonationAlertboxKeys.add(alertboxKey);
        return this;
    }

    public StreamChatBuilder addYouTube(String videoId) {
        youtubeVideoIds.add(videoId);
        return this;
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

    public StreamChat build() {
        if (chzzk == null && !chzzkChannelIds.isEmpty()) {
            chzzk = new ChzzkBuilder().build();
        }
        return new StreamChat(this);
    }
}
