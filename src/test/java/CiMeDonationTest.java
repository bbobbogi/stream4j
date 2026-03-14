import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.cime.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class CiMeDonationTest extends CiMeTestBase {

    private static final int CONNECT_INTERVAL_SECONDS = 5;
    private static final int MAX_CHANNELS = 50;
    private static final long IDLE_TIMEOUT_MS = 10 * 60 * 1000L; // 10분
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Map<String, CiMeChat> activeConnections = new ConcurrentHashMap<>();
    private final Set<String> connectedSlugs = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final Map<String, String> channelNames = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String[]> replaceQueue = new LinkedBlockingQueue<>();
    private final ExecutorService replaceWorker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cime-replace-worker");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService idleChecker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cime-idle-checker");
        t.setDaemon(true);
        return t;
    });
    private PrintWriter eventLog;

    private static String now() {
        return "CiMe|" + LocalDateTime.now().format(TIME_FMT);
    }

    private synchronized void saveEvent(String eventType, String channelName, String rawJson, Map<String, Object> parsed) {
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

    private void connectChannel(String channelSlug, String channelName) {
        if (connectedSlugs.contains(channelSlug)) return;

        try {
            CiMeChat chat = new CiMeChatBuilder(channelSlug)
                    .withAutoReconnect(false)
                    .withChatListener(new CiMeChatEventListener() {
                        @Override
                        public void onConnect(CiMeChat chat, boolean isReconnecting) {
                            System.out.println("[" + now() + "][연결] " + channelName + " (" + channelSlug + ")");
                        }

                        @Override
                        public void onError(Exception ex) {
                            System.out.println("[" + now() + "][에러] " + channelName + " — " + ex.getMessage());
                        }

                        @Override
                        public void onChat(CiMeChatMessage msg) {
                            lastActivityTime.put(channelSlug, System.currentTimeMillis());
                            String nickname = msg.hasUser() && msg.getUser().getNickname() != null
                                    ? msg.getUser().getNickname() : "익명";
                            // System.out.println("[" + now() + "][Chat] " + channelName
                            //         + " | " + nickname + " | " + msg.getContent());
                        }

                        @Override
                        public void onEvent(String eventName, String rawJson) {
                            lastActivityTime.put(channelSlug, System.currentTimeMillis());

                            Map<String, Object> parsed = new LinkedHashMap<>();
                            parsed.put("eventName", eventName);

                            // DONATION_CHAT — 채팅 후원
                            if ("DONATION_CHAT".equals(eventName)) {
                                parseDonationChat(channelName, rawJson, parsed);
                                return;
                            }

                            // DONATION_VIDEO — 영상 후원
                            if ("DONATION_VIDEO".equals(eventName)) {
                                parseDonationVideo(channelName, rawJson, parsed);
                                return;
                            }

                            // DONATION_MISSION — 미션 생성/등록
                            if ("DONATION_MISSION".equals(eventName)) {
                                parseDonationMission(channelName, rawJson, parsed);
                                return;
                            }

                            // DONATION_MISSION_UPDATED — 미션 상태 변경
                            if ("DONATION_MISSION_UPDATED".equals(eventName)) {
                                parseDonationMissionUpdated(channelSlug, channelName, rawJson, parsed);
                                return;
                            }

                            // DONATION_MISSION_REWARD_ADDED — 미션 참여
                            if ("DONATION_MISSION_REWARD_ADDED".equals(eventName)) {
                                parseDonationMissionReward(channelName, rawJson, parsed);
                                return;
                            }

                            // LIVE_ENDED → 방송 종료, 대체 채널 연결
                            if ("LIVE_ENDED".equals(eventName)) {
                                System.out.println("[" + now() + "][방송종료] " + channelName + " — 대체 채널 탐색");
                                saveEvent(eventName, channelName, rawJson, parsed);
                                CiMeChat c = activeConnections.remove(channelSlug);
                                connectedSlugs.remove(channelSlug);
                                lastActivityTime.remove(channelSlug);
                                channelNames.remove(channelSlug);
                                if (c != null) {
                                    try { c.closeBlocking(); } catch (Exception ignored) {}
                                }
                                replaceQueue.offer(new String[]{channelSlug, channelName});
                                return;
                            }

                            // SUBSCRIPTION_MESSAGE — 구독 알림
                            if ("SUBSCRIPTION_MESSAGE".equals(eventName)) {
                                parseSubscription(channelName, rawJson, parsed);
                                return;
                            }

                            // SUBSCRIPTION_GIFT_MESSAGE — 선물 구독
                            if ("SUBSCRIPTION_GIFT_MESSAGE".equals(eventName)) {
                                parseSubscriptionGift(channelName, rawJson, parsed);
                                return;
                            }

                            // 기타 이벤트 — rawJson 그대로 저장
                            saveEvent(eventName, channelName, rawJson, parsed);
                        }

                        @Override
                        public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                            System.out.println("[" + now() + "][연결종료] " + channelName + " | code=" + code + " reason=" + reason);
                            // evictIdleChannels/LIVE_ENDED에서 이미 제거했으면 중복 교체 방지
                            if (activeConnections.remove(channelSlug) == null) return;
                            connectedSlugs.remove(channelSlug);
                            lastActivityTime.remove(channelSlug);
                            channelNames.remove(channelSlug);
                            replaceQueue.offer(new String[]{channelSlug, channelName});
                        }
                    })
                    .build();

            chat.connectBlocking();
            activeConnections.put(channelSlug, chat);
            connectedSlugs.add(channelSlug);
            lastActivityTime.put(channelSlug, System.currentTimeMillis());
            channelNames.put(channelSlug, channelName);
        } catch (Exception e) {
            System.out.println("[" + now() + "][에러] " + channelName + " 연결 실패: " + e.getMessage());
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

                System.out.println("[" + now() + "][Donation] " + channelName
                        + " | " + nickname + " | " + amt + "원 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] DONATION_CHAT: " + e.getMessage());
        }

        saveEvent("DONATION_CHAT", channelName, rawJson, parsed);
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

                System.out.println("[" + now() + "][영상후원] " + channelName
                        + " | " + (anon ? "익명" : "후원자") + " | " + amt + "원 | " + videoTitle + " | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] DONATION_VIDEO: " + e.getMessage());
        }

        saveEvent("DONATION_VIDEO", channelName, rawJson, parsed);
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

                System.out.println("[" + now() + "][미션생성] " + channelName
                        + " | " + nickname + " | " + status + " | " + amt + "원 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] DONATION_MISSION: " + e.getMessage());
        }

        saveEvent("DONATION_MISSION", channelName, rawJson, parsed);
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
            System.out.println("[" + now() + "][파싱에러] DONATION_MISSION_UPDATED: " + e.getMessage());
        }

        // active-missions API로 미션 상세 조회
        try {
            Request request = new Request.Builder()
                    .url(CI_ME_API_URL + "/channels/" + channelSlug + "/active-missions")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JsonObject apiJson = JsonParser.parseString(body).getAsJsonObject();
                    if (apiJson.get("code").getAsInt() == 200) {
                        JsonArray missions = apiJson.getAsJsonObject("data").getAsJsonArray("missions");
                        if (missions != null) {
                            for (JsonElement missionEl : missions) {
                                JsonObject mission = missionEl.getAsJsonObject();
                                String id = mission.get("id").getAsString();

                                // missionId가 있으면 해당 미션만, 없으면 전부 출력
                                if (missionId != null && !missionId.equals(id)) continue;

                                String desc = mission.has("description") ? mission.get("description").getAsString() : "";
                                String state = mission.has("state") ? mission.get("state").getAsString() : "";
                                int reward = mission.has("reward") ? mission.get("reward").getAsInt() : 0;

                                parsed.put("description", desc);
                                parsed.put("state", state);
                                parsed.put("reward", reward);

                                // 참여자 목록
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

                                System.out.println("[" + now() + "][미션업데이트] " + channelName
                                        + " | mId=" + id + " | " + state + " | " + reward + "원 | " + desc);
                                if (supportersStr.length() > 0) {
                                    System.out.println("[" + now() + "][미션참여자] " + channelName
                                            + " | " + supportersStr);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // API 실패 시 기본 로그만 출력
            System.out.println("[" + now() + "][미션업데이트] " + channelName + " | mId=" + missionId);
        }

        saveEvent("DONATION_MISSION_UPDATED", channelName, rawJson, parsed);
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

                    System.out.println("[" + now() + "][미션참여] " + channelName
                            + " | " + nickname + " | " + amt + "원 | " + msg);
                }
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] DONATION_MISSION_REWARD_ADDED: " + e.getMessage());
        }

        saveEvent("DONATION_MISSION_REWARD_ADDED", channelName, rawJson, parsed);
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

                System.out.println("[" + now() + "][구독] " + channelName
                        + " | " + nickname + " | " + duration + "개월 " + tier + "티어 | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] SUBSCRIPTION_MESSAGE: " + e.getMessage());
        }

        saveEvent("SUBSCRIPTION_MESSAGE", channelName, rawJson, parsed);
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

                System.out.println("[" + now() + "][선물구독] " + channelName
                        + " | " + nickname + " | " + count + "개 " + targetType + " | " + msg);
            }
        } catch (Exception e) {
            System.out.println("[" + now() + "][파싱에러] SUBSCRIPTION_GIFT_MESSAGE: " + e.getMessage());
        }

        saveEvent("SUBSCRIPTION_GIFT_MESSAGE", channelName, rawJson, parsed);
    }

    private void evictIdleChannels() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastActivityTime.entrySet()) {
            String slug = entry.getKey();
            long elapsed = now - entry.getValue();
            if (elapsed >= IDLE_TIMEOUT_MS) {
                String name = channelNames.getOrDefault(slug, slug);
                System.out.println("[" + now() + "][유휴정리] " + name + " (" + (elapsed / 60000) + "분 무응답)");
                CiMeChat chat = activeConnections.remove(slug);
                connectedSlugs.remove(slug);
                lastActivityTime.remove(slug);
                channelNames.remove(slug);
                if (chat != null) {
                    try { chat.closeBlocking(); } catch (Exception ignored) {}
                }
                replaceQueue.offer(new String[]{slug, name});
            }
        }
    }

    private void processReplaceQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String[] disconnected = replaceQueue.take();

                // 큐에 더 있으면 모아서 한 번에 처리
                List<String[]> batch = new ArrayList<>();
                batch.add(disconnected);
                replaceQueue.drainTo(batch);

                System.out.println("[" + now() + "] 대체 채널 " + batch.size() + "개 탐색 중...");
                List<String[]> liveChannels = findLiveChannels(MAX_CHANNELS);
                Collections.shuffle(liveChannels);

                for (String[] dc : batch) {
                    boolean found = false;
                    for (String[] ch : liveChannels) {
                        if (!connectedSlugs.contains(ch[0])) {
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
    void testDonationMonitoring() throws Exception {
        List<String[]> liveChannels = findLiveChannels(MAX_CHANNELS);
        Assumptions.assumeTrue(!liveChannels.isEmpty(), "라이브 중인 ci.me 채널이 없어 테스트를 건너뜁니다.");
        Collections.shuffle(liveChannels);

        File logDir = new File("build/cime-donation-logs");
        logDir.mkdirs();
        File logFile = new File(logDir, "events-" + LocalDateTime.now().format(FILE_TIME_FMT) + ".jsonl");
        eventLog = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));

        System.out.println("[" + now() + "] === ci.me 후원 모니터링 ===");
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

            // 대체 큐 워커 시작
            replaceWorker.submit(this::processReplaceQueue);

            // 1분마다 유휴 채널 검사
            idleChecker.scheduleAtFixedRate(this::evictIdleChannels, 1, 1, TimeUnit.MINUTES);

            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("\n[" + now() + "] 테스트 중단");
        } finally {
            idleChecker.shutdownNow();
            replaceWorker.shutdownNow();
            System.out.println("[" + now() + "] " + activeConnections.size() + "개 연결 해제 중...");
            for (CiMeChat chat : activeConnections.values()) {
                try { chat.closeBlocking(); } catch (Exception ignored) {}
            }
            eventLog.close();
            System.out.println("[" + now() + "] 완료 — 로그: " + logFile.getAbsolutePath());
        }
    }
}
