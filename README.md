# stream4j

치지직, CiMe, SOOP, YouTube, 투네이션 — 5개 스트리밍 플랫폼의 **채팅과 후원을 하나의 API로** 다루는 Java 라이브러리입니다.

> 이 프로젝트는 [chzzk4j](https://github.com/R2turnTrue/chzzk4j)에서 출발하여, 치지직 외 4개 플랫폼(CiMe, SOOP, YouTube, 투네이션)을 추가 지원하도록 확장한 라이브러리입니다.

## 왜 필요한가요?

스트리밍 플랫폼마다 채팅 연결 방식, 후원 이벤트 구조, 인증 방법이 모두 다릅니다.
여러 플랫폼을 동시에 운영하려면 각각의 API를 따로 연동해야 하는데, stream4j는 이 과정을 **하나의 통합 API**로 해결합니다.

- 플랫폼별 URL을 넣으면 **자동으로 플랫폼을 감지**합니다
- 채팅 메시지와 후원 이벤트를 **공통 형식**으로 받을 수 있습니다
- 연결이 끊어지면 **자동으로 재연결**합니다

## 이런 분께 적합합니다

- 멀티플랫폼 **통합 채팅 뷰어**를 만들고 싶은 개발자
- 여러 플랫폼의 **후원 알림 봇**을 구축하려는 팀
- 스트리밍 관련 **자동화 도구**를 기획하는 PM
- 다중 플랫폼 방송 운영에 필요한 기술을 **검토하는 분**

## 지원 플랫폼

| 플랫폼 | 채팅 | 후원 | 방송 종료 감지 | 인증 필요 |
|---|:---:|:---:|:---:|:---:|
| **치지직** (Chzzk) | ✅ | ✅ | ✅ | 선택 |
| **CiMe** | ✅ | ✅ | ✅ | 불필요 |
| **SOOP** (구 아프리카TV) | ✅ | ✅ | ✅ | 선택 |
| **YouTube** | ✅ | ✅ | ✅ | 불필요 |
| **투네이션** (Toonation) | — | ✅ | — | 불필요 |

## 빠른 시작

URL만 넣으면 바로 동작합니다.

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

## 설치

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.bbobbogi:stream4j:0.2.0-SNAPSHOT")
}
```

## 핵심 기능

- **플랫폼 자동 감지** — URL을 넣으면 어떤 플랫폼인지 자동으로 판별합니다
- **통합 후원 모델** — 치즈, 별풍선, 슈퍼챗 등 플랫폼별 후원을 하나의 형식으로 통합합니다
- **안정적인 연결 유지** — 장시간 연결을 안정적으로 유지하고, 끊어지면 자동으로 재연결합니다
- **방송 종료 감지** — 각 플랫폼에 맞는 방식으로 방송 종료를 자동 감지합니다
- **원화 환산** — 각 플랫폼의 후원 단위(치즈, 빔, 별풍선 등)를 원화로 환산할 수 있습니다

## 대표 사용 사례

- 🖥️ 여러 플랫폼의 채팅을 한 화면에 모아보는 **통합 채팅 뷰어**
- 🔔 후원이 들어오면 알려주는 **멀티플랫폼 후원 알림 봇**
- 📊 채팅/후원 데이터를 수집·분석하는 **방송 관리 대시보드**

## 제한사항

- **Java 11 이상**이 필요합니다
- 일부 플랫폼(치지직, SOOP)은 **로그인 정보가 있어야** 모든 기능을 사용할 수 있습니다
- 각 플랫폼의 **정책 변경에 따라** 일부 기능이 영향을 받을 수 있습니다
- 투네이션은 후원 알림 전용으로, 채팅 기능은 지원하지 않습니다

## 상세 문서

더 자세한 사용법은 아래 문서를 참고하세요.

- [상세 사용 가이드](docs/USAGE.md) — Raw ID 사용법, 후원 데이터 상세, 설정 옵션
- [치지직 (Chzzk)](docs/platforms/chzzk.md)
- [CiMe](docs/platforms/cime.md)
- [SOOP](docs/platforms/soop.md)
- [YouTube](docs/platforms/youtube.md)
- [투네이션](docs/platforms/toonation.md)
- [인증 가이드](docs/auth.md) — 네이버 로그인, SOOP 로그인

## 라이선스

이 프로젝트는 [MIT License](LICENSE)로 배포됩니다.
