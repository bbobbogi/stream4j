import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.chat.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolLeakTest extends ChzzkTestBase {

    // 커넥션 풀 크기 (동시 연결 수)
    private static final int POOL_SIZE = 5;

    // 연결 후 대기 시간 (ms) - 연결이 안정화될 때까지 기다림
    private static final long CONNECT_WAIT_MS = 5_000;

    // 종료 후 스레드 정리 대기 시간 (ms)
    private static final long CLEANUP_WAIT_MS = 5_000;

    // 테스트 대상 채널 ID 목록 (제공된 YAML 데이터에서 추출, 중복 제거)
    private static final List<String> CHANNEL_IDS = List.of(
            "4231e028f04298354be53fd1e764f010",
            "a087ae5d1995d2a408e6ab673bf33035",
            "930d34bef2b21170f57f133911ea7c9d",
            "da714e509e48e7a37bdbcede79988c09",
            "b3eb5cb028c67dba6f929852def1b732",
            "b5a74d93551cac43377fc8644f1a672c",
            "dbe82d0cd2dc884d11813d02cd924228",
            "d6905ae5d59aa2a0bc4f569b2b03f559",
            "ad7a449061f71f48284c6410fcbd8749",
            "5efe9e97eac89d28dde10624e2684987",
            "25a2604cab173e5f6f3180d6da8d35fd",
            "0e2647cef57160864e0d6742c56deebd",
            "d7a48b928dcbf95146ec25681271111d",
            "a438faa4fc6df3e1d41aaf52a5a346e6",
            "5942ffb7aa15733c28fbc10a63fa2117",
            "8eb2659893b2faca5bec443c28be265e",
            "7838054c4c10356e3bcc6a4f61d339c3",
            "a976a50b310701ac13ed2c5391064168",
            "1b55e07f8d3cdf58334796ed76cd9699",
            "5b9628b47cda565dd03a3a2c8a853a24",
            "8a13b48bf962d6266f646e584a4ee466",
            "58ff653b1ca8ff2c7f352dab48a8f9e4",
            "c7db93c80112a4ccbaf29a45774347ee",
            "09b83455274a50bf0373737290b8821f",
            "eaa3d33a92b7fb253dd88bcf718b1897",
            "1c20761084b9587e4f6b0109187772ab",
            "4daecd525bf43a58c8b1f15263b5b9a5",
            "234e3c5d56094cb4fa58b30503b14d1d",
            "0490d625f4fcb3c1a7d8ad0290998a39",
            "33b49cdf98af3327f34fd017b6b7e853",
            "1c7b7019482efba3abfdc88aa6139f8f",
            "022cef96206d3ca5ff8dfd20f4998b00",
            "b1f0c7bf431fa5bbeeab4101f6fe5169",
            "b41937fced2370c1912e6949b4c59227",
            "f17fcf97e9d16287fefb45a038f36d71",
            "94dac968f9ce5ba03ffc93c081d44732",
            "8a614be10ddd7f2f006bd94c09a7ebc0",
            "00b395f25499b6a31a685550b5ae9a4c",
            "7b14b8186433531e7fca59164dca8b7e",
            "80431a7540076a4d6d2c29fa68709136",
            "1b7263344f2fb5f0713832e670fb9c39",
            "05c6daaae355aeae2f1843b9edc426b3",
            "29fc120282b520c5268858832034963d",
            "6c144288aea90dbb1f4d9f60da6e8acf",
            "6fbfc1f21a4ea8d2890ce84f814cac6a",
            "823c9105464f637d8a208d11d2bbfd93",
            "7b55c9c81d652146c9e069b8eca2bcaf",
            "3c2e52138d5b7fd1c1b750df807b8fc0",
            "6f96757cb6526fe15745b1470530f276",
            "7c55f6ef57ef4ef3b93d3e2c2ba89a1c",
            "d8c10a2e77e76677b093582ef6373e5b",
            "900d761fd519764075fd7ee8195a80b1",
            "fc463a000289c4e7b212ad6a8f8c793e",
            "bd435a37373e596d583ed6fd2f17d2f3",
            "bbd2d3a7abc36bc1d71fab75c35fa0e7",
            "4b2b2873dd2eca2b092945f9f448a762",
            "37f098f35c0327d1873f84a489356e03",
            "3d74925cb9c9b0db1fd213da16db6289",
            "7a7726e1c6eb40c7ef2f215718f24a95",
            "b8c7bed625293faf610cde79ccca139d",
            "859dade854204ad1d411399e1ed6fc2a",
            "ba4d5273baec6f02461b0a0cb4e19a79",
            "0298122d69a9b53d2dfc4c4e84d68a09",
            "625c363bc29084dda44544a66b8f1d77",
            "98d01e25e79820a55d261f3baf19f2eb",
            "70d4c17a7420222f5a249b9e943e2ea8",
            "e6eb84bc88967e10c5f01e7050b59a33",
            "eef2e1a2ddb0c24d0e9e7c30a18f75cb"
    );

    @Test
    void testConnectionPoolNoThreadLeak() throws Exception {
        System.out.println("=== 커넥션 풀 스레드 누수 테스트 시작 ===");
        System.out.println("총 채널 수: " + CHANNEL_IDS.size());
        System.out.println("풀 크기: " + POOL_SIZE);
        System.out.println();

        // 1. 테스트 시작 전 스레드 스냅샷
        Thread.sleep(1000); // JVM 안정화 대기
        Set<String> baselineThreads = getChzzkThreadNames();
        int baselineCount = baselineThreads.size();
        System.out.println("[기준] chzzk 관련 스레드 수: " + baselineCount);
        if (!baselineThreads.isEmpty()) {
            baselineThreads.forEach(t -> System.out.println("  - " + t));
        }
        System.out.println();

        // 모든 스레드 기준선 (전체)
        int baselineAllThreads = Thread.activeCount();
        System.out.println("[기준] 전체 활성 스레드 수: " + baselineAllThreads);
        System.out.println();

        // 2. 배치별로 연결/해제 반복
        int totalBatches = (int) Math.ceil((double) CHANNEL_IDS.size() / POOL_SIZE);
        int successCount = 0;
        int failCount = 0;

        for (int batchIdx = 0; batchIdx < totalBatches; batchIdx++) {
            int fromIndex = batchIdx * POOL_SIZE;
            int toIndex = Math.min(fromIndex + POOL_SIZE, CHANNEL_IDS.size());
            List<String> batchChannels = CHANNEL_IDS.subList(fromIndex, toIndex);

            System.out.println("--- 배치 " + (batchIdx + 1) + "/" + totalBatches +
                    " (채널 " + (fromIndex + 1) + "~" + toIndex + ") ---");

            // 2a. 배치 내 채널들에 동시 연결
            List<ChzzkChat> activeChatList = new ArrayList<>();
            ExecutorService connectExecutor = Executors.newFixedThreadPool(POOL_SIZE);
            List<Future<ChzzkChat>> futures = new ArrayList<>();

            for (String channelId : batchChannels) {
                futures.add(connectExecutor.submit(() -> {
                    try {
                        ChzzkChat chat = (loginChzzk != null ? loginChzzk : chzzk).chat(channelId)
                                .withAutoReconnect(false)
                                .withChatListener(new ChatEventListener() {
                                    @Override
                                    public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                                        System.out.println("  [연결] 채널 " + chat.getChannelId() + " 연결 성공");
                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                        System.out.println("  [에러] " + ex.getMessage());
                                    }

                                    @Override
                                    public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                                        System.out.println("  [종료] 채널 " + channelId + " 연결 종료 (code=" + code + ")");
                                    }
                                })
                                .build();

                        chat.connectBlocking();
                        return chat;
                    } catch (Exception e) {
                        System.out.println("  [실패] 채널 " + channelId + " 연결 실패: " + e.getMessage());
                        return null;
                    }
                }));
            }

            // 연결 결과 수집
            for (Future<ChzzkChat> future : futures) {
                try {
                    ChzzkChat chat = future.get(30, TimeUnit.SECONDS);
                    if (chat != null) {
                        activeChatList.add(chat);
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    System.out.println("  [타임아웃] 연결 대기 초과: " + e.getMessage());
                }
            }

            connectExecutor.shutdown();
            try {
                if (!connectExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    connectExecutor.shutdownNow();
                }
            } catch (InterruptedException ignored) {
                connectExecutor.shutdownNow();
            }

            System.out.println("  연결 성공: " + activeChatList.size() + "/" + batchChannels.size());

            // 2b. 잠시 대기 (연결 유지 상태에서 스레드 확인)
            Thread.sleep(CONNECT_WAIT_MS);

            Set<String> midThreads = getChzzkThreadNames();
            System.out.println("  현재 chzzk 스레드 수: " + midThreads.size());

            // 2c. 모든 연결 종료
            System.out.println("  모든 연결 종료 시작...");
            for (ChzzkChat chat : activeChatList) {
                try {
                    chat.closeBlocking();
                } catch (Exception e) {
                    System.out.println("  [종료 실패] " + e.getMessage());
                }
            }

            // 종료 후 정리 대기
            Thread.sleep(CLEANUP_WAIT_MS);

            Set<String> afterBatchThreads = getChzzkThreadNames();
            System.out.println("  종료 후 chzzk 스레드 수: " + afterBatchThreads.size());
            if (!afterBatchThreads.isEmpty()) {
                afterBatchThreads.forEach(t -> System.out.println("    - " + t));
            }
            System.out.println();
        }

        // 3. 최종 스레드 확인
        System.out.println("=== 최종 결과 ===");
        System.out.println("연결 성공: " + successCount);
        System.out.println("연결 실패: " + failCount);
        System.out.println();

        // GC 유도 후 스레드 정리 대기
        System.gc();
        Thread.sleep(CLEANUP_WAIT_MS);

        Set<String> finalChzzkThreads = getChzzkThreadNames();
        int finalAllThreads = Thread.activeCount();

        System.out.println("[최종] chzzk 관련 스레드 수: " + finalChzzkThreads.size() + " (기준: " + baselineCount + ")");
        if (!finalChzzkThreads.isEmpty()) {
            finalChzzkThreads.forEach(t -> System.out.println("  - " + t));
        }
        System.out.println("[최종] 전체 활성 스레드 수: " + finalAllThreads + " (기준: " + baselineAllThreads + ")");
        System.out.println();

        // 4. 누수 검증
        // 누수된 chzzk 스레드가 기준선 대비 증가했는지 확인
        // WebSocket 관련 스레드 (chzzk-chat-ws, WebSocketReadThread, WebSocketWriteThread 등)
        Set<String> leakedThreads = new HashSet<>(finalChzzkThreads);
        leakedThreads.removeAll(baselineThreads);

        if (!leakedThreads.isEmpty()) {
            System.out.println("!!! 누수 의심 스레드 !!!");
            leakedThreads.forEach(t -> System.out.println("  - " + t));
        }

        // chzzk 관련 스레드가 기준선 + 허용치(2) 이하여야 함
        int allowedLeakage = 2; // OkHttp 커넥션 풀 등 약간의 여유
        assertTrue(
                finalChzzkThreads.size() <= baselineCount + allowedLeakage,
                "스레드 누수 감지! 기준: " + baselineCount +
                        ", 최종: " + finalChzzkThreads.size() +
                        ", 허용: " + allowedLeakage +
                        "\n누수 스레드: " + leakedThreads
        );

        System.out.println("=== 스레드 누수 테스트 통과 ===");
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

    private Set<String> getChzzkThreadNames() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(t -> {
                    String name = t.getName().toLowerCase();
                    return name.contains("chzzk") ||
                            name.contains("websocket") ||
                            name.contains("websocketread") ||
                            name.contains("websocketwrite");
                })
                .map(Thread::getName)
                .collect(Collectors.toSet());
    }
}
