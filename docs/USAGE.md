# 상세 사용 가이드

## 통합 API (StreamChat)

### URL로 연결하기

URL만 넣으면 플랫폼을 자동 감지합니다.

```java
StreamChat chat = new StreamChatBuilder()
        .add("https://chzzk.naver.com/live/924a636224c9203259af46ad7d8b70ca")
        .add("https://ci.me/@lyn")
        .add("https://play.sooplive.co.kr/tjrdbs999/292536969")
        .add("https://www.youtube.com/watch?v=Qv6o6WACJ60")
        .add("https://toon.at/widget/alertbox/abc123")
        .withListener(listener)
        .build();

chat.connectAll();
```

### Raw ID로 연결하기

URL 대신 플랫폼 ID를 직접 지정할 수도 있습니다.

```java
new StreamChatBuilder()
        .add("924a636224c9203259af46ad7d8b70ca", DonationPlatform.CHZZK)
        .add("tjrdbs999", DonationPlatform.SOOP)
        .add("@lyn", DonationPlatform.CIME)
        .add("alertbox_key", DonationPlatform.TOONATION)
        .add("@jtbc_news", DonationPlatform.YOUTUBE)
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

### 설정 옵션

```java
new StreamChatBuilder()
        .add("https://...")
        .withSoopCredentials("아이디", "비밀번호")    // SOOP 로그인 (선택)
        .withYouTubePollInterval(3000)             // YouTube 폴링 간격 (선택, 기본값 5000ms)
        .withAutoReconnect(true)                   // 자동 재연결 (기본값 false)
        .withDebugMode()                           // 디버그 로그 출력
        .withListener(listener)
        .build();
```

---

## 후원 정보 (Donation)

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

### 후원 타입 (DonationType)

| 타입 | 설명 |
|---|---|
| `CHAT` | 일반 채팅 후원 (치즈, 별풍선 등) |
| `VIDEO` | 영상 후원 |
| `MISSION` | 미션 후원 |
| `PARTY` | 파티 후원 |
| `SUBSCRIPTION` | 구독 |
| `SUBSCRIPTION_GIFT` | 구독 선물 |

### 후원 상태 (DonationStatus)

| 상태 | 설명 |
|---|---|
| `SUCCESS` | 후원 완료 |
| `PENDING` | 대기 중 |
| `APPROVED` | 승인됨 |
| `REJECTED` | 거절됨 |
| `EXPIRED` | 만료됨 |

---

## 기술 상세

### 연결 안정성
- **OkHttp ManagedWebSocket** 기반 WebSocket 관리 (closing 플래그, 지수 백오프 재시도)
- **idle timeout 감지** — 메시지 없이 유휴 상태가 지속되면 자동 감지 및 재연결
- **NonRetryableException** — 구독자 전용, 성인 인증 등 비복구성 에러는 재시도 없이 즉시 중단

### 플랫폼별 방송 종료 감지

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
