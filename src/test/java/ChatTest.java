import com.google.gson.Gson;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import io.github.bbobbogi.stream4j.chzzk.*;
import io.github.bbobbogi.stream4j.chzzk.chat.*;
import io.github.bbobbogi.stream4j.util.RawApiUtils;

import java.io.IOException;
import java.util.Optional;

public class ChatTest extends ChzzkTestBase {
    // CI 환경에서는 30초, 로컬에서는 700초 대기
    private static final long TEST_DURATION_MS = 30_000;

    @Test
    void testingChat() throws IOException, InterruptedException {
        // 동적으로 라이브 중인 채널을 찾음
        Optional<String> liveChannelId = findLiveChannelId();
        Assumptions.assumeTrue(liveChannelId.isPresent(), "라이브 중인 채널이 없어 테스트를 건너뜁니다.");

        String channelId = liveChannelId.get();
        System.out.println("테스트에 사용할 채널 ID: " + channelId);

        ChzzkChat chat = loginChzzk.chat(channelId)
                .withChatListener(new ChzzkChatEventListener() {
                    @Override
                    public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                        System.out.println("Connect received!");
                        //chat.sendChat("ㅋㅋㅋㅋ");

                        if (!isReconnecting)
                            chat.requestRecentChat(50);
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onChat(ChatMessage msg) {

                        System.out.println(msg);

                        if (msg.getProfile() == null) {
                            System.out.println("[Chat] 익명: " + msg.getContent());
                            return;
                        }

                        System.out.println("[Chat] " + msg.getProfile().getNickname() + ": " + msg.getContent());
                    }

                    @Override
                    public void onDonationChat(DonationMessage msg) {
                        if (msg.getProfile() == null) {
                            System.out.println("[Donation] 익명: " + msg.getContent() + ": " + msg.getContent() + " [" + msg.getPayAmount() + "원]");
                            return;
                        }

                        System.out.println("[Donation] " + msg.getProfile().getNickname() + ": " + msg.getContent() + " [" + msg.getPayAmount() + "원]");
                    }

                    @Override
                    public void onSubscriptionChat(SubscriptionMessage msg) {
                        if (msg.getProfile() == null) {
                            System.out.println("[Subscription] 익명: " + msg.getContent() + ": [" + msg.getSubscriptionMonth() + "개월 " + msg.getSubscriptionTierName() + "]");
                            return;
                        }

                        System.out.println("[Subscription] " + msg.getProfile().getNickname() + ": [" + msg.getSubscriptionMonth() + "개월 " + msg.getSubscriptionTierName() + "]");
                    }

                    @Override
                    public void onMissionDonationChat(MissionDonationMessage msg) {
                        if (msg.getProfile() == null) {
                            System.out.println("[Mission] 익명: " + msg.getMissionText() + ": [" + msg.getPayAmount() + "원]");
                            return;
                        }

                        System.out.println("[Mission] 익명: " + msg.getMissionText() + ": [" + msg.getPayAmount() + "원]");
                    }

                    @Override
                    public void onPartyDonationChat(PartyDonationMessage msg) {
                        if (msg.getProfile() == null) {
                            System.out.println("[Party] 익명: " + msg.getContent() + ": " + msg.getContent() + " [" + msg.getPayAmount() + "원 / " + msg.getPartyName() + "]");
                            return;
                        }

                        System.out.println("[Party] " + msg.getProfile().getNickname() + ": " + msg.getContent() + " [" + msg.getPayAmount() + "원 / " + msg.getPartyName() + "]");
                    }
                })
                .build();

        System.out.println(new Gson().toJson(RawApiUtils.getContentJson(chzzk.getHttpClient(),
                RawApiUtils.httpGetRequest("https://api.chzzk.naver.com/service/v2/channels/" + channelId + "/live-detail").build(), chzzk.isDebug)));
         chat.connect();
         Thread.sleep(TEST_DURATION_MS);
         chat.close();
    }
}