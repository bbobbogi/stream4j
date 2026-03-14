import org.junit.jupiter.api.Test;

/**
 * 치지직(Chzzk) + ci.me 동시 모니터링 테스트.
 * 하나의 JVM에서 두 모니터를 스레드로 나눠 실행합니다.
 */
public class CombinedMonitorTest {

    @Test
    void testCombinedMonitoring() throws Exception {
        MissionDonationTest chzzkMonitor = new MissionDonationTest();
        CiMeDonationTest cimeMonitor = new CiMeDonationTest();

        Thread chzzkThread = new Thread(() -> {
            try {
                System.out.println("=== [Chzzk] 모니터링 시작 ===");
                chzzkMonitor.testMissionDonationLifecycle();
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    System.out.println("[Chzzk] 오류: " + e.getMessage());
                }
            }
        }, "chzzk-monitor");

        Thread cimeThread = new Thread(() -> {
            try {
                System.out.println("=== [CiMe] 모니터링 시작 ===");
                cimeMonitor.testDonationMonitoring();
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    System.out.println("[CiMe] 오류: " + e.getMessage());
                }
            }
        }, "cime-monitor");

        chzzkThread.start();
        cimeThread.start();

        // 둘 다 끝날 때까지 대기 (Ctrl+C로 중단)
        chzzkThread.join();
        cimeThread.join();
    }
}
