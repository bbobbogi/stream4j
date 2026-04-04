import org.junit.jupiter.api.Test;
import io.github.bbobbogi.stream4j.chzzk.Chzzk;
import io.github.bbobbogi.stream4j.chzzk.*;
import io.github.bbobbogi.stream4j.chzzk.chat.*;
import io.github.bbobbogi.stream4j.cime.*;
import io.github.bbobbogi.stream4j.soop.*;
import io.github.bbobbogi.stream4j.youtube.*;
import io.github.bbobbogi.stream4j.util.RawApiUtils;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.*;
import okhttp3.Request;
import okhttp3.Response;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolLeakTest extends ChzzkTestBase {

    private static final int POOL_SIZE = 5;
    private static final long CONNECT_WAIT_MS = 3_000;
    private static final long CLEANUP_WAIT_MS = 3_000;
    private static final int ALLOWED_LEAKAGE = 2;

    @Test
    void testChzzkConnectionPool() throws Exception {
        List<String> channelIds = fetchChzzkLiveChannels(POOL_SIZE * 3);
        org.junit.jupiter.api.Assumptions.assumeFalse(channelIds.isEmpty(),
                "라이브 중인 치지직 채널이 없어 테스트를 건너뜁니다.");

        System.out.println("=== 치지직 커넥션 풀 테스트 (" + channelIds.size() + "채널) ===");

        Set<String> baseline = snapshotThreads();
        int successCount = 0;
        int failCount = 0;

        int totalBatches = (int) Math.ceil((double) channelIds.size() / POOL_SIZE);
        for (int batchIdx = 0; batchIdx < totalBatches; batchIdx++) {
            int from = batchIdx * POOL_SIZE;
            int to = Math.min(from + POOL_SIZE, channelIds.size());
            List<String> batch = channelIds.subList(from, to);

            System.out.println("--- 배치 " + (batchIdx + 1) + "/" + totalBatches + " ---");

            List<ChzzkChat> chats = new ArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
            List<Future<ChzzkChat>> futures = new ArrayList<>();

            for (String channelId : batch) {
                futures.add(pool.submit(() -> {
                    try {
                        ChzzkChat chat = (loginChzzk != null ? loginChzzk : chzzk).chat(channelId)
                                .withAutoReconnect(false)
                                .withChatListener(new ChzzkChatEventListener() {
                                    @Override public void onConnect(ChzzkChat c, boolean r) {
                                        System.out.println("  [치지직 연결] " + c.getChannelId());
                                    }
                                    @Override public void onError(Exception ex) {}
                                    @Override public void onConnectionClosed(int code, String reason, boolean remote, boolean tryReconnect) {}
                                })
                                .build();
                        chat.connect();
                        return chat;
                    } catch (Exception e) {
                        System.out.println("  [치지직 실패] " + channelId + ": " + e.getMessage());
                        return null;
                    }
                }));
            }

            for (Future<ChzzkChat> f : futures) {
                try {
                    ChzzkChat c = f.get(30, TimeUnit.SECONDS);
                    if (c != null) { chats.add(c); successCount++; } else { failCount++; }
                } catch (Exception e) { failCount++; }
            }
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);

            System.out.println("  연결: " + chats.size() + "/" + batch.size());
            Thread.sleep(CONNECT_WAIT_MS);

            for (ChzzkChat c : chats) { try { c.close(); } catch (Exception ignored) {} }
            Thread.sleep(CLEANUP_WAIT_MS);
        }

        System.out.println("성공: " + successCount + " / 실패: " + failCount);
        assertNoLeak(baseline);
    }

    @Test
    void testSOOPConnectionPool() throws Exception {
        List<String> streamerIds = fetchSOOPLiveStreamers(POOL_SIZE);
        org.junit.jupiter.api.Assumptions.assumeFalse(streamerIds.isEmpty(),
                "라이브 중인 SOOP 채널이 없어 테스트를 건너뜁니다.");

        System.out.println("=== SOOP 커넥션 풀 테스트 (" + streamerIds.size() + "채널) ===");

        Set<String> baseline = snapshotThreads();
        List<SOOPChat> chats = new ArrayList<>();

        for (String sid : streamerIds) {
            try {
                SOOPChat sc = new SOOPChatBuilder(sid).withAutoReconnect(false)
                        .withChatListener(new SOOPChatEventListener() {
                            @Override public void onConnect(SOOPChat c, boolean r) { System.out.println("  [SOOP 연결] " + sid); }
                            @Override public void onError(Exception ex) {}
                        }).build();
                sc.connect();
                chats.add(sc);
            } catch (Exception e) {
                System.out.println("  [SOOP 실패] " + sid + ": " + e.getMessage());
            }
        }

        System.out.println("  연결: " + chats.size() + "/" + streamerIds.size());
        Thread.sleep(CONNECT_WAIT_MS);

        for (SOOPChat sc : chats) { try { sc.close(); } catch (Exception ignored) {} }
        Thread.sleep(CLEANUP_WAIT_MS);

        assertNoLeak(baseline);
    }

    @Test
    void testCiMeConnectionPool() throws Exception {
        List<String> slugs = fetchCiMeLiveChannels(POOL_SIZE);
        org.junit.jupiter.api.Assumptions.assumeFalse(slugs.isEmpty(),
                "라이브 중인 CiMe 채널이 없어 테스트를 건너뜁니다.");

        System.out.println("=== CiMe 커넥션 풀 테스트 (" + slugs.size() + "채널) ===");

        Set<String> baseline = snapshotThreads();
        List<CiMeChat> chats = new ArrayList<>();

        for (String slug : slugs) {
            try {
                CiMeChat cc = new CiMeChatBuilder(slug).withAutoReconnect(false)
                        .withChatListener(new CiMeChatEventListener() {
                            @Override public void onConnect(CiMeChat c, boolean r) { System.out.println("  [CiMe 연결] " + slug); }
                            @Override public void onError(Exception ex) {}
                        }).build();
                cc.connect();
                chats.add(cc);
            } catch (Exception e) {
                System.out.println("  [CiMe 실패] " + slug + ": " + e.getMessage());
            }
        }

        System.out.println("  연결: " + chats.size() + "/" + slugs.size());
        Thread.sleep(CONNECT_WAIT_MS);

        for (CiMeChat cc : chats) { try { cc.close(); } catch (Exception ignored) {} }
        Thread.sleep(CLEANUP_WAIT_MS);

        assertNoLeak(baseline);
    }

    @Test
    void testYouTubeConnectionPool() throws Exception {
        List<String> videoIds = fetchYouTubeLiveVideos(POOL_SIZE);
        org.junit.jupiter.api.Assumptions.assumeFalse(videoIds.isEmpty(),
                "라이브 중인 YouTube 영상이 없어 테스트를 건너뜁니다.");

        System.out.println("=== YouTube 커넥션 풀 테스트 (" + videoIds.size() + "개) ===");

        Set<String> baseline = snapshotThreads();
        List<YouTubeChat> chats = new ArrayList<>();

        for (String vid : videoIds) {
            try {
                YouTubeChat yc = new YouTubeChatBuilder(vid).withAutoReconnect(false)
                        .withChatListener(new YouTubeChatEventListener() {
                            @Override public void onConnect(YouTubeChat c, boolean r) { System.out.println("  [YouTube 연결] " + vid); }
                            @Override public void onError(Exception ex) {}
                        }).build();
                yc.connect();
                chats.add(yc);
            } catch (Exception e) {
                System.out.println("  [YouTube 실패] " + vid);
            }
        }

        System.out.println("  연결: " + chats.size() + "/" + videoIds.size());
        Thread.sleep(CONNECT_WAIT_MS);

        for (YouTubeChat yc : chats) { try { yc.close(); } catch (Exception ignored) {} }
        Thread.sleep(CLEANUP_WAIT_MS);

        assertNoLeak(baseline);
    }

    @Test
    void dumpAllThreads() {
        System.out.println("=== 전체 스레드 덤프 ===");
        Thread.getAllStackTraces().keySet().stream()
                .sorted(Comparator.comparing(Thread::getName))
                .forEach(t -> System.out.printf("  [%s] %s daemon=%b state=%s%n",
                        t.getThreadGroup() != null ? t.getThreadGroup().getName() : "null",
                        t.getName(), t.isDaemon(), t.getState()));
    }

    private Set<String> snapshotThreads() throws InterruptedException {
        Thread.sleep(500);
        Set<String> threads = getMonitoredThreadNames();
        System.out.println("[기준] 관련 스레드: " + threads.size());
        return threads;
    }

    private void assertNoLeak(Set<String> baseline) throws InterruptedException {
        System.gc();
        Thread.sleep(CLEANUP_WAIT_MS);

        Set<String> after = getMonitoredThreadNames();
        Set<String> leaked = new HashSet<>(after);
        leaked.removeAll(baseline);

        System.out.println("[최종] 관련 스레드: " + after.size() + " (기준: " + baseline.size() + ")");
        if (!leaked.isEmpty()) {
            System.out.println("누수 의심:");
            leaked.forEach(t -> System.out.println("  - " + t));
        }

        assertTrue(after.size() <= baseline.size() + ALLOWED_LEAKAGE,
                "스레드 누수! 기준: " + baseline.size() + " 최종: " + after.size() + " 누수: " + leaked);
    }

    private Set<String> getMonitoredThreadNames() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(t -> {
                    String name = t.getName().toLowerCase();
                    return name.contains("chzzk") || name.contains("ws-ping") ||
                            name.contains("soop") || name.contains("youtube") ||
                            name.contains("chat-poller") || name.contains("cime");
                })
                .map(Thread::getName)
                .collect(Collectors.toSet());
    }

    private List<String> fetchChzzkLiveChannels(int max) {
        List<String> channelIds = new ArrayList<>();
        try {
            JsonElement content = RawApiUtils.getContentJson(
                    chzzk.getHttpClient(),
                    RawApiUtils.httpGetRequest(Chzzk.API_URL + "/service/v1/streamer-partners/recommended").build(),
                    chzzk.isDebug);
            JsonArray partners = content.getAsJsonObject().getAsJsonArray("streamerPartners");
            if (partners == null) return channelIds;

            for (JsonElement el : partners) {
                JsonObject s = el.getAsJsonObject();
                if (s.get("openLive").getAsBoolean()) {
                    channelIds.add(s.get("channelId").getAsString());
                    System.out.println("  [치지직] " + s.get("channelName").getAsString());
                    if (channelIds.size() >= max) break;
                }
            }
        } catch (Exception e) {
            System.out.println("[치지직] 채널 목록 조회 실패: " + e.getMessage());
        }
        return channelIds;
    }

    private List<String> fetchSOOPLiveStreamers(int max) {
        List<String> ids = new ArrayList<>();
        try {
            Request req = new Request.Builder()
                    .url("https://live.sooplive.co.kr/api/main_broad_list_api.php?selectType=action&selectValue=all&orderType=view_cnt&pageNo=1&lang=ko_KR&_=" + System.currentTimeMillis())
                    .get().build();
            try (Response resp = SharedHttpClient.get().newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                    JsonArray broads = json.has("broad") ? json.getAsJsonArray("broad") : null;
                    if (broads != null) {
                        for (int i = 0; i < Math.min(max, broads.size()); i++) {
                            String sid = broads.get(i).getAsJsonObject().get("user_id").getAsString();
                            ids.add(sid);
                            System.out.println("  [SOOP] " + sid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SOOP] 채널 목록 조회 실패: " + e.getMessage());
        }
        return ids;
    }

    private List<String> fetchCiMeLiveChannels(int max) {
        List<String> slugs = new ArrayList<>();
        try {
            Request req = new Request.Builder()
                    .url("https://ci.me/api/app/lives?sort=POPULAR")
                    .get().build();
            try (Response resp = CiMeChat.getSharedHttpClient().newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return slugs;
                JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                if (json.get("code").getAsInt() != 200) return slugs;

                JsonArray sections = json.getAsJsonObject("data").getAsJsonArray("sections");
                for (JsonElement secEl : sections) {
                    JsonObject sec = secEl.getAsJsonObject();
                    if (!"LIVE".equals(sec.get("type").getAsString())) continue;
                    JsonArray items = sec.getAsJsonArray("items");
                    if (items == null) continue;

                    for (JsonElement itemEl : items) {
                        JsonObject item = itemEl.getAsJsonObject();
                        if (!"ACTIVE".equals(item.get("state").getAsString())) continue;
                        if (item.has("isAdult") && item.get("isAdult").getAsBoolean()) continue;
                        String slug = item.getAsJsonObject("channel").get("slug").getAsString();
                        slugs.add(slug);
                        System.out.println("  [CiMe] " + slug);
                        if (slugs.size() >= max) return slugs;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[CiMe] 채널 목록 조회 실패: " + e.getMessage());
        }
        return slugs;
    }

    private List<String> fetchYouTubeLiveVideos(int max) {
        List<String> vids = new ArrayList<>();
        try {
            Request req = new Request.Builder()
                    .url("https://www.youtube.com/results?search_query=%EA%B2%8C%EC%9E%84&sp=CAMSAkAB")
                    .header("Accept-Language", "ko").get().build();
            try (Response resp = SharedHttpClient.get().newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String html = resp.body().string();
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"videoId\":\"([^\"]{11})\"").matcher(html);
                    Set<String> seen = new HashSet<>();
                    while (m.find() && vids.size() < max) {
                        String vid = m.group(1);
                        if (seen.add(vid)) {
                            vids.add(vid);
                            System.out.println("  [YouTube] " + vid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[YouTube] 영상 목록 조회 실패: " + e.getMessage());
        }
        return vids;
    }
}
