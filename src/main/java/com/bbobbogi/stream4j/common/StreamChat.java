package com.bbobbogi.stream4j.common;

import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.chat.*;
import com.bbobbogi.stream4j.cime.*;
import com.bbobbogi.stream4j.soop.*;
import com.bbobbogi.stream4j.toonation.*;
import com.bbobbogi.stream4j.youtube.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class StreamChat {


    private final List<StreamChatEventListener> listeners;
    private final boolean autoReconnect;
    private final boolean debug;

    private final Chzzk chzzk;
    private final List<String> chzzkChannelIds;
    private final List<String> cimeSlugs;
    private final List<String> soopStreamerIds;
    private final List<String> toonationAlertboxKeys;
    private final List<String> youtubeVideoIds;

    private final Map<String, ChzzkChat> chzzkChats = new ConcurrentHashMap<>();
    private final Map<String, CiMeChat> cimeChats = new ConcurrentHashMap<>();
    private final Map<String, SOOPChat> soopChats = new ConcurrentHashMap<>();
    private final Map<String, ToonationChat> toonationChats = new ConcurrentHashMap<>();
    private final List<YouTubeChat> youtubeChats = new ArrayList<>();


    StreamChat(StreamChatBuilder builder) {
        this.listeners = new ArrayList<>(builder.listeners);
        this.autoReconnect = builder.autoReconnect;
        this.debug = builder.debug;
        this.chzzk = builder.chzzk;
        this.chzzkChannelIds = new ArrayList<>(builder.chzzkChannelIds);
        this.cimeSlugs = new ArrayList<>(builder.cimeSlugs);
        this.soopStreamerIds = new ArrayList<>(builder.soopStreamerIds);
        this.toonationAlertboxKeys = new ArrayList<>(builder.toonationAlertboxKeys);
        this.youtubeVideoIds = new ArrayList<>(builder.youtubeVideoIds);
    }

    public static StreamChatBuilder builder() {
        return new StreamChatBuilder();
    }

    public CompletableFuture<Void> connectAllAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String channelId : chzzkChannelIds) {
            futures.add(CompletableFuture.runAsync(() -> connectChzzk(channelId)));
        }
        for (String slug : cimeSlugs) {
            futures.add(CompletableFuture.runAsync(() -> connectCiMe(slug)));
        }
        for (String streamerId : soopStreamerIds) {
            futures.add(CompletableFuture.runAsync(() -> connectSoop(streamerId)));
        }
        for (String key : toonationAlertboxKeys) {
            futures.add(CompletableFuture.runAsync(() -> connectToonation(key)));
        }
        for (String videoId : youtubeVideoIds) {
            futures.add(CompletableFuture.runAsync(() -> connectYouTube(videoId)));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void connectAllBlocking() {
        connectAllAsync().join();
    }

    public CompletableFuture<Void> closeAllAsync() {
        List<CompletableFuture<Void>> closeFutures = new ArrayList<>();

        for (ChzzkChat chat : chzzkChats.values()) {
            closeFutures.add(CompletableFuture.runAsync(() -> {
                try { chat.closeBlocking(); } catch (Exception e) { logDebug("Error closing ChzzkChat", e); }
            }));
        }
        for (CiMeChat chat : cimeChats.values()) {
            closeFutures.add(CompletableFuture.runAsync(() -> {
                try { chat.closeBlocking(); } catch (Exception e) { logDebug("Error closing CiMeChat", e); }
            }));
        }
        for (SOOPChat chat : soopChats.values()) {
            closeFutures.add(CompletableFuture.runAsync(() -> {
                try { chat.closeBlocking(); } catch (Exception e) { logDebug("Error closing SOOPChat", e); }
            }));
        }
        for (ToonationChat chat : toonationChats.values()) {
            closeFutures.add(CompletableFuture.runAsync(() -> {
                try { chat.closeBlocking(); } catch (Exception e) { logDebug("Error closing ToonationChat", e); }
            }));
        }
        for (YouTubeChat chat : youtubeChats) {
            closeFutures.add(CompletableFuture.runAsync(() -> {
                try { chat.closeBlocking(); } catch (Exception e) { logDebug("Error closing YouTubeChat", e); }
            }));
        }

        return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    chzzkChats.clear();
                    cimeChats.clear();
                    soopChats.clear();
                    toonationChats.clear();
                    youtubeChats.clear();
                });
    }

    public void closeAllBlocking() {
        closeAllAsync().join();
    }

    public int getConnectionCount() {
        return chzzkChats.size() + cimeChats.size() + soopChats.size() + toonationChats.size() + youtubeChats.size();
    }

    private void connectChzzk(String channelId) {
        try {
            ChzzkChat chat = chzzk.chat(channelId)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new ChatEventListener() {
                        @Override
                        public void onConnect(ChzzkChat c, boolean isReconnecting) {
                            emit(l -> l.onConnect(DonationPlatform.CHZZK, channelId));
                        }

                        @Override
                        public void onChat(ChatMessage msg) {
                            String nickname = msg.getProfile() != null ? msg.getProfile().getNickname() : null;
                            emit(l -> l.onChat(DonationPlatform.CHZZK, channelId, nickname, msg.getContent()));
                        }

                        @Override
                        public void onDonationChat(DonationMessage msg) {
                            String nickname = msg.getProfile() != null ? msg.getProfile().getNickname() : null;
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.CHAT, DonationStatus.SUCCESS,
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getContent(), msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onMissionDonation(MissionDonationMessage msg) {
                            String nickname = msg.getNickname();
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.MISSION, DonationStatus.fromMissionStatus(msg.getMissionStatusRaw()),
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getMissionText() != null ? msg.getMissionText() : "",
                                    msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getTotalPayAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {
                            String nickname = msg.getNickname();
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.MISSION, DonationStatus.fromMissionStatus(msg.getMissionStatusRaw()),
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getMissionText() != null ? msg.getMissionText() : "",
                                    msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onPartyDonationChat(PartyDonationMessage msg) {
                            String nickname = msg.getProfile() != null ? msg.getProfile().getNickname() : null;
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.PARTY, DonationStatus.SUCCESS,
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getContent(), msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onPartyDonationInfo(PartyDonationInfo info) {
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.PARTY, DonationStatus.fromPartyStatus(info.getStatusRaw()),
                                    null, info.getHostChannelNickname(),
                                    info.getPartyName() != null ? info.getPartyName() : "",
                                    false, CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, info.getTotalDonationAmount()), info
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onPartyDonationFinish(PartyDonationFinishEvent event) {
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.PARTY, DonationStatus.FINISH,
                                    null, "",
                                    "", false, CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, 0), event
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onPartyDonationConfirm(PartyDonationConfirmEvent event) {
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.PARTY, DonationStatus.CONFIRM,
                                    null, event.getChannelName() != null ? event.getChannelName() : "",
                                    "rank=" + event.getRank() + " " + (event.getRankName() != null ? event.getRankName() : ""),
                                    false, CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, 0), event
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onSubscriptionChat(SubscriptionMessage msg) {
                            String nickname = msg.getProfile() != null ? msg.getProfile().getNickname() : null;
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.SUBSCRIPTION, DonationStatus.SUCCESS,
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getContent(), msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, 0), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.CHZZK, channelId, reason));
                        }

                        @Override
                        public void onBroadcastEnd(ChzzkChat c) {
                            emit(l -> l.onBroadcastEnd(DonationPlatform.CHZZK, channelId));
                            chzzkChats.remove(channelId);
                        }
                    })
                    .build();
            chat.connectBlocking();
            chzzkChats.put(channelId, chat);
        } catch (Exception e) {
            emit(l -> l.onError(DonationPlatform.CHZZK, channelId, e));
        }
    }

    private void connectCiMe(String slug) {
        try {
            CiMeChat chat = new CiMeChatBuilder(slug)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new CiMeChatEventListener() {
                        @Override
                        public void onConnect(CiMeChat c, boolean isReconnecting) {
                            emit(l -> l.onConnect(DonationPlatform.CIME, slug));
                        }

                        @Override
                        public void onChat(CiMeChatMessage msg) {
                            String nickname = msg.getUser() != null ? msg.getUser().getNickname() : null;
                            emit(l -> l.onChat(DonationPlatform.CIME, slug, nickname, msg.getContent()));
                        }

                        @Override
                        public void onEvent(String eventType, String rawJson) {
                            if ("LIVE_ENDED".equals(eventType)) {
                                emit(l -> l.onBroadcastEnd(DonationPlatform.CIME, slug));
                                CiMeChat removed = cimeChats.remove(slug);
                                if (removed != null) {
                                    try { removed.closeBlocking(); } catch (Exception e) { logDebug("Error closing CiMeChat on LIVE_ENDED", e); }
                                }
                                return;
                            }
                            if (eventType != null && eventType.startsWith("DONATION_")) {
                                parseCiMeDonation(slug, eventType, rawJson);
                            }
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.CIME, slug, reason));
                        }
                    })
                    .build();
            chat.connectBlocking();
            cimeChats.put(slug, chat);
        } catch (Exception e) {
            emit(l -> l.onError(DonationPlatform.CIME, slug, e));
        }
    }

    private void parseCiMeDonation(String slug, String eventType, String rawJson) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();
            com.google.gson.JsonObject data = json.has("data") ? json.getAsJsonObject("data") : json;

            String nickname = data.has("nickname") ? data.get("nickname").getAsString() : "익명";
            String message = data.has("message") ? data.get("message").getAsString() : "";
            int amount = data.has("amount") ? data.get("amount").getAsInt() : 0;

            DonationType type;
            switch (eventType) {
                case "DONATION_VIDEO": type = DonationType.VIDEO; break;
                case "DONATION_MISSION": type = DonationType.MISSION; break;
                default: type = DonationType.CHAT; break;
            }

            Donation donation = new Donation(
                    DonationPlatform.CIME, type, DonationStatus.SUCCESS,
                    null, nickname, message, false, CurrencyUtils.of(CurrencyUtils.CIME_BEAM, amount), rawJson
            );
            emit(l -> l.onDonation(donation));
        } catch (Exception e) {
            logDebug("Error parsing CiMe donation", e);
        }
    }

    private void connectSoop(String streamerId) {
        try {
            SOOPChat chat = new SOOPChatBuilder(streamerId)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new SOOPChatEventListener() {
                        @Override
                        public void onConnect(SOOPChat c, boolean isReconnecting) {
                            emit(l -> l.onConnect(DonationPlatform.SOOP, streamerId));
                        }

                        @Override
                        public void onChat(String userId, String username, String message) {
                            emit(l -> l.onChat(DonationPlatform.SOOP, streamerId, username, message));
                        }

                        @Override
                        public void onDonation(SOOPChat c, SOOPDonationMessage msg) {
                            Donation donation = new Donation(
                                    DonationPlatform.SOOP, DonationType.CHAT, DonationStatus.SUCCESS,
                                    msg.getFrom(), msg.getFromUsername() != null ? msg.getFromUsername() : "익명",
                                    "", false, CurrencyUtils.of(CurrencyUtils.SOOP_BALLOON, msg.getAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onSubscribe(SOOPChat c, String from, String fromUsername, int monthCount, int tier) {
                            Donation donation = new Donation(
                                    DonationPlatform.SOOP, DonationType.SUBSCRIPTION, DonationStatus.SUCCESS,
                                    from, fromUsername != null ? fromUsername : "익명",
                                    monthCount + "개월 " + tier + "티어", false, CurrencyUtils.of(CurrencyUtils.SOOP_BALLOON, 0), null
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.SOOP, streamerId, reason));
                        }

                        @Override
                        public void onBroadcastEnd(SOOPChat c) {
                            emit(l -> l.onBroadcastEnd(DonationPlatform.SOOP, streamerId));
                            soopChats.remove(streamerId);
                        }

                        @Override
                        public void onError(Exception ex) {
                            emit(l -> l.onError(DonationPlatform.SOOP, streamerId, ex));
                        }
                    })
                    .build();
            chat.connectBlocking();
            soopChats.put(streamerId, chat);
        } catch (Exception e) {
            emit(l -> l.onError(DonationPlatform.SOOP, streamerId, e));
        }
    }

    private void connectToonation(String key) {
        try {
            ToonationChat chat = new ToonationChatBuilder(key)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new ToonationChatEventListener() {
                        @Override
                        public void onConnect(ToonationChat c, boolean isReconnecting) {
                            emit(l -> l.onConnect(DonationPlatform.TOONATION, key));
                        }

                        @Override
                        public void onDonation(ToonationChat c, ToonationDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            String message = msg.getMessage() != null ? msg.getMessage() : "";
                            DonationType type = msg.isVideoDonation() ? DonationType.VIDEO : DonationType.CHAT;
                            Donation donation = new Donation(
                                    DonationPlatform.TOONATION, type, DonationStatus.SUCCESS,
                                    msg.getAccount(), nickname,
                                    message, msg.isAnonymous(), CurrencyUtils.of(CurrencyUtils.TOONATION_WON, msg.getAmount()), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.TOONATION, key, reason));
                        }

                        @Override
                        public void onError(Exception ex) {
                            emit(l -> l.onError(DonationPlatform.TOONATION, key, ex));
                        }
                    })
                    .build();
            chat.connectBlocking();
            toonationChats.put(key, chat);
        } catch (Exception e) {
            emit(l -> l.onError(DonationPlatform.TOONATION, key, e));
        }
    }


    private void connectYouTube(String videoId) {
        try {
            YouTubeChat chat = new YouTubeChatBuilder(videoId)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new YouTubeChatEventListener() {
                        @Override
                        public void onConnect(YouTubeChat c, boolean isReconnecting) {
                            emit(l -> l.onConnect(DonationPlatform.YOUTUBE, videoId));
                        }

                        @Override
                        public void onChat(ChatItem item) {
                            emit(l -> l.onChat(DonationPlatform.YOUTUBE, videoId, item.getAuthorName(), item.getMessage()));
                        }

                        @Override
                        public void onSuperChat(ChatItem item) {
                            Donation donation = new Donation(
                                    DonationPlatform.YOUTUBE, DonationType.CHAT, DonationStatus.SUCCESS,
                                    item.getAuthorChannelID(), item.getAuthorName() != null ? item.getAuthorName() : "익명",
                                    item.getMessage() != null ? item.getMessage() : "",
                                    false, CurrencyUtils.parse(item.getPurchaseAmount()), item
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onSuperSticker(ChatItem item) {
                            Donation donation = new Donation(
                                    DonationPlatform.YOUTUBE, DonationType.CHAT, DonationStatus.SUCCESS,
                                    item.getAuthorChannelID(), item.getAuthorName() != null ? item.getAuthorName() : "익명",
                                    "[스티커]", false, CurrencyUtils.parse(item.getPurchaseAmount()), item
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onNewMember(ChatItem item) {
                            Donation donation = new Donation(
                                    DonationPlatform.YOUTUBE, DonationType.SUBSCRIPTION, DonationStatus.SUCCESS,
                                    item.getAuthorChannelID(), item.getAuthorName() != null ? item.getAuthorName() : "익명",
                                    item.getMessage() != null ? item.getMessage() : "",
                                    false, CurrencyUtils.of("KRW", 0), item
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onBroadcastEnd(YouTubeChat c) {
                            emit(l -> l.onBroadcastEnd(DonationPlatform.YOUTUBE, videoId));
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.YOUTUBE, videoId, reason));
                        }

                        @Override
                        public void onError(Exception ex) {
                            emit(l -> l.onError(DonationPlatform.YOUTUBE, videoId, ex));
                        }
                    })
                    .build();
            chat.connectBlocking();
            youtubeChats.add(chat);
        } catch (Exception e) {
            emit(l -> l.onError(DonationPlatform.YOUTUBE, videoId, e));
        }
    }

    private void emit(java.util.function.Consumer<StreamChatEventListener> action) {
        for (StreamChatEventListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                logDebug("Error in listener callback", e);
            }
        }
    }

    private void logDebug(String message, Exception e) {
        if (debug) {
            System.err.println("[StreamChat] " + message + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
