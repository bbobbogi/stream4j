import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.*;
import com.bbobbogi.stream4j.chzzk.chat.*;
import com.bbobbogi.stream4j.cime.*;
import com.bbobbogi.stream4j.cime.chat.CiMeChatMessage;
import com.bbobbogi.stream4j.common.CurrencyUtils;
import com.bbobbogi.stream4j.soop.*;
import com.bbobbogi.stream4j.soop.chat.SOOPDonationMessage;
import com.bbobbogi.stream4j.soop.chat.SOOPMissionEvent;
import com.bbobbogi.stream4j.youtube.*;
import com.bbobbogi.stream4j.youtube.chat.ChatItem;
import com.bbobbogi.stream4j.util.SharedHttpClient;
import com.bbobbogi.stream4j.util.RawApiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private final SOOPTestBase soopBase = new SOOPTestBase();
    private final YouTubeTestBase youtubeBase = new YouTubeTestBase();

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
    private final Map<String, YouTubeChat> youtubeConnections = new ConcurrentHashMap<>();
    private final Set<String> youtubeConnectedIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> youtubeLastActivity = new ConcurrentHashMap<>();
    private final Map<String, String> youtubeChannelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> youtubeReplaceQueue = new LinkedBlockingQueue<>();

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private static final long BLACKLIST_TTL_MS = 30 * 60 * 1000;

    private final ExecutorService chzzkReplaceWorker = newDaemonExecutor("chzzk-replace");
    private final ExecutorService cimeReplaceWorker = newDaemonExecutor("cime-replace");
    private final ExecutorService soopReplaceWorker = newDaemonExecutor("soop-replace");
    private final ExecutorService youtubeReplaceWorker = newDaemonExecutor("youtube-replace");

    private static ExecutorService newDaemonExecutor(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }
    private PrintWriter chzzkEventLog;
    private PrintWriter cimeEventLog;
    private PrintWriter soopEventLog;
    private PrintWriter youtubeEventLog;

    private boolean isBlacklisted(String id) {
        Long until = blacklist.get(id);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            blacklist.remove(id);
            return false;
        }
        return true;
    }

    private void addToBlacklist(String id) {
        blacklist.put(id, System.currentTimeMillis() + BLACKLIST_TTL_MS);
    }

    private static String now(String platform) {
        return platform + "|" + LocalDateTime.now().format(TIME_FMT);
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
                    .withChatListener(new ChzzkChatEventListener() {
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
                                    + " | " + nickname + " | " + CurrencyUtils.format(CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount())) + " | " + msg.getContent());

                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount());
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("content", msg.getContent());
                            parsed.put("anonymous", msg.isAnonymous());
                            saveEvent("Chzzk", "Donation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonationChat(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount());
                            System.out.println("[" + now("Chzzk") + "][MissionChat] " + channelName
                                    + " | " + nickname + " | status=" + msg.getMissionStatusRaw()
                                    + " | " + CurrencyUtils.format(pa) + " | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("statusEnum", String.valueOf(msg.getMissionStatus()));
                            parsed.put("success", msg.isMissionSucceed());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("missionText", msg.getMissionText());
                            saveEvent("Chzzk", "MissionChat", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonation(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount());
                            CurrencyUtils.ParsedAmount totalPa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getTotalPayAmount());
                            System.out.println("[" + now("Chzzk") + "][미션생성] " + channelName
                                    + " | " + nickname + " | " + msg.getMissionStatusRaw()
                                    + " | " + CurrencyUtils.format(pa) + " | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("success", msg.isMissionSucceed());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("totalPayAmount", totalPa.amount());
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
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount());
                            System.out.println("[" + now("Chzzk") + "][미션참여] " + channelName
                                    + " | " + nickname + " | " + CurrencyUtils.format(pa) + " | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("missionText", msg.getMissionText());
                            fetchMissionDetails(channelId, channelName, msg.getRelatedMissionDonationId(), parsed);
                            saveEvent("Chzzk", "MissionParticipation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onPartyDonationChat(PartyDonationMessage msg) {
                            String nickname = msg.isAnonymous() ? "익명" : (msg.getProfile() != null && msg.getProfile().getNickname() != null) ? msg.getProfile().getNickname() : "익명";
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CHZZK_CHEESE, msg.getPayAmount());
                            System.out.println("[" + now("Chzzk") + "][파티후원] " + channelName
                                    + " | " + nickname + " | " + CurrencyUtils.format(pa) + " | " + msg.getPartyName() + " | " + msg.getContent());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("partyName", msg.getPartyName());
                            parsed.put("content", msg.getContent());
                            parsed.put("anonymous", msg.isAnonymous());
                            saveEvent("Chzzk", "PartyDonation", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onPartyDonationInfo(PartyDonationInfo info) {
                            System.out.println("[" + now("Chzzk") + "][파티정보] " + channelName
                                    + " | " + info.getPartyName() + " | 상태=" + info.getStatusRaw()
                                    + " | 멤버=" + info.getMemberCount() + " | 총액=" + info.getTotalDonationAmount());
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("partyName", info.getPartyName());
                            parsed.put("statusRaw", info.getStatusRaw());
                            parsed.put("memberCount", info.getMemberCount());
                            parsed.put("totalDonationAmount", info.getTotalDonationAmount());
                            parsed.put("hostChannelNickname", info.getHostChannelNickname());
                            saveEvent("Chzzk", "PartyInfo", channelName, info.getRawJson(), parsed);
                        }

                        @Override
                        public void onPartyDonationFinish(PartyDonationFinishEvent event) {
                            System.out.println("[" + now("Chzzk") + "][파티종료] " + channelName
                                    + " | confirmNeeded=" + event.isConfirmNeeded());
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("confirmNeeded", event.isConfirmNeeded());
                            saveEvent("Chzzk", "PartyFinish", channelName, event.getRawJson(), parsed);
                        }

                        @Override
                        public void onPartyDonationConfirm(PartyDonationConfirmEvent event) {
                            System.out.println("[" + now("Chzzk") + "][파티확정] " + channelName
                                    + " | rank=" + event.getRank() + " | " + event.getChannelName() + " | " + event.getRankName());
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("rank", event.getRank());
                            parsed.put("channelName", event.getChannelName());
                            parsed.put("rankName", event.getRankName());
                            saveEvent("Chzzk", "PartyConfirm", channelName, event.getRawJson(), parsed);
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
                            System.out.println("[" + now("Chzzk") + "][구독선물] " + channelName
                                    + " | " + msg.getQuantity() + "개 " + msg.getGiftTierName()
                                    + " | type=" + msg.getSelectionTypeRaw());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("giftId", msg.getGiftId());
                            parsed.put("quantity", msg.getQuantity());
                            parsed.put("completedQuantity", msg.getCompletedQuantity());
                            parsed.put("giftTierName", msg.getGiftTierName());
                            parsed.put("giftTierNo", msg.getGiftTierNo());
                            parsed.put("selectionType", msg.getSelectionTypeRaw());
                            parsed.put("userIdHash", msg.getUserIdHash());
                            saveEvent("Chzzk", "SubscriptionGift", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onSubscriptionGiftReceiver(SubscriptionGiftReceiverEvent msg) {
                            System.out.println("[" + now("Chzzk") + "][구독선물수신] " + channelName
                                    + " | " + msg.getReceiverNickname() + " | " + msg.getGiftTierName());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("giftId", msg.getGiftId());
                            parsed.put("receiverNickname", msg.getReceiverNickname());
                            parsed.put("receiverUserIdHash", msg.getReceiverUserIdHash());
                            parsed.put("giftTierName", msg.getGiftTierName());
                            parsed.put("selectionType", msg.getSelectionTypeRaw());
                            saveEvent("Chzzk", "SubscriptionGiftReceiver", channelName, msg.getRawJson(), parsed);
                            chzzkLastActivity.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onBroadcastEnd(ChzzkChat c) {
                            System.out.println("[" + now("Chzzk") + "][방송종료] " + channelName);
                            chzzkConnections.remove(channelId);
                            chzzkConnectedIds.remove(channelId);
                            chzzkLastActivity.remove(channelId);
                            chzzkChannelNames.remove(channelId);
                            c.closeAsync();
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

            chat.connect();
            chzzkConnections.put(channelId, chat);
            chzzkConnectedIds.add(channelId);
            chzzkLastActivity.put(channelId, System.currentTimeMillis());
            chzzkChannelNames.put(channelId, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("Chzzk") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
            addToBlacklist(channelId);
            chzzkReplaceQueue.offer(new String[]{channelId, channelName});
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

    private void processChzzkReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] item = chzzkReplaceQueue.poll(5, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    chzzkReplaceQueue.drainTo(batch);
                    replaceChzzkChannels(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processCiMeReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] item = cimeReplaceQueue.poll(5, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    cimeReplaceQueue.drainTo(batch);
                    replaceCiMeChannels(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processSOOPReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] item = soopReplaceQueue.poll(5, TimeUnit.SECONDS);
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

    private void processYouTubeReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] item = youtubeReplaceQueue.poll(5, TimeUnit.SECONDS);
                if (item != null) {
                    List<String[]> batch = new ArrayList<>();
                    batch.add(item);
                    youtubeReplaceQueue.drainTo(batch);
                    replaceYouTubeChannels(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void replaceChzzkChannels(List<String[]> batch) {
        int need = MAX_CHANNELS - chzzkConnections.size();
        System.out.println("[" + now("Chzzk") + "] " + batch.size() + "개 종료, " + need + "개 보충 필요 (" + chzzkConnections.size() + "/" + MAX_CHANNELS + ")");
        if (need <= 0) return;

        List<String[]> liveChannels = chzzkBase.findLiveChannels(MAX_CHANNELS);
        Collections.shuffle(liveChannels);
        int connected = 0;
        for (String[] ch : liveChannels) {
            if (connected >= need) break;
            if (!chzzkConnectedIds.contains(ch[0]) && !isBlacklisted(ch[0])) {
                connectChzzkChannel(ch[0], ch[1]);
                connected++;
                try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
            }
        }
        System.out.println("[" + now("Chzzk") + "] 보충 완료: " + chzzkConnections.size() + "/" + MAX_CHANNELS + "개");
    }

    private void replaceCiMeChannels(List<String[]> batch) {
        int need = MAX_CHANNELS - cimeConnections.size();
        System.out.println("[" + now("CiMe") + "] " + batch.size() + "개 종료, " + need + "개 보충 필요 (" + cimeConnections.size() + "/" + MAX_CHANNELS + ")");
        if (need <= 0) return;

        List<String[]> liveChannels = cimeBase.findLiveChannels(MAX_CHANNELS);
        Collections.shuffle(liveChannels);
        int connected = 0;
        for (String[] ch : liveChannels) {
            if (connected >= need) break;
            if (!cimeConnectedSlugs.contains(ch[0]) && !isBlacklisted(ch[0])) {
                connectCiMeChannel(ch[0], ch[1]);
                connected++;
                try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
            }
        }
        System.out.println("[" + now("CiMe") + "] 보충 완료: " + cimeConnections.size() + "/" + MAX_CHANNELS + "개");
    }

    private void replaceSOOPChannels(List<String[]> batch) {
        int need = MAX_CHANNELS - soopConnections.size();
        System.out.println("[" + now("SOOP") + "] " + batch.size() + "개 종료, " + need + "개 보충 필요 (" + soopConnections.size() + "/" + MAX_CHANNELS + ")");
        if (need <= 0) return;

        List<String[]> liveChannels = soopBase.findLiveChannels(MAX_CHANNELS);
        Collections.shuffle(liveChannels);
        int connected = 0;
        for (String[] ch : liveChannels) {
            if (connected >= need) break;
            if (!soopConnectedIds.contains(ch[0]) && !isBlacklisted(ch[0])) {
                connectSOOPChannel(ch[0], ch[1]);
                connected++;
                try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
            }
        }
        System.out.println("[" + now("SOOP") + "] 보충 완료: " + soopConnections.size() + "/" + MAX_CHANNELS + "개");
    }

    private void replaceYouTubeChannels(List<String[]> batch) {
        int need = MAX_CHANNELS - youtubeConnections.size();
        System.out.println("[" + now("YouTube") + "] " + batch.size() + "개 종료, " + need + "개 보충 필요 (" + youtubeConnections.size() + "/" + MAX_CHANNELS + ")");
        if (need <= 0) return;

        List<String[]> liveChannels = youtubeBase.findLiveChannels(MAX_CHANNELS);
        Collections.shuffle(liveChannels);
        int connected = 0;
        for (String[] ch : liveChannels) {
            if (connected >= need) break;
            if (!youtubeConnectedIds.contains(ch[0]) && !isBlacklisted(ch[0])) {
                connectYouTubeChannel(ch[0], ch[1]);
                connected++;
                try { Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
            }
        }
        System.out.println("[" + now("YouTube") + "] 보충 완료: " + youtubeConnections.size() + "/" + MAX_CHANNELS + "개");
    }

    private void connectYouTubeChannel(String videoId, String channelName) {
        if (youtubeConnectedIds.contains(videoId)) return;
        String displayName = (channelName != null && !channelName.isEmpty()) ? channelName : videoId;
        try {
            YouTubeChat chat = new YouTubeChatBuilder(videoId)
                    .withAutoReconnect(false)
                    .withChatListener(new YouTubeChatEventListener() {
                        @Override
                        public void onConnect(YouTubeChat chat, boolean isReconnecting) {
                            System.out.println("[" + now("YouTube") + "][연결] " + displayName + " (" + videoId + ")");
                        }

                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now("YouTube") + "][에러] " + displayName + " — " + ex.getMessage());
                        }

                        @Override
                        public void onChat(ChatItem item) {
                            youtubeLastActivity.put(videoId, System.currentTimeMillis());
                        }

                        @Override
                        public void onSuperChat(ChatItem item) {
                            youtubeLastActivity.put(videoId, System.currentTimeMillis());
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.parse(item.getPurchaseAmount());
                            System.out.println("[" + now("YouTube") + "][SuperChat] " + displayName
                                    + " | " + item.getAuthorName() + " | " + CurrencyUtils.format(pa) + " | " + item.getMessage());
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", "SUPER_CHAT");
                            parsed.put("author", item.getAuthorName());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("rawAmount", pa.raw());
                            parsed.put("message", item.getMessage());
                            saveEvent("YouTube", "SuperChat", displayName, null, parsed);
                        }

                        @Override
                        public void onSuperSticker(ChatItem item) {
                            youtubeLastActivity.put(videoId, System.currentTimeMillis());
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.parse(item.getPurchaseAmount());
                            System.out.println("[" + now("YouTube") + "][SuperSticker] " + displayName
                                    + " | " + item.getAuthorName() + " | " + CurrencyUtils.format(pa));
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", "SUPER_STICKER");
                            parsed.put("author", item.getAuthorName());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
                            parsed.put("rawAmount", pa.raw());
                            saveEvent("YouTube", "SuperSticker", displayName, null, parsed);
                        }

                        @Override
                        public void onNewMember(ChatItem item) {
                            youtubeLastActivity.put(videoId, System.currentTimeMillis());
                            String memberMsg = item.getMessage() != null ? item.getMessage() : "";
                            System.out.println("[" + now("YouTube") + "][멤버십] " + displayName
                                    + " | " + item.getAuthorName() + (memberMsg.isEmpty() ? "" : " | " + memberMsg));
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", "NEW_MEMBER");
                            parsed.put("author", item.getAuthorName());
                            parsed.put("message", memberMsg);
                            saveEvent("YouTube", "NewMember", displayName, null, parsed);
                        }

                        @Override
                        public void onBroadcastEnd(YouTubeChat chat) {
                            System.out.println("[" + now("YouTube") + "][방송종료] " + displayName);
                            youtubeConnections.remove(videoId);
                            youtubeConnectedIds.remove(videoId);
                            youtubeLastActivity.remove(videoId);
                            youtubeChannelNames.remove(videoId);
                            chat.closeAsync();
                            youtubeReplaceQueue.offer(new String[]{videoId, displayName});
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryReconnect) {
                            System.out.println("[" + now("YouTube") + "][연결종료] " + displayName + " | code=" + code + " reason=" + reason);
                            if (youtubeConnections.remove(videoId) == null) return;
                            youtubeConnectedIds.remove(videoId);
                            youtubeLastActivity.remove(videoId);
                            youtubeChannelNames.remove(videoId);
                            youtubeReplaceQueue.offer(new String[]{videoId, displayName});
                        }
                    })
                    .build();
            chat.connect();
            youtubeConnections.put(videoId, chat);
            youtubeConnectedIds.add(videoId);
            youtubeLastActivity.put(videoId, System.currentTimeMillis());
            youtubeChannelNames.put(videoId, displayName);
        } catch (Exception e) {
            System.out.println("[" + now("YouTube") + "][에러] " + displayName + " 연결 실패: " + e.getMessage());
            addToBlacklist(videoId);
            youtubeReplaceQueue.offer(new String[]{videoId, displayName});
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
                                        c.close();
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

            chat.connect();
            cimeConnections.put(channelSlug, chat);
            cimeConnectedSlugs.add(channelSlug);
            cimeLastActivity.put(channelSlug, System.currentTimeMillis());
            cimeChannelNames.put(channelSlug, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("CiMe") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
            addToBlacklist(channelSlug);
            cimeReplaceQueue.offer(new String[]{channelSlug, channelName});
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
                            CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.SOOP_BALLOON, msg.getAmount());
                            System.out.println("[" + now("SOOP") + "][Donation] " + channelName
                                    + " | " + msg.getFromUsername() + " | " + CurrencyUtils.format(pa) + " | " + typeLabel);
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", msg.getType().name());
                            parsed.put("from", msg.getFrom());
                            parsed.put("fromUsername", msg.getFromUsername());
                            parsed.put("currency", pa.currencyCode());
                            parsed.put("amount", pa.amount());
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
                        public void onNewSubscribe(SOOPChat chat, String userId, String nickname, int duration) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                            System.out.println("[" + now("SOOP") + "][신규구독] " + channelName
                                    + " | " + nickname + " (" + userId + ")");
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("userId", userId);
                            parsed.put("nickname", nickname);
                            parsed.put("duration", duration);
                            saveEvent("SOOP", "SUBSCRIPTION_NEW", channelName, null, parsed);
                        }
                        @Override
                        public void onSubscriptionGift(SOOPChat chat, String gifterUserId, String gifterNickname, String recipientUserId, String recipientNickname, int months) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                            System.out.println("[" + now("SOOP") + "][구독선물] " + channelName
                                    + " | " + gifterNickname + " → " + recipientNickname + " | " + months + "개월");
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("gifterUserId", gifterUserId);
                            parsed.put("gifterNickname", gifterNickname);
                            parsed.put("recipientUserId", recipientUserId);
                            parsed.put("recipientNickname", recipientNickname);
                            parsed.put("months", months);
                            saveEvent("SOOP", "SUBSCRIPTION_GIFT", channelName, null, parsed);
                        }
                        @Override
                        public void onMission(SOOPChat chat, SOOPMissionEvent event) {
                            soopLastActivity.put(streamerId, System.currentTimeMillis());
                            System.out.println("[" + now("SOOP") + "][미션] " + channelName
                                    + " | " + event.getTypeRaw() + " | " + event.getTitle()
                                    + (event.getUserNick() != null ? " | " + event.getUserNick() : "")
                                    + (event.getGiftCount() > 0 ? " | " + event.getGiftCount() + "개" : ""));
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("type", event.getTypeRaw());
                            parsed.put("title", event.getTitle());
                            parsed.put("key", event.getKey());
                            parsed.put("userId", event.getUserId());
                            parsed.put("userNick", event.getUserNick());
                            parsed.put("giftCount", event.getGiftCount());
                            parsed.put("settleCount", event.getSettleCount());
                            parsed.put("missionStatus", event.getMissionStatus());
                            saveEvent("SOOP", "MISSION", channelName, null, parsed);
                        }
                        @Override
                        public void onBroadcastEnd(SOOPChat chat) {
                            System.out.println("[" + now("SOOP") + "][방송종료] " + channelName);
                        }
                        @Override
                        public void onUnhandledPacket(String typeCode, String[] fields) {
                            if ("0004".equals(typeCode) || "0127".equals(typeCode)) return;
                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("typeCode", typeCode);
                            parsed.put("fields", java.util.Arrays.asList(fields));
                            saveEvent("SOOP", "UNHANDLED_PACKET", channelName, null, parsed);
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
            chat.connect();
            soopConnections.put(streamerId, chat);
            soopConnectedIds.add(streamerId);
            soopLastActivity.put(streamerId, System.currentTimeMillis());
            soopChannelNames.put(streamerId, channelName);
        } catch (Exception e) {
            System.out.println("[" + now("SOOP") + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
            addToBlacklist(streamerId);
            soopReplaceQueue.offer(new String[]{streamerId, channelName});
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

                CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CIME_BEAM, amt);
                parsed.put("donationId", donationId);
                parsed.put("message", msg);
                parsed.put("currency", pa.currencyCode());
                parsed.put("amount", pa.amount());
                parsed.put("anonymous", anon);
                parsed.put("nickname", nickname);

                System.out.println("[" + now("CiMe") + "][Donation] " + channelName
                        + " | " + nickname + " | " + CurrencyUtils.format(pa) + " | " + msg);
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

                CurrencyUtils.ParsedAmount pa = CurrencyUtils.of(CurrencyUtils.CIME_BEAM, amt);
                parsed.put("donationId", donationId);
                parsed.put("message", msg);
                parsed.put("currency", pa.currencyCode());
                parsed.put("amount", pa.amount());
                parsed.put("anonymous", anon);
                parsed.put("videoId", videoId);
                parsed.put("videoType", videoType);
                parsed.put("videoTitle", videoTitle);
                parsed.put("videoStart", videoStart);
                parsed.put("videoEnd", videoEnd);

                System.out.println("[" + now("CiMe") + "][영상후원] " + channelName
                        + " | " + (anon ? "익명" : "후원자") + " | " + CurrencyUtils.format(pa) + " | " + videoTitle + " | " + msg);
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
        chzzkReplaceWorker.submit(this::processChzzkReplaceQueue);
        cimeReplaceWorker.submit(this::processCiMeReplaceQueue);
        soopReplaceWorker.submit(this::processSOOPReplaceQueue);
        youtubeReplaceWorker.submit(this::processYouTubeReplaceQueue);
    }

    private void stopMonitorThreads() {
        chzzkReplaceWorker.shutdownNow();
        cimeReplaceWorker.shutdownNow();
        soopReplaceWorker.shutdownNow();
        youtubeReplaceWorker.shutdownNow();
    }

    private void cleanupChzzk() {
         System.out.println("[" + now("Chzzk") + "] " + chzzkConnections.size() + "개 연결 해제 중...");
         for (ChzzkChat chat : chzzkConnections.values()) {
             try { chat.close(); } catch (Exception ignored) {}
         }
         if (chzzkEventLog != null) chzzkEventLog.close();
     }

     private void cleanupCiMe() {
         System.out.println("[" + now("CiMe") + "] " + cimeConnections.size() + "개 연결 해제 중...");
         for (CiMeChat chat : cimeConnections.values()) {
             try { chat.close(); } catch (Exception ignored) {}
         }
         if (cimeEventLog != null) cimeEventLog.close();
     }

     private void cleanupSOOP() {
         System.out.println("[" + now("SOOP") + "] " + soopConnections.size() + "개 연결 해제 중...");
         for (SOOPChat chat : soopConnections.values()) {
             try { chat.close(); } catch (Exception ignored) {}
         }
         if (soopEventLog != null) soopEventLog.close();
     }

     private void cleanupYouTube() {
         System.out.println("[" + now("YouTube") + "] " + youtubeConnections.size() + "개 연결 해제 중...");
         for (YouTubeChat chat : youtubeConnections.values()) {
             try { chat.close(); } catch (Exception ignored) {}
         }
         if (youtubeEventLog != null) youtubeEventLog.close();
     }

    private void initChzzk() throws Exception {
        List<String[]> liveChannels = chzzkBase.findLiveChannels(MAX_CHANNELS);
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
        List<String[]> liveChannels = soopBase.findLiveChannels(MAX_CHANNELS);
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



    private void initYouTube() throws Exception {
        String configuredIds = chzzkBase.properties.getProperty("YOUTUBE_VIDEO_IDS", "");
        List<String[]> channels;
        if (!configuredIds.isEmpty()) {
            channels = Arrays.stream(configuredIds.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .map(id -> new String[]{id, id})
                    .collect(java.util.stream.Collectors.toList());
        } else {
            channels = youtubeBase.findLiveChannels(MAX_CHANNELS);
        }

        if (channels.isEmpty()) {
            System.out.println("[" + now("YouTube") + "] 라이브 중인 채널이 없습니다.");
            return;
        }

        File logDir = new File("build/youtube-donation-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        youtubeEventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now("YouTube") + "] === YouTube 후원 모니터링 ===");
        System.out.println("[" + now("YouTube") + "] 채널 수: " + channels.size());
        System.out.println("[" + now("YouTube") + "] 로그: " + logFile.getAbsolutePath());

        for (String[] ch : channels) {
            connectYouTubeChannel(ch[0], ch[1]);
            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
        }

        System.out.println("[" + now("YouTube") + "] 초기 연결 완료: " + youtubeConnections.size() + "개 채널");
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

        Assumptions.assumeTrue(!chzzkConnections.isEmpty() || !cimeConnections.isEmpty() || !soopConnections.isEmpty() || !youtubeConnections.isEmpty(),
                "연결된 채널이 없어 테스트를 건너뛱니다.");

        System.out.println();
        System.out.println("=== 모니터링 시작 (Chzzk: " + chzzkConnections.size() + " / CiMe: " + cimeConnections.size() + " / SOOP: " + soopConnections.size() + " / YouTube: " + youtubeConnections.size() + ") ===");
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
            cleanupYouTube();
        }
    }
}
