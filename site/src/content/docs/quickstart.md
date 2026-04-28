---
title: 설치 및 빠른 시작
description: stream4j를 Maven Central에서 받아 5분 안에 첫 통합 채팅을 받아보는 가이드입니다.
---

## 설치

Maven Central에 배포되어 있습니다.

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bbobbogi:stream4j:1.0.1-SNAPSHOT")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.bbobbogi</groupId>
    <artifactId>stream4j</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

> **요구사항**: Java 11 이상

## 빠른 시작

URL만 넣으면 바로 동작합니다. 아래 코드를 실행하면 5개 플랫폼의 채팅과 후원이 동일한 콜백으로 들어옵니다.

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
            public void onChat(DonationPlatform platform, String channelId,
                               String nickname, String message) {
                System.out.println("[" + platform + "] " + nickname + ": " + message);
            }
        })
        .build();

chat.connectAll();
```

## 다음 단계

- [통합 사용 가이드](/usage/) — Raw ID, 설정 옵션, Donation 모델 상세
- [플랫폼별 가이드](/platforms/chzzk/) — 플랫폼별 개별 API 사용법
