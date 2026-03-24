import com.bbobbogi.stream4j.util.SharedHttpClient;
import com.bbobbogi.stream4j.youtube.*;
import com.bbobbogi.stream4j.youtube.chat.ChatItem;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeChatTest extends ChzzkTestBase {

    private static final long TEST_DURATION_MS = 30_000;

    private Optional<String> findLiveVideoId() {
        try {
            Request request = new Request.Builder()
                    .url("https://www.youtube.com/results?search_query=%EA%B2%8C%EC%9E%84&sp=CAMSAkAB")
                    .header("Accept-Language", "ko")
                    .get()
                    .build();
            try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return Optional.empty();
                String html = response.body().string();
                Matcher matcher = Pattern.compile("\"videoId\":\"([^\"]{11})\"").matcher(html);
                Set<String> seen = new HashSet<>();
                while (matcher.find()) {
                    String vid = matcher.group(1);
                    if (seen.add(vid)) return Optional.of(vid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Test
    void testingYouTubeChat() throws InterruptedException {
        String configuredId = properties.getProperty("YOUTUBE_VIDEO_ID", "");
        String videoId;
        if (!configuredId.isEmpty()) {
            videoId = configuredId;
        } else {
            Optional<String> found = findLiveVideoId();
            Assumptions.assumeTrue(found.isPresent(), "라이브 중인 YouTube 영상이 없어 테스트를 건너뜁니다.");
            videoId = found.get();
        }

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

        chat.connect();
         Thread.sleep(TEST_DURATION_MS);
         chat.close();
    }
}
