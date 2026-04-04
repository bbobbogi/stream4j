# 인증 가이드

## 네이버 로그인 (치지직)

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

### 수동 인증

ChromeDriver 없이 직접 인증 토큰을 전달할 수도 있습니다.

```java
Chzzk chzzk = new ChzzkBuilder()
        .withAuthorization(NID_AUT, NID_SES)
        .build();
```

## SOOP 로그인

SOOP은 로그인 없이도 기본 기능을 사용할 수 있지만, 일부 기능은 로그인이 필요합니다.

### 개별 API

```java
SOOPChat chat = new SOOPChatBuilder("https://play.sooplive.co.kr/...")
        .withCredentials("아이디", "비밀번호")
        .build();
```

### 통합 API

```java
new StreamChatBuilder()
        .add("https://play.sooplive.co.kr/...")
        .withSoopCredentials("아이디", "비밀번호")
        .build();
```
