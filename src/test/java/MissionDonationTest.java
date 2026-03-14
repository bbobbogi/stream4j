import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.chat.*;
import xyz.r2turntrue.chzzk4j.util.RawApiUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class MissionDonationTest extends ChzzkTestBase {

    private static final int CONNECT_INTERVAL_SECONDS = 5;
    private static final int MAX_CHANNELS = 50;
    private static final long IDLE_TIMEOUT_MS = 10 * 60 * 1000L; // 10분
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Map<String, ChzzkChat> activeConnections = new ConcurrentHashMap<>();
    private final Set<String> connectedChannelIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final Map<String, String> channelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> replaceQueue = new LinkedBlockingQueue<>();
    private final ExecutorService replaceWorker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "chzzk-replace-worker");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService idleChecker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "idle-checker");
        t.setDaemon(true);
        return t;
    });
    private PrintWriter eventLog;

    private static String now() {
        return "Chzzk|" + LocalDateTime.now().format(TIME_FMT);
    }

    private List<String[]> findLiveChannels() {
        List<String[]> channels = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int concurrentUserCount = 0;
        int liveId = 0;

        // 페이지네이션으로 MAX_CHANNELS까지 수집
        while (channels.size() < MAX_CHANNELS) {
            try {
                String url = Chzzk.API_URL + "/service/v1/lives?sortType=POPULAR&size=50"
                        + "&concurrentUserCount=" + concurrentUserCount
                        + (liveId > 0 ? "&liveId=" + liveId : "");

                JsonElement contentJson = RawApiUtils.getContentJson(
                        chzzk.getHttpClient(),
                        RawApiUtils.httpGetRequest(url).build(),
                        chzzk.isDebug);

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
                        System.out.println("[" + now() + "][에러] 채널 파싱 스킵: " + e.getMessage());
                    }
                }

                // 다음 페이지 커서
                JsonObject page = content.getAsJsonObject("page");
                if (page == null || !page.has("next") || page.get("next").isJsonNull()) break;
                JsonObject next = page.getAsJsonObject("next");
                concurrentUserCount = next.get("concurrentUserCount").getAsInt();
                liveId = next.get("liveId").getAsInt();
            } catch (Exception e) {
                System.out.println("[" + now() + "][에러] 채널 목록 조회 실패: " + e.getMessage());
                break;
            }
        }
        return channels;
    }

    private synchronized void saveEvent(String eventType, String channelName, ChatMessage msg, Map<String, Object> parsed) {
        if (eventLog == null) return;

        JsonObject entry = new JsonObject();
        entry.addProperty("timestamp", LocalDateTime.now().toString());
        entry.addProperty("eventType", eventType);
        entry.addProperty("channel", channelName);

        if (msg.getRawJson() != null) {
            try {
                entry.add("rawJson", JsonParser.parseString(msg.getRawJson()));
            } catch (Exception e) {
                entry.addProperty("rawJson", msg.getRawJson());
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

    private void connectChannel(String channelId, String channelName) {
        if (connectedChannelIds.contains(channelId)) return;

        try {
            ChzzkChat chat = loginChzzk.chat(channelId)
                    .withAutoReconnect(false)
                    .withChatListener(new ChatEventListener() {
                        @Override
                        public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                            System.out.println("[" + now() + "][연결] " + channelName);
                            if (!isReconnecting) chat.requestRecentChat(50);
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now() + "][에러] " + channelName + " — " + ex.getMessage());
                        }


                        @Override
                        public void onChat(ChatMessage msg) {
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onDonationChat(DonationMessage msg) {
                            String nickname = msg.isAnonymous() ? "익명" : (msg.getProfile() != null && msg.getProfile().getNickname() != null) ? msg.getProfile().getNickname() : "익명";
                            System.out.println("[" + now() + "][Donation] " + channelName
                                    + " | " + nickname + " | " + msg.getPayAmount() + "원 | " + msg.getContent());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("content", msg.getContent());
                            parsed.put("anonymous", msg.isAnonymous());
                            saveEvent("Donation", channelName, msg, parsed);
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonationChat(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now() + "][MissionChat] " + channelName
                                    + " | " + nickname + " | status=" + msg.getMissionStatusRaw()
                                    + " | " + msg.getPayAmount() + "원 | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("statusEnum", String.valueOf(msg.getMissionStatus()));
                            parsed.put("success", msg.isMissionSucceed());
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("missionText", msg.getMissionText());
                            saveEvent("MissionChat", channelName, msg, parsed);
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonation(MissionDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now() + "][미션생성] " + channelName
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

                            saveEvent("MissionEvent", channelName, msg, parsed);
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {
                            String nickname = msg.getNickname() != null ? msg.getNickname() : "익명";
                            System.out.println("[" + now() + "][미션참여] " + channelName
                                    + " | " + nickname + " | " + msg.getPayAmount() + "원 | " + msg.getMissionText());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("nickname", nickname);
                            parsed.put("statusRaw", msg.getMissionStatusRaw());
                            parsed.put("amount", msg.getPayAmount());
                            parsed.put("missionText", msg.getMissionText());
                            fetchMissionDetails(channelId, channelName, msg.getRelatedMissionDonationId(), parsed);
                            saveEvent("MissionParticipation", channelName, msg, parsed);
                            lastActivityTime.put(channelId, System.currentTimeMillis());
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            System.out.println("[" + now() + "][연결종료] " + channelName + " | code=" + code + " reason=" + reason);
                            if (activeConnections.remove(channelId) == null) return;
                            connectedChannelIds.remove(channelId);
                            lastActivityTime.remove(channelId);
                            channelNames.remove(channelId);
                            replaceQueue.offer(new String[]{channelId, channelName});
                        }
                    })
                    .build();

            chat.connectBlocking();
            activeConnections.put(channelId, chat);
            connectedChannelIds.add(channelId);
            lastActivityTime.put(channelId, System.currentTimeMillis());
            channelNames.put(channelId, channelName);
        } catch (Exception e) {
            System.out.println("[" + now() + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
        }
    }

    private void fetchMissionDetails(String channelId, String channelName, String targetMissionId, Map<String, Object> parsed) {
        try {
            okhttp3.Request request = RawApiUtils.httpGetRequest(
                    Chzzk.API_URL + "/service/v2/channels/" + channelId + "/donations/missions?filterStatus=APPROVED&filterStatus=COMPLETED&filterStatus=EXPIRED&filterStatus=REJECTED&filterStatus=PENDING&page=0&size=50"
            ).build();

            try (okhttp3.Response response = chzzk.getHttpClient().newCall(request).execute()) {
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

                        System.out.println("[" + now() + "][미션상세] " + channelName
                                + " | " + missionType + " | " + status + " | " + totalAmount + "원"
                                + " | 참여" + participationCount + "명"
                                + " | 생성: " + creatorNickname
                                + " | " + missionText);
                    } catch (Exception e) {
                        // 개별 미션 파싱 실패 스킵
                    }
                }
            }
        } catch (Exception e) {
            // API 실패 시 무시
        }
    }

    private void evictIdleChannels() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastActivityTime.entrySet()) {
            String channelId = entry.getKey();
            long elapsed = now - entry.getValue();
            if (elapsed >= IDLE_TIMEOUT_MS) {
                String name = channelNames.getOrDefault(channelId, channelId);
                System.out.println("[" + now() + "][유휴정리] " + name + " (" + (elapsed / 60000) + "분 무응답)");
                ChzzkChat chat = activeConnections.remove(channelId);
                connectedChannelIds.remove(channelId);
                lastActivityTime.remove(channelId);
                channelNames.remove(channelId);
                if (chat != null) {
                    try { chat.closeBlocking(); } catch (Exception ignored) {}
                }
                replaceQueue.offer(new String[]{channelId, name});
            }
        }
    }

    private void processReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] disconnected = replaceQueue.take();

                List<String[]> batch = new ArrayList<>();
                batch.add(disconnected);
                replaceQueue.drainTo(batch);

                System.out.println("[" + now() + "] 대체 채널 " + batch.size() + "개 탐색 중...");
                List<String[]> liveChannels = findLiveChannels();
                Collections.shuffle(liveChannels);

                for (String[] dc : batch) {
                    boolean found = false;
                    for (String[] ch : liveChannels) {
                        if (!connectedChannelIds.contains(ch[0])) {
                            System.out.println("[" + now() + "] " + dc[1] + " \u2192 " + ch[1] + " 대체 연결");
                            connectChannel(ch[0], ch[1]);
                            Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("[" + now() + "] " + dc[1] + " 대체 불가 (현재 " + activeConnections.size() + "개 연결)");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Test
    void testMissionDonationLifecycle() throws Exception {
        List<String[]> liveChannels = findLiveChannels();
        Assumptions.assumeTrue(!liveChannels.isEmpty(), "라이브 중인 채널이 없어 테스트를 건너뜁니다.");
        Collections.shuffle(liveChannels);

        File logDir = new File("build/mission-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        eventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now() + "] === 미션 후원 모니터링 ===");
        System.out.println("[" + now() + "] 채널 수: " + liveChannels.size());
        System.out.println("[" + now() + "] 로그 파일: " + logFile.getAbsolutePath());
        System.out.println("[" + now() + "] 종료: Ctrl+C");
        System.out.println();

        try {
            for (String[] channel : liveChannels) {
                connectChannel(channel[0], channel[1]);
                Thread.sleep(CONNECT_INTERVAL_SECONDS * 1000L);
            }

            System.out.println();
            System.out.println("[" + now() + "] 초기 연결 완료: " + activeConnections.size() + "개 채널");
            System.out.println("[" + now() + "] 이벤트 수집 중... (끊김 시 자동 대체)");

            replaceWorker.submit(this::processReplaceQueue);
            idleChecker.scheduleAtFixedRate(this::evictIdleChannels, 1, 1, TimeUnit.MINUTES);

            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("\n[" + now() + "] 테스트 중단");
        } finally {
            idleChecker.shutdownNow();
            replaceWorker.shutdownNow();
            System.out.println("[" + now() + "] " + activeConnections.size() + "개 연결 해제 중...");
            for (ChzzkChat chat : activeConnections.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }
            eventLog.close();
            System.out.println("[" + now() + "] 완료 — 로그: " + logFile.getAbsolutePath());
        }
    }
}
