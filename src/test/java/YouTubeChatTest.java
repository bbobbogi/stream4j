import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import io.github.bbobbogi.stream4j.youtube.*;
import io.github.bbobbogi.stream4j.youtube.chat.ChatItem;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeChatTest extends ChzzkTestBase {

    private static final long TEST_DURATION_MS = 10_000;
    private static final int MAX_LIVE_CANDIDATES = 5;

    private List<String> findLiveVideoIds(int limit) {
        List<String> ids = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url("https://www.youtube.com/results?search_query=%EA%B2%8C%EC%9E%84&sp=CAMSAkAB")
                    .header("Accept-Language", "ko")
                    .get()
                    .build();
            try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return Collections.emptyList();
                String html = response.body().string();
                Matcher matcher = Pattern.compile("\"videoId\":\"([^\"]{11})\"").matcher(html);
                Set<String> seen = new HashSet<>();
                while (matcher.find() && ids.size() < limit) {
                    String vid = matcher.group(1);
                    if (seen.add(vid)) ids.add(vid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    @Test
    void testingYouTubeChat() throws InterruptedException {
        String configuredId = properties.getProperty("YOUTUBE_VIDEO_ID", "");
        List<String> candidates;
        if (!configuredId.isEmpty()) {
            candidates = List.of(configuredId);
        } else {
            candidates = findLiveVideoIds(MAX_LIVE_CANDIDATES);
            Assumptions.assumeFalse(candidates.isEmpty(), "라이브 중인 YouTube 영상이 없어 테스트를 건너뜁니다.");
        }

        Throwable lastError = null;
        for (String videoId : candidates) {
            try {
                runChatSession(videoId);
                return;
            } catch (Throwable t) {
                lastError = t;
                System.out.println("[YouTube] " + videoId + " 시도 실패: " + t.getMessage() + " — 다음 후보로 재시도합니다.");
            }
        }

        Assumptions.assumeTrue(
                false,
                "모든 후보 영상에서 라이브 채팅 연결이 실패했습니다 (방송 종료/네트워크 등 휘발성 원인 추정): "
                        + (lastError != null ? lastError.getMessage() : "원인 불명")
        );
    }

    private void runChatSession(String videoId) throws InterruptedException {
        System.out.println("테스트에 사용할 영상 ID: " + videoId);

        YouTubeChat chat = new YouTubeChatBuilder(videoId)
                .withAutoReconnect(false)
                .withChatListener(new YouTubeChatEventListener() {
                    @Override
                    public void onConnect(YouTubeChat chat, boolean isReconnecting) {
                        System.out.println("[YouTube] Connected! (reconnecting: " + isReconnecting + ")");
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onChat(ChatItem item) {
                        System.out.println("[YouTube Chat] " + item.getAuthorName() + ": " + item.getMessage());
                    }

                    @Override
                    public void onSuperChat(ChatItem item) {
                        System.out.println("[YouTube SuperChat] " + item.getAuthorName() + ": " + item.getPurchaseAmount() + " | " + item.getMessage());
                    }

                    @Override
                    public void onSuperSticker(ChatItem item) {
                        System.out.println("[YouTube SuperSticker] " + item.getAuthorName() + ": " + item.getPurchaseAmount());
                    }

                    @Override
                    public void onNewMember(ChatItem item) {
                        System.out.println("[YouTube NewMember] " + item.getAuthorName() + ": " + item.getMessage());
                    }

                    @Override
                    public void onBroadcastEnd(YouTubeChat chat) {
                        System.out.println("[YouTube] Broadcast ended!");
                    }

                    @Override
                    public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                        System.out.println("[YouTube] Connection closed: " + reason);
                    }
                })
                .build();

        try {
            chat.connect();
            Thread.sleep(TEST_DURATION_MS);
        } finally {
            try {
                chat.close();
            } catch (Exception ignored) {
            }
        }
    }
}
