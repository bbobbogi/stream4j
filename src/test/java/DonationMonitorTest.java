import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.chat.*;
import com.bbobbogi.stream4j.cime.*;
import com.bbobbogi.stream4j.soop.*;
import com.bbobbogi.stream4j.youtube.*;
import com.bbobbogi.stream4j.util.SharedHttpClient;
import com.bbobbogi.stream4j.util.RawApiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Tag("manual")
public class DonationMonitorTest {

    private final ChzzkTestBase chzzkBase = new ChzzkTestBase();
    private final CiMeTestBase cimeBase = new CiMeTestBase();

    private static final int CONNECT_INTERVAL_SECONDS = 5;
    private static final int MAX_CHANNELS = 50;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Map<String, ChzzkChat> chzzkConnections = new ConcurrentHashMap<>();
    private final Set<String> chzzkConnectedIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> chzzkLastActivity = new ConcurrentHashMap<>();
    private final Map<String, String> chzzkChannelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> chzzkReplaceQueue = new LinkedBlockingQueue<>();
    private final Map<String, CiMeChat> cimeConnections = new ConcurrentHashMap<>();
    private final Set<String> cimeConnectedSlugs = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> cimeLastActivity = new ConcurrentHashMap<>();
    private final Map<String, String> cimeChannelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> cimeReplaceQueue = new LinkedBlockingQueue<>();
    private final Map<String, SOOPChat> soopConnections = new ConcurrentHashMap<>();
    private final Set<String> soopConnectedIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> soopLastActivity = new ConcurrentHashMap<>();
    private final Map<String, String> soopChannelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> soopReplaceQueue = new LinkedBlockingQueue<>();

    private final ExecutorService replaceWorker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "replace-worker");
        t.setDaemon(true);
        return t;
    });
    private PrintWriter chzzkEventLog;
    private PrintWriter cimeEventLog;
    private PrintWriter soopEventLog;
    private final List<YouTubeChat> youtubeChats = new ArrayList<>();
    private PrintWriter youtubeEventLog;

    private static String now(String platform) {
        return platform + "|" + LocalDateTime.now().format(TIME_FMT);
    }

    private List<String[]> findChzzkLiveChannels() {
        List<String[]> channels = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int concurrentUserCount = 0;
        int liveId = 0;

        while (channels.size() < MAX_CHANNELS) {
            try {
                String url = Chzzk.API_URL + "/service/v1/lives?sortType=POPULAR&size=50"
                        + "&concurrentUserCount=" + concurrentUserCount
                        + (liveId > 0 ? "&liveId=" + liveId : "");

                JsonElement contentJson = RawApiUtils.getContentJson(
                        chzzkBase.chzzk.getHttpClient(),
                        RawApiUtils.httpGetRequest(url).build(),
                        chzzkBase.chzzk.isDebug);

                JsonObject content = contentJson.getAsJsonObject();
                JsonArray data = content.getAsJsonArray("data");
                if (data == null || data.isEmpty()) break;

                for (JsonElement element : data) {
                    try {
                        JsonObject live = element.getAsJsonObject();
                        JsonObject channel = live.getAsJsonObject("channel");
                        if (channel == null) continue;

                        String id = channel.get("channelId").getAsString();
                        String name = channel.get("channelName").getAsString();
                        if (seen.add(id)) {
                            channels.add(new String[]{id, name});
                        }

                        if (channels.size() >= MAX_CHANNELS) return channels;
                    } catch (Exception e) {
                        System.out.println("[" + now("Chzzk") + "][에러] 채널 파싱 스킵: " + e.getMessage());
                    }
                }

                JsonObject page = content.getAsJsonObject("page");
                if (page == null || !page.has("next") || page.get("next").isJsonNull()) break;
                JsonObject next = page.getAsJsonObject("next");
                concurrentUserCount = next.get("concurrentUserCount").getAsInt();
                liveId = next.get("liveId").getAsInt();
            } catch (Exception e) {
                System.out.println("[" + now("Chzzk") + "][에러] 채널 목록 조회 실패: " + e.getMessage());
                break;
            }
        }
        return channels;
    }

    private List<String[]> findSOOPLiveChannels() {
        List<String[]> channels = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try {
            Request request = new Request.Builder()
                    .url("https://live.sooplive.co.kr/api/main_broad_list_api.php?selectType=action&selectValue=all&orderType=view_cnt&pageNo=1&lang=ko_KR&_=" + System.currentTimeMillis())
                    .get()
                    .build();
            try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return channels;
                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray broads = json.has("broad") ? json.getAsJsonArray("broad") : null;
                if (broads == null) return channels;
                for (JsonElement el : broads) {
                    try {
                        JsonObject b = el.getAsJsonObject();
                        String userId = b.has("user_id") ? b.get("user_id").getAsString() : null;
                        String userNick = b.has("user_nick") ? b.get("user_nick").getAsString() : userId;
                        if (userId != null && seen.add(userId)) {
                            channels.add(new String[]{userId, userNick});
                        }
                        if (channels.size() >= MAX_CHANNELS) break;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("[" + now("SOOP") + "][에러] 라이브 목록 조회 실패: " + e.getMessage());
        }
        return channels;
    }

    private synchronized void saveEvent(String platform, String eventType, String channelName, String rawJson, Map<String, Object> parsed) {
        PrintWriter eventLog;
        if ("Chzzk".equals(platform)) eventLog = chzzkEventLog;
        else if ("CiMe".equals(platform)) eventLog = cimeEventLog;
        else if ("YouTube".equals(platform)) eventLog = youtubeEventLog;
        else eventLog = soopEventLog;
        if (eventLog == null) return;

        JsonObject entry = new JsonObject();
        entry.addProperty("timestamp", LocalDateTime.now().toString());
        entry.addProperty("eventType", eventType);
        entry.addProperty("channel", channelName);

        if (rawJson != null) {
            try {
                entry.add("rawJson", JsonParser.parseString(rawJson));
            } catch (Exception e) {
                entry.addProperty("rawJson", rawJson);
            }
        }

        JsonObject parsedObj = new JsonObject();
        for (Map.Entry<String, Object> e : parsed.entrySet()) {
            if (e.getValue() == null) {
                parsedObj.addProperty(e.getKey(), (String) null);
            } else if (e.getValue() instanceof Number) {
                parsedObj.addProperty(e.getKey(), (Number) e.getValue());
            } else if (e.getValue() instanceof Boolean) {
                parsedObj.addProperty(e.getKey(), (Boolean) e.getValue());
            } else {
                parsedObj.addProperty(e.getKey(), e.getValue().toString());
            }
        }
        entry.add("parsed", parsedObj);

        eventLog.println(GSON.toJson(entry));
        eventLog.flush();
    }

    private void connectChzzkChannel(String channelId, String channelName) {
        if (chzzkConnectedIds.contains(channelId)) return;

        try {
            ChzzkChat chat = chzzkBase.loginChzzk.chat(channelId)
                    .withAutoReconnect(false)
                    .withChatListener(new ChatEventListener() {
                        @Override
                        public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                            System.out.println("[" + now("Chzzk") + "][연결] " + channelName);
                            if (!isReconnecting) chat.requestRecentChat(50);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now("Chzzk") + "][에러] " + channelName + " — " + ex.getMessage());
                        }

                        @Override
                        public void onChat(ChatMessage msg) {
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onDonationChat(DonationMessage msg) {
                            String nickname = msg.isAnonymous() ? "익명" : (msg.getProfile() != null && msg.getProfile().getNickname() != null) ? msg.getProfile().getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][Donation] " + channelName
                                    + " | " + nickname + " | " + msg.getPayAmount() + "원 | " + msg.getContent());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("content", msg.getContent());
                            parsed.put("anonymous", msg.isAnonymous());
                            saveEvent("Chzzk", "Donation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonationChat(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][MissionChat] " + channelName
                                    + " | " + nickname + " | status=" + msg.getMissionStatusRaw()
                                    + " | " + msg.getPayAmount() + "원 | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("statusEnum", String.valueOf(msg.getMissionStatus()));
                            parsed.put("success", msg.isMissionSucceed());
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("missionText", msg.getMissionText());
                            saveEvent("Chzzk", "MissionChat", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonation(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][미션생성] " + channelName
                                    + " | " + nickname + " | " + msg.getMissionStatusRaw()
                                    + " | " + msg.getPayAmount() + "원 | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("success", msg.isMissionSucceed());
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("totalPayAmount", msg.getTotalPayAmount());
                            parsed.put("missionText", msg.getMissionText());
                            parsed.put("participationCount", msg.getParticipationCount());
                            parsed.put("missionDonationId", msg.getMissionDonationId());

                            fetchMissionDetails(channelId, channelName, msg.getMissionDonationId(), parsed);

                            saveEvent("Chzzk", "MissionEvent", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][미션참여] " + channelName
                                    + " | " + nickname + " | " + msg.getPayAmount() + "원 | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("missionText", msg.getMissionText());
                            fetchMissionDetails(channelId, channelName, msg.getRelatedMissionDonationId(), parsed);
                            saveEvent("Chzzk", "MissionParticipation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onPartyDonationChat(PartyDonationMessage msg) {
                            String nickname = msg.isAnonymous() ? "익명" : (msg.getProfile() != null && msg.getProfile().getNickname() != null) ? msg.getProfile().getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][파티후원] " + channelName
                                    + " | " + nickname + " | " + msg.getPayAmount() + "원 | " + msg.getPartyName() + " | " + msg.getContent());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("partyName", msg.getPartyName());
                            parsed.put("content", msg.getContent());
                            parsed.put("anonymous", msg.isAnonymous());
                            saveEvent("Chzzk", "PartyDonation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onSubscriptionChat(SubscriptionMessage msg) {
                            String nickname = (msg.getProfile() != null && msg.getProfile().getNickname() != null) ? msg.getProfile().getNickname() : "익명";
                            System.out.println("[" + now("Chzzk") + "][구독] " + channelName
                                    + " | " + nickname + " | " + msg.getSubscriptionMonth() + "개월 " + msg.getSubscriptionTierName());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("month", msg.getSubscriptionMonth());
                            parsed.put("tierName", msg.getSubscriptionTierName());
                            saveEvent("Chzzk", "Subscription", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onSubscriptionGift(SubscriptionGiftEvent msg) {
                            System.out.println("[" + now("Chzzk") + "][구독선물] " + channelName + " | " + msg);

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("raw", msg.toString());
                            saveEvent("Chzzk", "SubscriptionGift", channelName, null, parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onBroadcastEnd(ChzzkChat c) {
                            System.out.println("[" + now("Chzzk") + "][방송종료] " + channelName);
                            chzzkConnections.remove(channelId);
                            chzzkConnectedIds.remove(channelId);
                            chzzkLastActivity.remove(channelId);
                            chzzkChannelNames.remove(channelId);
                            CompletableFuture.runAsync(() -> {
                                try { c.closeBlocking(); } catch (Exception ignored) {}
                            });
                            chzzkReplaceQueue.offer(new String[]{channelId, channelName});
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            System.out.println("[" + now("Chzzk") + "][연결종료] " + channelName + " | code=" + code + " reason=" + reason);
                            if (chzzkConnections.remove(channelId) == null) return;
                            chzzkConnectedIds.remove(channelId);
                            chzzkLastActivity.remove(channelId);
                            chzzkChannelNames.remove(channelId);
                            chzzkReplaceQueue.offer(new String[]{channelId, channelName});
                        }
                    })
                    .build();

            chat.connectBlocking();
            chzzkConnections.put(channelId, chat);
            chzzkConnectedIds.add(channelId);
            chzzkLastActivity.put(channelId, System.currentTimeMillis());
            chzzkChannelNames.put(channelId, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("Chzzk") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
        }
    }

    private void fetchMissionDetails(String channelId, String channelName, String targetMissionId, Map<String, Object> parsed) {
        try {
            okhttp3.Request request = RawApiUtils.httpGetRequest(
                    Chzzk.API_URL + "/service/v2/channels/" + channelId + "/donations/missions?filterStatus=APPROVED&filterStatus=COMPLETED&filterStatus=EXPIRED&filterStatus=REJECTED&filterStatus=PENDING&page=0&size=50"
            ).build();

            try (okhttp3.Response response = chzzkBase.chzzk.getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return;

                String bodyString = response.body().string();
                JsonObject root = JsonParser.parseString(bodyString).getAsJsonObject();
                if (!root.has("content") || root.get("content").isJsonNull()) return;
                JsonObject content = root.getAsJsonObject("content");

                JsonArray data = content.getAsJsonArray("data");
                if (data == null || data.isEmpty()) return;

                for (JsonElement element : data) {
                    try {
                        JsonObject mission = element.getAsJsonObject();
                        String missionId = mission.has("missionDonationId") ? mission.get("missionDonationId").getAsString() : "";
                        if (targetMissionId != null && !targetMissionId.equals(missionId)) continue;
                        String missionText = mission.has("missionText") ? mission.get("missionText").getAsString() : "";
                        String status = mission.has("status") ? mission.get("status").getAsString() : "";
                        String missionType = mission.has("missionType") ? mission.get("missionType").getAsString() : "";
                        int totalAmount = mission.has("totalAmount") ? mission.get("totalAmount").getAsInt() : 0;
                        int participationCount = mission.has("participationCount") ? mission.get("participationCount").getAsInt() : 0;
                        boolean anonymous = mission.has("anonymous") && mission.get("anonymous").getAsBoolean();
                        String creatorNickname = "익명";
                        if (!anonymous && mission.has("user") && !mission.get("user").isJsonNull()) {
                            JsonObject user = mission.getAsJsonObject("user");
                            if (user.has("nickname") && !user.get("nickname").isJsonNull()) {
                                creatorNickname = user.get("nickname").getAsString();
                            }
                        }

                        System.out.println("[" + now("Chzzk") + "][미션상세] " + channelName
                                + " | " + missionType + " | " + status + " | " + totalAmount + "원"
                                + " | 참여" + participationCount + "명"
                                + " | 생성: " + creatorNickname
                                + " | " + missionText);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void processReplaceQueues() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 양쪽 큐 모두 대기 — 어느 쪽이든 들어오면 처리
                String[] item = chzzkReplaceQueue.poll(5, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    chzzkReplaceQueue.drainTo(batch);
                    replaceChzzkChannels(batch);
                }

                item = cimeReplaceQueue.poll(0, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    cimeReplaceQueue.drainTo(batch);
                    replaceCiMeChannels(batch);
                }

                item = soopReplaceQueue.poll(0, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    soopReplaceQueue.drainTo(batch);
                    replaceSOOPChannels(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void replaceChzzkChannels(List<String[]> batch) {
        System.out.println("[" + now("Chzzk") + "] 대체 채널 " + batch.size() + "개 탐색 중...");
        List<String[]> liveChannels = findChzzkLiveChannels();
        Collections.shuffle(liveChannels);

        for (String[] dc : batch) {
            boolean found = false;
            for (String[] ch : liveChannels) {
                if (!chzzkConnectedIds.contains(ch[0])) {
                    System.out.println("[" + now("Chzzk") + "] " + dc[1] + " \u2192 " + ch[1] + " 대체 연결");
                    connectChzzkChannel(ch[0], ch[1]);
                    try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("[" + now("Chzzk") + "] " + dc[1] + " 대체 불가 (현재 " + chzzkConnections.size() + "개 연결)");
            }
        }
    }

    private void replaceCiMeChannels(List<String[]> batch) {
        System.out.println("[" + now("CiMe") + "] 대체 채널 " + batch.size() + "개 탐색 중...");
        List<String[]> liveChannels = cimeBase.findLiveChannels(MAX_CHANNELS);
        Collections.shuffle(liveChannels);

        for (String[] dc : batch) {
            boolean found = false;
            for (String[] ch : liveChannels) {
                if (!cimeConnectedSlugs.contains(ch[0])) {
                    System.out.println("[" + now("CiMe") + "] " + dc[1] + " \u2192 " + ch[1] + " 대체 연결");
                    connectCiMeChannel(ch[0], ch[1]);
                    try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("[" + now("CiMe") + "] " + dc[1] + " 대체 불가 (현재 " + cimeConnections.size() + "개 연결)");
            }
        }
    }

    private void replaceSOOPChannels(List<String[]> batch) {
        System.out.println("[" + now("SOOP") + "] 대체 채널 " + batch.size() + "개 탐색 중...");
        List<String[]> liveChannels = findSOOPLiveChannels();
        Collections.shuffle(liveChannels);
        for (String[] dc : batch) {
            boolean found = false;
            for (String[] ch : liveChannels) {
                if (!soopConnectedIds.contains(ch[0])) {
                    System.out.println("[" + now("SOOP") + "] " + dc[1] + " → " + ch[1] + " 대체 연결");
                    connectSOOPChannel(ch[0], ch[1]);
                    try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("[" + now("SOOP") + "] " + dc[1] + " 대체 불가 (현재 " + soopConnections.size() + "개 연결)");
            }
        }
    }

    private void connectCiMeChannel(String channelSlug, String channelName) {
        if (cimeConnectedSlugs.contains(channelSlug)) return;

        try {
            CiMeChat chat = new CiMeChatBuilder(channelSlug)
                    .withAutoReconnect(false)
                    .withChatListener(new CiMeChatEventListener() {
                        @Override
                        public void onConnect(CiMeChat chat, boolean isReconnecting) {
                            System.out.println("[" + now("CiMe") + "][연결] " + channelName + " (" + channelSlug + ")");
                        }

                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now("CiMe") + "][에러] " + channelName + " — " + ex.getMessage());
                        }

                        @Override
                        public void onChat(CiMeChatMessage msg) {
                            cimeLastActivity.put(channelSlug, System.currentTimeMillis());
                            String nickname = msg.hasUser() && msg.getUser().getNickname() != null
                                    ? msg.getUser().getNickname() : "익명";
                        }

                        @Override
                        public void onEvent(String eventName, String rawJson) {
                            cimeLastActivity.put(channelSlug, System.currentTimeMillis());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("eventName", eventName);

                            if ("DONATION_CHAT".equals(eventName)) {
                                parseDonationChat(channelName, rawJson, parsed);
                                return;
                            }

                            if ("DONATION_VIDEO".equals(eventName)) {
                                parseDonationVideo(channelName, rawJson, parsed);
                                return;
                            }

                            if ("DONATION_MISSION".equals(eventName)) {
                                parseDonationMission(channelName, rawJson, parsed);
                                return;
                            }

                            if ("DONATION_MISSION_UPDATED".equals(eventName)) {
                                parseDonationMissionUpdated(channelSlug, channelName, rawJson, parsed);
                                return;
                            }

                            if ("DONATION_MISSION_REWARD_ADDED".equals(eventName)) {
                                parseDonationMissionReward(channelName, rawJson, parsed);
                                return;
                            }

                            if ("LIVE_ENDED".equals(eventName)) {
                                System.out.println("[" + now("CiMe") + "][방송종료] " + channelName + " — 대체 채널 탐색");
                                saveEvent("CiMe", eventName, channelName, rawJson, parsed);
                                CiMeChat c = cimeConnections.remove(channelSlug);
                                cimeConnectedSlugs.remove(channelSlug);
                                cimeLastActivity.remove(channelSlug);
                                cimeChannelNames.remove(channelSlug);
                                if (c != null) {
                                    try {
                                        c.closeBlocking();
                                    } catch (Exception ignored) {
                                    }
                                }
                                cimeReplaceQueue.offer(new String[]{channelSlug, channelName});
                                return;
                            }

                            if ("SUBSCRIPTION_MESSAGE".equals(eventName)) {
                                parseSubscription(channelName, rawJson, parsed);
                                return;
                            }

                            if ("SUBSCRIPTION_GIFT_MESSAGE".equals(eventName)) {
                                parseSubscriptionGift(channelName, rawJson, parsed);
                                return;
                            }

                            saveEvent("CiMe", eventName, channelName, rawJson, parsed);
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            System.out.println("[" + now("CiMe") + "][연결종료] " + channelName + " | code=" + code + " reason=" + reason);
                            if (cimeConnections.remove(channelSlug) == null) return;
                            cimeConnectedSlugs.remove(channelSlug);
                            cimeLastActivity.remove(channelSlug);
                            cimeChannelNames.remove(channelSlug);
                            cimeReplaceQueue.offer(new String[]{channelSlug, channelName});
                        }
                    })
                    .build();

            chat.connectBlocking();
            cimeConnections.put(channelSlug, chat);
            cimeConnectedSlugs.add(channelSlug);
            cimeLastActivity.put(channelSlug, System.currentTimeMillis());
            cimeChannelNames.put(channelSlug, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
        }
    }

    private void connectSOOPChannel(String streamerId, String channelName) {
        if (soopConnectedIds.contains(streamerId)) return;
        try {
            SOOPChatBuilder builder = new SOOPChatBuilder(streamerId)
                    .withAutoReconnect(false);

            String soopId = chzzkBase.properties.getProperty("SOOP_ID");
            String soopPw = chzzkBase.properties.getProperty("SOOP_PW");
            if (soopId != null && !soopId.isEmpty() && soopPw != null && !soopPw.isEmpty()) {
                builder.withCredentials(soopId, soopPw);
            }

            SOOPChat chat = builder
                    .withChatListener(new SOOPChatEventListener() {
                        @Override
                        public void onConnect(SOOPChat chat, boolean isReconnecting) {
                            System.out.println("[" + now("SOOP") + "][연결] " + channelName + " (" + streamerId + ")");
                        }
                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now("SOOP") + "][에러] " + channelName + " — " + ex.getMessage());
                        }
                        @Override
                        public void onChat(String userId, String username, String message) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                        }
                        @Override
                        public void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                            String typeLabel = switch (msg.getType()) {
                                case TEXT -> "별풍선";
                                case VIDEO -> "영상풍선";
                                case AD_BALLOON -> "AD풍선";
                            };
                            System.out.println("[" + now("SOOP") + "][Donation] " + channelName
                                    + " | " + msg.getFromUsername() + " | " + msg.getAmount() + "개 | " + typeLabel);
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", msg.getType().name());
                            parsed.put("from", msg.getFrom());
                            parsed.put("fromUsername", msg.getFromUsername());
                            parsed.put("amount", msg.getAmount());
                            parsed.put("fanClubOrdinal", msg.getFanClubOrdinal());
                            saveEvent("SOOP", "DONATION", channelName, null, parsed);
                        }
                        @Override
                        public void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                            System.out.println("[" + now("SOOP") + "][구독] " + channelName
                                    + " | " + fromUsername + " | " + monthCount + "개월 " + tier + "티어");
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("from", from);
                            parsed.put("fromUsername", fromUsername);
                            parsed.put("monthCount", monthCount);
                            parsed.put("tier", tier);
                            saveEvent("SOOP", "SUBSCRIBE", channelName, null, parsed);
                        }
                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            System.out.println("[" + now("SOOP") + "][연결종료] " + channelName + " | reason=" + reason);
                            if (soopConnections.remove(streamerId) == null) return;
                            soopConnectedIds.remove(streamerId);
                            soopLastActivity.remove(streamerId);
                            soopChannelNames.remove(streamerId);
                            soopReplaceQueue.offer(new String[]{streamerId, channelName});
                        }
                    })
                    .build();
            chat.connectBlocking();
            soopConnections.put(streamerId, chat);
            soopConnectedIds.add(streamerId);
            soopLastActivity.put(streamerId, System.currentTimeMillis());
            soopChannelNames.put(streamerId, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("SOOP") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
        }
    }

    private void parseDonationChat(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                int amt = extra.has("amt") ? extra.get("amt").getAsInt() : 0;
                boolean anon = extra.has("anon") && extra.get("anon").getAsBoolean();
                String donationId = extra.has("dId") ? extra.get("dId").getAsString() : null;
                String nickname = "익명";
                if (!anon && extra.has("prof") && !extra.get("prof").isJsonNull()) {
                    JsonObject prof = extra.getAsJsonObject("prof");
                    if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                        JsonObject ch = prof.getAsJsonObject("ch");
                        nickname = ch.has("na") ? ch.get("na").getAsString() : "익명";
                    }
                }

                parsed.put("donationId", donationId);
                parsed.put("message", msg);
                parsed.put("amount", amt);
                parsed.put("anonymous", anon);
                parsed.put("nickname", nickname);

                System.out.println("[" + now("CiMe") + "][Donation] " + channelName
                        + " | " + nickname + " | " + amt + "원 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] DONATION_CHAT: " + e.getMessage());
        }

        saveEvent("CiMe", "DONATION_CHAT", channelName, rawJson, parsed);
    }

    private void parseDonationVideo(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                int amt = extra.has("amt") ? extra.get("amt").getAsInt() : 0;
                boolean anon = extra.has("anon") && extra.get("anon").getAsBoolean();
                String donationId = extra.has("dId") ? extra.get("dId").getAsString() : null;
                String videoId = extra.has("vId") ? extra.get("vId").getAsString() : null;
                String videoType = extra.has("vType") ? extra.get("vType").getAsString() : null;
                String videoTitle = extra.has("vTitle") ? extra.get("vTitle").getAsString() : "";
                int videoStart = extra.has("vStart") ? extra.get("vStart").getAsInt() : 0;
                int videoEnd = extra.has("vEnd") ? extra.get("vEnd").getAsInt() : 0;

                parsed.put("donationId", donationId);
                parsed.put("message", msg);
                parsed.put("amount", amt);
                parsed.put("anonymous", anon);
                parsed.put("videoId", videoId);
                parsed.put("videoType", videoType);
                parsed.put("videoTitle", videoTitle);
                parsed.put("videoStart", videoStart);
                parsed.put("videoEnd", videoEnd);

                System.out.println("[" + now("CiMe") + "][영상후원] " + channelName
                        + " | " + (anon ? "익명" : "후원자") + " | " + amt + "원 | " + videoTitle + " | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] DONATION_VIDEO: " + e.getMessage());
        }

        saveEvent("CiMe", "DONATION_VIDEO", channelName, rawJson, parsed);
    }

    private void parseDonationMission(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                String missionId = extra.has("mId") ? extra.get("mId").getAsString() : null;
                String status = extra.has("st") ? extra.get("st").getAsString() : null;
                int amt = extra.has("amt") ? extra.get("amt").getAsInt() : 0;
                int timeout = extra.has("to") ? extra.get("to").getAsInt() : 0;
                boolean anon = extra.has("anon") && extra.get("anon").getAsBoolean();
                String nickname = "익명";
                if (!anon && extra.has("prof") && !extra.get("prof").isJsonNull()) {
                    JsonObject prof = extra.getAsJsonObject("prof");
                    if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                        JsonObject ch = prof.getAsJsonObject("ch");
                        nickname = ch.has("na") ? ch.get("na").getAsString() : "익명";
                    }
                }

                parsed.put("missionId", missionId);
                parsed.put("message", msg);
                parsed.put("status", status);
                parsed.put("amount", amt);
                parsed.put("timeout", timeout);
                parsed.put("anonymous", anon);
                parsed.put("nickname", nickname);

                System.out.println("[" + now("CiMe") + "][미션생성] " + channelName
                        + " | " + nickname + " | " + status + " | " + amt + "원 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] DONATION_MISSION: " + e.getMessage());
        }

        saveEvent("CiMe", "DONATION_MISSION", channelName, rawJson, parsed);
    }

    private void parseDonationMissionUpdated(String channelSlug, String channelName, String rawJson, Map<String, Object> parsed) {
        String missionId = null;
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();
                missionId = extra.has("mId") ? extra.get("mId").getAsString() : null;
                parsed.put("missionId", missionId);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] DONATION_MISSION_UPDATED: " + e.getMessage());
        }

        try {
            Request request = new Request.Builder()
                    .url(CiMeTestBase.CI_ME_API_URL + "/channels/" + channelSlug + "/active-missions")
                    .get()
                    .build();

            try (Response response = cimeBase.httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JsonObject apiJson = JsonParser.parseString(body).getAsJsonObject();
                    if (apiJson.get("code").getAsInt() == 200) {
                        JsonArray missions = apiJson.getAsJsonObject("data").getAsJsonArray("missions");
                        if (missions != null) {
                            for (JsonElement missionEl : missions) {
                                JsonObject mission = missionEl.getAsJsonObject();
                                String id = mission.get("id").getAsString();

                                if (missionId != null && !missionId.equals(id)) continue;

                                String desc = mission.has("description") ? mission.get("description").getAsString() : "";
                                String state = mission.has("state") ? mission.get("state").getAsString() : "";
                                int reward = mission.has("reward") ? mission.get("reward").getAsInt() : 0;

                                parsed.put("description", desc);
                                parsed.put("state", state);
                                parsed.put("reward", reward);

                                StringBuilder supportersStr = new StringBuilder();
                                if (mission.has("supporters") && !mission.get("supporters").isJsonNull()) {
                                    JsonArray supporters = mission.getAsJsonArray("supporters");
                                    for (JsonElement sEl : supporters) {
                                        JsonObject s = sEl.getAsJsonObject();
                                        if (supportersStr.length() > 0) supportersStr.append(", ");
                                        supportersStr.append(s.get("name").getAsString())
                                                .append("(").append(s.get("amount").getAsInt()).append("원)");
                                    }
                                }

                                System.out.println("[" + now("CiMe") + "][미션업데이트] " + channelName
                                        + " | mId=" + id + " | " + state + " | " + reward + "원 | " + desc);
                                if (supportersStr.length() > 0) {
                                    System.out.println("[" + now("CiMe") + "][미션참여자] " + channelName
                                            + " | " + supportersStr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][미션업데이트] " + channelName + " | mId=" + missionId);
        }

        saveEvent("CiMe", "DONATION_MISSION_UPDATED", channelName, rawJson, parsed);
    }

    private void parseDonationMissionReward(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                parsed.put("missionText", msg);

                if (extra.has("add") && !extra.get("add").isJsonNull()) {
                    JsonObject add = extra.getAsJsonObject("add");
                    int amt = add.has("amt") ? add.get("amt").getAsInt() : 0;
                    boolean anon = add.has("anon") && add.get("anon").getAsBoolean();
                    String nickname = "익명";
                    if (!anon && add.has("prof") && !add.get("prof").isJsonNull()) {
                        JsonObject prof = add.getAsJsonObject("prof");
                        if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                            JsonObject ch = prof.getAsJsonObject("ch");
                            nickname = ch.has("na") ? ch.get("na").getAsString() : "익명";
                        }
                    }

                    parsed.put("amount", amt);
                    parsed.put("anonymous", anon);
                    parsed.put("nickname", nickname);

                    System.out.println("[" + now("CiMe") + "][미션참여] " + channelName
                            + " | " + nickname + " | " + amt + "원 | " + msg);
                }
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] DONATION_MISSION_REWARD_ADDED: " + e.getMessage());
        }

        saveEvent("CiMe", "DONATION_MISSION_REWARD_ADDED", channelName, rawJson, parsed);
    }

    private void parseSubscription(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                String nickname = "익명";
                if (extra.has("prof") && !extra.get("prof").isJsonNull()) {
                    JsonObject prof = extra.getAsJsonObject("prof");
                    if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                        JsonObject ch = prof.getAsJsonObject("ch");
                        nickname = ch.has("na") ? ch.get("na").getAsString() : "익명";
                    }
                }
                String subName = "";
                int duration = 0;
                int tier = 0;
                if (extra.has("sub") && !extra.get("sub").isJsonNull()) {
                    JsonObject sub = extra.getAsJsonObject("sub");
                    subName = sub.has("na") ? sub.get("na").getAsString() : "";
                    duration = sub.has("du") ? sub.get("du").getAsInt() : 0;
                    tier = sub.has("ti") ? sub.get("ti").getAsInt() : 0;
                }

                parsed.put("nickname", nickname);
                parsed.put("message", msg);
                parsed.put("streamerName", subName);
                parsed.put("duration", duration);
                parsed.put("tier", tier);

                System.out.println("[" + now("CiMe") + "][구독] " + channelName
                        + " | " + nickname + " | " + duration + "개월 " + tier + "티어 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] SUBSCRIPTION_MESSAGE: " + e.getMessage());
        }

        saveEvent("CiMe", "SUBSCRIPTION_MESSAGE", channelName, rawJson, parsed);
    }

    private void parseSubscriptionGift(String channelName, String rawJson, Map<String, Object> parsed) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonObject attrs = json.getAsJsonObject("Attributes");
            if (attrs != null && attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                JsonObject extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();

                String msg = extra.has("msg") ? extra.get("msg").getAsString() : "";
                int count = extra.has("cnt") ? extra.get("cnt").getAsInt() : 0;
                String targetType = extra.has("tt") ? extra.get("tt").getAsString() : "";
                boolean anon = extra.has("anon") && extra.get("anon").getAsBoolean();
                String nickname = "익명";
                if (!anon && extra.has("prof") && !extra.get("prof").isJsonNull()) {
                    JsonObject prof = extra.getAsJsonObject("prof");
                    if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                        JsonObject ch = prof.getAsJsonObject("ch");
                        nickname = ch.has("na") ? ch.get("na").getAsString() : "익명";
                    }
                }

                parsed.put("nickname", nickname);
                parsed.put("message", msg);
                parsed.put("count", count);
                parsed.put("targetType", targetType);
                parsed.put("anonymous", anon);

                System.out.println("[" + now("CiMe") + "][선물구독] " + channelName
                        + " | " + nickname + " | " + count + "개 " + targetType + " | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][파싱에러] SUBSCRIPTION_GIFT_MESSAGE: " + e.getMessage());
        }

        saveEvent("CiMe", "SUBSCRIPTION_GIFT_MESSAGE", channelName, rawJson, parsed);
    }

    private void startMonitorThreads() {
        replaceWorker.submit(this::processReplaceQueues);
    }

    private void stopMonitorThreads() {
        replaceWorker.shutdownNow();
    }

    private void cleanupChzzk() {
        System.out.println("[" + now("Chzzk") + "] " + chzzkConnections.size() + "개 연결 해제 중...");
        for (ChzzkChat chat : chzzkConnections.values()) {
            try { chat.closeBlocking(); } catch (Exception ignored) {}
        }
        if (chzzkEventLog != null) chzzkEventLog.close();
    }

    private void cleanupCiMe() {
        System.out.println("[" + now("CiMe") + "] " + cimeConnections.size() + "개 연결 해제 중...");
        for (CiMeChat chat : cimeConnections.values()) {
            try { chat.closeBlocking(); } catch (Exception ignored) {}
        }
        if (cimeEventLog != null) cimeEventLog.close();
    }

    private void cleanupSOOP() {
        System.out.println("[" + now("SOOP") + "] " + soopConnections.size() + "개 연결 해제 중...");
        for (SOOPChat chat : soopConnections.values()) {
            try { chat.closeBlocking(); } catch (Exception ignored) {}
        }
        if (soopEventLog != null) soopEventLog.close();
    }

    private void initChzzk() throws Exception {
        List<String[]> liveChannels = findChzzkLiveChannels();
        if (liveChannels.isEmpty()) {
            System.out.println("[" + now("Chzzk") + "] 라이브 중인 채널이 없습니다.");
            return;
        }
        Collections.shuffle(liveChannels);

        File logDir = new File("build/mission-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        chzzkEventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now("Chzzk") + "] === 미션 후원 모니터링 ===");
        System.out.println("[" + now("Chzzk") + "] 채널 수: " + liveChannels.size());
        System.out.println("[" + now("Chzzk") + "] 로그: " + logFile.getAbsolutePath());

        for (String[] channel : liveChannels) {
            connectChzzkChannel(channel[0], channel[1]);
            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
        }

        System.out.println("[" + now("Chzzk") + "] 초기 연결 완료: " + chzzkConnections.size() + "개 채널");
    }

    private void initCiMe() throws Exception {
        List<String[]> liveChannels = cimeBase.findLiveChannels(MAX_CHANNELS);
        if (liveChannels.isEmpty()) {
            System.out.println("[" + now("CiMe") + "] 라이브 중인 채널이 없습니다.");
            return;
        }
        Collections.shuffle(liveChannels);

        File logDir = new File("build/cime-donation-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        cimeEventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now("CiMe") + "] === ci.me 후원 모니터링 ===");
        System.out.println("[" + now("CiMe") + "] 채널 수: " + liveChannels.size());
        System.out.println("[" + now("CiMe") + "] 로그: " + logFile.getAbsolutePath());

        for (String[] channel : liveChannels) {
            connectCiMeChannel(channel[0], channel[1]);
            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
        }

        System.out.println("[" + now("CiMe") + "] 초기 연결 완료: " + cimeConnections.size() + "개 채널");
    }

    private void initSOOP() throws Exception {
        List<String[]> liveChannels = findSOOPLiveChannels();
        if (liveChannels.isEmpty()) {
            System.out.println("[" + now("SOOP") + "] 라이브 중인 채널이 없습니다.");
            return;
        }
        Collections.shuffle(liveChannels);

        File logDir = new File("build/soop-donation-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        soopEventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now("SOOP") + "] === SOOP 후원 모니터링 ===");
        System.out.println("[" + now("SOOP") + "] 채널 수: " + liveChannels.size());
        System.out.println("[" + now("SOOP") + "] 로그: " + logFile.getAbsolutePath());

        for (String[] channel : liveChannels) {
            connectSOOPChannel(channel[0], channel[1]);
            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
        }

        System.out.println("[" + now("SOOP") + "] 초기 연결 완료: " + soopConnections.size() + "개 채널");
    }

    private static final String[] YOUTUBE_SEARCH_QUERIES = {
            "%EA%B2%8C%EC%9E%84",
            "%EB%B2%84%EC%B8%84%EC%96%BC",
            "%EB%B0%A9%EC%86%A1",
            "%EC%97%AC%EC%BA%A0",
            "%EB%82%A8%EC%BA%A0",
            "%EC%B1%84%ED%8C%85"
    };

    private List<String> findYouTubeLiveVideoIds() {
        List<String> videoIds = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String query : YOUTUBE_SEARCH_QUERIES) {
            if (videoIds.size() >= MAX_CHANNELS) break;
            try {
                Request request = new Request.Builder()
                        .url("https://www.youtube.com/results?search_query=" + query + "&sp=CAMSAkAB")
                        .header("Accept-Language", "ko")
                        .get()
                        .build();
                try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) continue;
                    String html = response.body().string();
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"videoId\":\"([^\"]{11})\"")
                            .matcher(html);
                    while (matcher.find() && videoIds.size() < MAX_CHANNELS) {
                        String vid = matcher.group(1);
                        if (seen.add(vid)) videoIds.add(vid);
                    }
                }
            } catch (Exception e) {
                System.out.println("[" + now("YouTube") + "][에러] 검색 실패 (" + query + "): " + e.getMessage());
            }
        }
        return videoIds;
    }

    private void initYouTube() throws Exception {
        String configuredIds = chzzkBase.properties.getProperty("YOUTUBE_VIDEO_IDS", "");
        List<String> videoIds;
        if (!configuredIds.isEmpty()) {
            videoIds = Arrays.stream(configuredIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
        } else {
            videoIds = findYouTubeLiveVideoIds();
        }

        if (videoIds.isEmpty()) {
            System.out.println("[" + now("YouTube") + "] 라이브 중인 채널이 없습니다.");
            return;
        }

        File logDir = new File("build/youtube-donation-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        youtubeEventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now("YouTube") + "] === YouTube 후원 모니터링 ===");
        System.out.println("[" + now("YouTube") + "] 채널 수: " + videoIds.size());
        System.out.println("[" + now("YouTube") + "] 로그: " + logFile.getAbsolutePath());

        for (String videoId : videoIds) {
            try {
                String vid = videoId;
                YouTubeChat chat = new YouTubeChatBuilder(vid)
                        .withAutoReconnect(true)
                        .withChatListener(new YouTubeChatEventListener() {
                            @Override
                            public void onConnect(YouTubeChat chat, boolean isReconnecting) {
                                System.out.println("[" + now("YouTube") + "][연결] " + vid);
                            }

                            @Override
                            public void onError(Exception ex) {}

                            @Override
                            public void onChat(ChatItem item) {}

                            @Override
                            public void onSuperChat(ChatItem item) {
                                System.out.println("[" + now("YouTube") + "][SuperChat] " + vid
                                        + " | " + item.getAuthorName() + " | " + item.getPurchaseAmount() + " | " + item.getMessage());
                                Map<String, Object> parsed = new LinkedHashMap<>();
                                parsed.put("type", "SUPER_CHAT");
                                parsed.put("author", item.getAuthorName());
                                parsed.put("amount", item.getPurchaseAmount());
                                parsed.put("message", item.getMessage());
                                saveEvent("YouTube", "SuperChat", vid, null, parsed);
                            }

                            @Override
                            public void onSuperSticker(ChatItem item) {
                                System.out.println("[" + now("YouTube") + "][SuperSticker] " + vid
                                        + " | " + item.getAuthorName() + " | " + item.getPurchaseAmount());
                                Map<String, Object> parsed = new LinkedHashMap<>();
                                parsed.put("type", "SUPER_STICKER");
                                parsed.put("author", item.getAuthorName());
                                parsed.put("amount", item.getPurchaseAmount());
                                saveEvent("YouTube", "SuperSticker", vid, null, parsed);
                            }

                            @Override
                            public void onNewMember(ChatItem item) {
                                System.out.println("[" + now("YouTube") + "][멤버십] " + vid
                                        + " | " + item.getAuthorName() + " | " + item.getMessage());
                                Map<String, Object> parsed = new LinkedHashMap<>();
                                parsed.put("type", "NEW_MEMBER");
                                parsed.put("author", item.getAuthorName());
                                parsed.put("message", item.getMessage());
                                saveEvent("YouTube", "NewMember", vid, null, parsed);
                            }

                            @Override
                            public void onBroadcastEnd(YouTubeChat chat) {
                                System.out.println("[" + now("YouTube") + "][방송종료] " + vid);
                            }
                        })
                        .build();
                chat.connectBlocking();
                youtubeChats.add(chat);
            } catch (Exception e) {
                System.out.println("[" + now("YouTube") + "][에러] " + videoId + " 연결 실패: " + e.getMessage());
            }
            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
        }

        System.out.println("[" + now("YouTube") + "] 초기 연결 완료: " + youtubeChats.size() + "개 채널");
    }

    @Test
    void testDonationMonitoring() throws Exception {
        Thread chzzkInit = new Thread(() -> {
            try { initChzzk(); } catch (Exception e) {
                if (!(e instanceof InterruptedException)) System.out.println("[Chzzk] 초기화 오류: " + e.getMessage());
            }
        }, "chzzk-init");

        Thread cimeInit = new Thread(() -> {
            try { initCiMe(); } catch (Exception e) {
                if (!(e instanceof InterruptedException)) System.out.println("[CiMe] 초기화 오류: " + e.getMessage());
            }
        }, "cime-init");

        Thread soopInit = new Thread(() -> {
            try { initSOOP(); } catch (Exception e) {
                if (!(e instanceof InterruptedException)) System.out.println("[SOOP] 초기화 오류: " + e.getMessage());
            }
        }, "soop-init");

        Thread youtubeInit = new Thread(() -> {
            try { initYouTube(); } catch (Exception e) {
                if (!(e instanceof InterruptedException)) System.out.println("[YouTube] 초기화 오류: " + e.getMessage());
            }
        }, "youtube-init");

        chzzkInit.start();
        cimeInit.start();
        soopInit.start();
        youtubeInit.start();
        chzzkInit.join();
        cimeInit.join();
        soopInit.join();
        youtubeInit.join();

        Assumptions.assumeTrue(!chzzkConnections.isEmpty() || !cimeConnections.isEmpty() || !soopConnections.isEmpty() || !youtubeChats.isEmpty(),
                "연결된 채널이 없어 테스트를 건너뛱니다.");

        System.out.println();
        System.out.println("=== 모니터링 시작 (Chzzk: " + chzzkConnections.size() + " / CiMe: " + cimeConnections.size() + " / SOOP: " + soopConnections.size() + " / YouTube: " + youtubeChats.size() + ") ===");
        System.out.println("=== 종료: Ctrl+C ===");

        startMonitorThreads();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("\n테스트 중단");
        } finally {
            stopMonitorThreads();
            cleanupChzzk();
            cleanupCiMe();
            cleanupSOOP();
            for (YouTubeChat yt : youtubeChats) {
                try { yt.closeBlocking(); } catch (Exception ignored) {}
            }
            if (youtubeEventLog != null) youtubeEventLog.close();
        }
    }
}
