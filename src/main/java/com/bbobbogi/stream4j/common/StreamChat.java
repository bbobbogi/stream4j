package com.bbobbogi.stream4j.common;

import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.chat.*;
import com.bbobbogi.stream4j.chzzk.types.channel.live.ChzzkLiveStatus;
import com.bbobbogi.stream4j.cime.*;
import com.bbobbogi.stream4j.soop.*;
import com.bbobbogi.stream4j.toonation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StreamChat {

    private static final long CHZZK_POLL_INTERVAL_SECONDS = 30;

    private final List<StreamChatEventListener> listeners;
    private final boolean autoReconnect;
    private final boolean debug;

    private final Chzzk chzzk;
    private final List<String> chzzkChannelIds;
    private final List<String> cimeSlugs;
    private final List<String> soopStreamerIds;
    private final List<String> toonationAlertboxKeys;

    private final Map<String, ChzzkChat> chzzkChats = new ConcurrentHashMap<>();
    private final Map<String, CiMeChat> cimeChats = new ConcurrentHashMap<>();
    private final Map<String, SOOPChat> soopChats = new ConcurrentHashMap<>();
    private final Map<String, ToonationChat> toonationChats = new ConcurrentHashMap<>();

    private ScheduledExecutorService chzzkPoller;
    private ScheduledFuture<?> chzzkPollTask;

    StreamChat(StreamChatBuilder builder) {
        this.listeners = new ArrayList<>(builder.listeners);
        this.autoReconnect = builder.autoReconnect;
        this.debug = builder.debug;
        this.chzzk = builder.chzzk;
        this.chzzkChannelIds = new ArrayList<>(builder.chzzkChannelIds);
        this.cimeSlugs = new ArrayList<>(builder.cimeSlugs);
        this.soopStreamerIds = new ArrayList<>(builder.soopStreamerIds);
        this.toonationAlertboxKeys = new ArrayList<>(builder.toonationAlertboxKeys);
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

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(this::startChzzkPoller);
    }

    public void connectAllBlocking() {
        connectAllAsync().join();
    }

    public CompletableFuture<Void> closeAllAsync() {
        return CompletableFuture.runAsync(() -> {
            stopChzzkPoller();

            for (ChzzkChat chat : chzzkChats.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }
            for (CiMeChat chat : cimeChats.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }
            for (SOOPChat chat : soopChats.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }
            for (ToonationChat chat : toonationChats.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }

            chzzkChats.clear();
            cimeChats.clear();
            soopChats.clear();
            toonationChats.clear();
        });
    }

    public void closeAllBlocking() {
        closeAllAsync().join();
    }

    public int getConnectionCount() {
        return chzzkChats.size() + cimeChats.size() + soopChats.size() + toonationChats.size();
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
                                    DonationPlatform.CHZZK, DonationType.CHAT,
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getContent(), msg.getPayAmount(), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onMissionDonation(MissionDonationMessage msg) {
                            String nickname = msg.getNickname();
                            Donation donation = new Donation(
                                    DonationPlatform.CHZZK, DonationType.MISSION,
                                    msg.getUserIdHash(), nickname != null ? nickname : "익명",
                                    msg.getMissionText() != null ? msg.getMissionText() : "",
                                    msg.getTotalPayAmount(), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            emit(l -> l.onDisconnect(DonationPlatform.CHZZK, channelId, reason));
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
                                    try { removed.closeBlocking(); } catch (Exception ignored) {}
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
                    DonationPlatform.CIME, type,
                    null, nickname, message, amount, rawJson
            );
            emit(l -> l.onDonation(donation));
        } catch (Exception ignored) {}
    }

    private void connectSoop(String streamerId) {
        try {
            SOOPChat chat = new SOOPChatBuilder(streamerId)
                    .withAutoReconnect(autoReconnect)
                    .withChatListener(new SOOPChatEventListener() {
                        @Override
                        public void onConnect(SOOPChat c) {
                            emit(l -> l.onConnect(DonationPlatform.SOOP, streamerId));
                        }

                        @Override
                        public void onChat(SOOPChat c, String userId, String username, String message) {
                            emit(l -> l.onChat(DonationPlatform.SOOP, streamerId, username, message));
                        }

                        @Override
                        public void onDonation(SOOPChat c, SOOPDonationMessage msg) {
                            Donation donation = new Donation(
                                    DonationPlatform.SOOP, DonationType.CHAT,
                                    msg.getFrom(), msg.getFromUsername() != null ? msg.getFromUsername() : "익명",
                                    "", msg.getAmount(), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onSubscribe(SOOPChat c, String from, String fromUsername, int monthCount, int tier) {
                            Donation donation = new Donation(
                                    DonationPlatform.SOOP, DonationType.SUBSCRIPTION,
                                    from, fromUsername != null ? fromUsername : "익명",
                                    monthCount + "개월 " + tier + "티어", 0, null
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(SOOPChat c, String reason, boolean tryingToReconnect) {
                            if (!tryingToReconnect) {
                                emit(l -> l.onBroadcastEnd(DonationPlatform.SOOP, streamerId));
                                soopChats.remove(streamerId);
                            }
                            emit(l -> l.onDisconnect(DonationPlatform.SOOP, streamerId, reason));
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
                                    DonationPlatform.TOONATION, type,
                                    msg.getAccount(), nickname,
                                    message, msg.getAmount(), msg
                            );
                            emit(l -> l.onDonation(donation));
                        }

                        @Override
                        public void onConnectionClosed(ToonationChat c, int code, String reason, boolean remote, boolean tryingToReconnect) {
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

    private void startChzzkPoller() {
        if (chzzkChats.isEmpty() || chzzk == null) return;

        chzzkPoller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "stream4j-chzzk-poller");
            t.setDaemon(true);
            return t;
        });

        chzzkPollTask = chzzkPoller.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, ChzzkChat> entry : chzzkChats.entrySet()) {
                String channelId = entry.getKey();
                try {
                    ChzzkLiveStatus status = chzzk.getLiveStatus(channelId);
                    if (status == null || !status.isOnline()) {
                        if (debug) System.out.println("[StreamChat] Chzzk broadcast ended: " + channelId);
                        emit(l -> l.onBroadcastEnd(DonationPlatform.CHZZK, channelId));
                        ChzzkChat chat = chzzkChats.remove(channelId);
                        if (chat != null) {
                            try { chat.closeBlocking(); } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception e) {
                    if (debug) System.out.println("[StreamChat] Chzzk poll error for " + channelId + ": " + e.getMessage());
                }
            }
        }, CHZZK_POLL_INTERVAL_SECONDS, CHZZK_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopChzzkPoller() {
        if (chzzkPollTask != null) {
            chzzkPollTask.cancel(false);
            chzzkPollTask = null;
        }
        if (chzzkPoller != null) {
            chzzkPoller.shutdownNow();
            chzzkPoller = null;
        }
    }

    private void emit(java.util.function.Consumer<StreamChatEventListener> action) {
        for (StreamChatEventListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                if (debug) e.printStackTrace();
            }
        }
    }
}
