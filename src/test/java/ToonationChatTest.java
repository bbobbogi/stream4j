import com.bbobbogi.stream4j.toonation.*;
import com.bbobbogi.stream4j.toonation.chat.ToonationDonationMessage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class ToonationChatTest extends ChzzkTestBase {

    private static final long TEST_DURATION_MS = 30_000;

    @Test
    void testingToonationChat() throws InterruptedException {
        String alertboxKey = properties.getProperty("TOONATION_KEY", "");
        Assumptions.assumeTrue(!alertboxKey.isEmpty(), "TOONATION_KEY 미설정. 테스트를 건너뜁니다.");

        System.out.println("테스트에 사용할 alertbox key: " + alertboxKey);

        ToonationChat chat = new ToonationChatBuilder(alertboxKey)
                .withAutoReconnect(false)
                .withDebugMode()
                .withChatListener(new ToonationChatEventListener() {
                    @Override
                    public void onConnect(ToonationChat chat, boolean isReconnecting) {
                        System.out.println("[Toonation] Connected! (reconnecting: " + isReconnecting + ")");
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onDonation(ToonationChat chat, ToonationDonationMessage msg) {
                        System.out.println("[Toonation Donation] " + msg.getNickname() + ": " + msg.getAmount() + "원 | " + msg.getMessage());
                    }

                    @Override
                    public void onBlocked(ToonationChat chat) {
                        System.out.println("[Toonation] Blocked!");
                    }

                    @Override
                    public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                        System.out.println("[Toonation] Connection closed: " + reason);
                    }
                })
                .build();

         chat.connect();
         Thread.sleep(TEST_DURATION_MS);
         chat.close();
    }
}
