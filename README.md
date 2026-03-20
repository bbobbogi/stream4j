# stream4j

> **Note**: 이 저장소는 [원본 chzzk4j](https://github.com/R2turnTrue/chzzk4j)의 비공식 포크입니다.

치지직, CiMe, SOOP, 투네이션, YouTube 5개 플랫폼을 지원하는 Java 스트리밍 채팅/후원 라이브러리입니다.

## 설치

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bbobbogi:chzzk4j:0.0.12")
}
```

## 빠른 시작

### 통합 API (StreamChat)

URL만 넣으면 플랫폼을 자동 감지합니다.

```java
StreamChat chat = new StreamChatBuilder()
        .add("https://chzzk.naver.com/live/924a636224c9203259af46ad7d8b70ca")
        .add("https://ci.me/@lyn")
        .add("https://play.sooplive.co.kr/tjrdbs999/292536969")
        .add("https://www.youtube.com/watch?v=Qv6o6WACJ60")
        .add("https://toon.at/widget/alertbox/abc123")
        .withListener(new StreamChatEventListener() {
            @Override
            public void onDonation(Donation donation) {
                System.out.println("[" + donation.platform() + "] "
                        + donation.nickname() + ": "
                        + donation.formattedAmount() + " - "
                        + donation.message());
            }

            @Override
            public void onChat(DonationPlatform platform, String channelId, String nickname, String message) {
                System.out.println("[" + platform + "] " + nickname + ": " + message);
            }
        })
        .build();

chat.connectAll();
```

raw ID는 플랫폼 타입 힌트와 함께 사용합니다.

```java
new StreamChatBuilder()
        .add("924a636224c9203259af46ad7d8b70ca", DonationPlatform.CHZZK)
        .add("tjrdbs999", DonationPlatform.SOOP)
        .add("@lyn", DonationPlatform.CIME)
        .add("alertbox_key", DonationPlatform.TOONATION)
        .add("@jtbc_news", DonationPlatform.YOUTUBE)
        .withSoopCredentials("아이디", "비밀번호")            // SOOP 로그인 (선택)
        .withYouTubePollInterval(3000)                     // YouTube 폴링 간격 (선택)
        .withAutoReconnect(true)
        .withDebugMode()
        .withListener(listener)
        .build();
```

### 지원하는 URL 형식

| 플랫폼 | URL 형식 |
|---|---|
| **치지직** | `https://chzzk.naver.com/live/{channelId}`, `https://chzzk.naver.com/{channelId}`, `https://m.chzzk.naver.com/...` |
| **CiMe** | `https://ci.me/@{slug}/live`, `https://ci.me/@{slug}`, `@slug` |
| **SOOP** | `https://play.sooplive.co.kr/{userId}/...`, `https://www.sooplive.co.kr/station/{userId}` |
| **YouTube** | `https://www.youtube.com/watch?v={videoId}`, `https://www.youtube.com/@{username}`, `@username` |
| **투네이션** | `https://toon.at/widget/alertbox/{key}` |

### 후원 정보 (Donation)

모든 플랫폼의 후원이 `Donation` record로 통합됩니다.

```java
donation.platform()        // CHZZK, CIME, SOOP, TOONATION, YOUTUBE
donation.type()            // CHAT, VIDEO, MISSION, PARTY, SUBSCRIPTION, SUBSCRIPTION_GIFT
donation.status()          // SUCCESS, PENDING, APPROVED, REJECTED, EXPIRED, ...
donation.nickname()        // 후원자 닉네임
donation.message()         // 후원 메시지
donation.anonymous()       // 익명 여부
donation.amount()          // 금액 (int)
donation.currencyCode()    // CHEESE, BEAM, BALLOON, KRW, USD, ...
donation.amountInKRW()     // 원화 환산 금액
donation.formattedAmount() // "1000치즈 (1000원)", "₩5,000" 등
donation.raw()             // 플랫폼별 원본 객체
```

---

## 개별 플랫폼 API

### 치지직 (Chzzk)

```java
Chzzk chzzk = new ChzzkBuilder()
        .withAuthorization(NID_AUT, NID_SES) // 선택
        .build();

ChzzkChat chat = chzzk.chat("https://chzzk.naver.com/live/924a636224c9203259af46ad7d8b70ca")
        .withChatListener(new ChatEventListener() {
            @Override
            public void onChat(ChatMessage msg) {
                System.out.println(msg.getProfile().getNickname() + ": " + msg.getContent());
            }

            @Override
            public void onDonationChat(DonationMessage msg) {
                System.out.println("[후원] " + msg.getPayAmount() + "치즈 - " + msg.getContent());
            }

            @Override
            public void onMissionDonation(MissionDonationMessage msg) {
                System.out.println("[미션] " + msg.getMissionText() + " | 상태: " + msg.getMissionStatusRaw());
            }

            @Override
            public void onPartyDonationChat(PartyDonationMessage msg) {
                System.out.println("[파티] " + msg.getPartyName() + " - " + msg.getContent());
            }

            @Override
            public void onSubscriptionChat(SubscriptionMessage msg) {
                System.out.println("[구독] " + msg.getSubscriptionMonth() + "개월");
            }
        })
        .withAutoReconnect(true)
        .withDebugMode()
        .build();

chat.connectBlocking();
```

### CiMe

```java
CiMeChat chat = new CiMeChatBuilder("https://ci.me/@lyn")
        .withChatListener(new CiMeChatEventListener() {
            @Override
            public void onChat(CiMeChatMessage msg) {
                System.out.println(msg.getUser().getNickname() + ": " + msg.getContent());
            }

            @Override
            public void onEvent(String eventType, String rawJson) {
                // DONATION_CHAT, DONATION_VIDEO, DONATION_MISSION,
                // SUBSCRIPTION_MESSAGE, LIVE_ENDED 등
                System.out.println("[이벤트] " + eventType);
            }
        })
        .withAutoReconnect(true)
        .build();

chat.connectBlocking();
```

### SOOP (구 아프리카TV)

```java
SOOPChat chat = new SOOPChatBuilder("https://play.sooplive.co.kr/tjrdbs999/292536969")
        .withChatListener(new SOOPChatEventListener() {
            @Override
            public void onChat(String userId, String username, String message) {
                System.out.println(username + ": " + message);
            }

            @Override
            public void onDonation(SOOPChat c, SOOPDonationMessage msg) {
                System.out.println("[별풍선] " + msg.getFromUsername() + ": " + msg.getAmount() + "개");
            }

            @Override
            public void onBroadcastEnd(SOOPChat c) {
                System.out.println("방송 종료");
            }
        })
        .withCredentials("아이디", "비밀번호") // 선택
        .withAutoReconnect(true)
        .build();

chat.connectBlocking();
```

### YouTube

```java
YouTubeChat chat = new YouTubeChatBuilder("https://www.youtube.com/watch?v=Qv6o6WACJ60")
        .withChatListener(new YouTubeChatEventListener() {
            @Override
            public void onChat(ChatItem item) {
                System.out.println(item.getAuthorName() + ": " + item.getMessage());
            }

            @Override
            public void onSuperChat(ChatItem item) {
                System.out.println("[SuperChat] " + item.getAuthorName() + ": " + item.getPurchaseAmount());
            }

            @Override
            public void onBroadcastEnd(YouTubeChat chat) {
                System.out.println("방송 종료");
            }
        })
        .withPollInterval(5000)
        .withAutoReconnect(true)
        .build();

chat.connectBlocking();
```

### 투네이션 (Toonation)

```java
ToonationChat chat = new ToonationChatBuilder("https://toon.at/widget/alertbox/abc123")
        .withChatListener(new ToonationChatEventListener() {
            @Override
            public void onDonation(ToonationChat c, ToonationDonationMessage msg) {
                System.out.println("[후원] " + msg.getNickname() + ": " + msg.getAmount() + "원");
            }
        })
        .withAutoReconnect(true)
        .build();

chat.connectBlocking();
```

---

## 주요 특징

### 연결 안정성
- **OkHttp ManagedWebSocket** 기반 WebSocket 관리 (closing 플래그, 지수 백오프 재시도)
- **idle timeout 감지** — 메시지 없이 유휴 상태가 지속되면 자동 감지 및 재연결
- **NonRetryableException** — 구독자 전용, 성인 인증 등 비복구성 에러는 재시도 없이 즉시 중단

### 플랫폼별 방송종료 감지
| 플랫폼 | 감지 방식 |
|---|---|
| 치지직 | 30초 폴링으로 방송 상태 확인 |
| CiMe | `LIVE_ENDED` WebSocket 이벤트 |
| SOOP | `svc_SETBJSTAT=0` 패킷 |
| YouTube | `invalidationContinuationData` 부재 + `getBroadcastInfo()` 검증 |
| 투네이션 | alertbox 기반 (방송 독립), idle timeout 60초 |

### CiMe 토큰 자동 갱신
- JWT 토큰 만료 5분 전부터 자동 갱신 시도 (최대 3회)
- 새 연결 먼저 열고 구 연결 닫기 (메시지 유실 방지)
- 메시지 ID 기반 중복 제거

### 후원 통합 (Donation)
- 5개 플랫폼의 후원을 단일 `Donation` record로 통합
- `DonationType`: CHAT, VIDEO, MISSION, PARTY, SUBSCRIPTION, SUBSCRIPTION_GIFT
- `DonationStatus`: SUCCESS, PENDING, APPROVED, REJECTED, EXPIRED 등
- `CurrencyUtils`: 치즈/빔/별풍선/원/USD 등 통화 변환 및 원화 환산

---

## 치지직 API

```java
// 채널 정보
ChzzkChannel channel = chzzk.getChannel("channelId");
System.out.println(channel.getChannelName());

// 채팅 규칙
ChzzkChannelRules rules = chzzk.getChannelChatRules("channelId");

// 방송 상태
chzzk.getLiveStatus("channelId");
chzzk.getLiveDetail("channelId");
```

## 네이버 로그인

Selenium + ChromeDriver를 통한 네이버 로그인을 지원합니다.

> ⚠️ **주의**: 네이버 2차 인증(2단계 인증)이 활성화되어 있으면 자동 로그인이 실패합니다. 사용 전 [네이버 내 정보](https://nid.naver.com/user2/help/myInfo)에서 2단계 인증을 비활성화해 주세요.

```java
Chrome.setDriverProperty("ChromeDriver 경로");

Naver naver = new Naver("네이버 ID", "비밀번호");
naver.login().thenRun(() -> {
    Chzzk chzzk = new ChzzkBuilder()
            .withAuthorization(naver)
            .build();
}).join();
```
