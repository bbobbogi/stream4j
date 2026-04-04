# SOOP (구 아프리카TV)

## 채팅 연결

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

## 인증

SOOP은 로그인 없이도 사용할 수 있지만, 일부 기능은 로그인이 필요합니다.

```java
.withCredentials("아이디", "비밀번호")
```

통합 API에서는 아래와 같이 설정합니다.

```java
new StreamChatBuilder()
        .add("https://play.sooplive.co.kr/...")
        .withSoopCredentials("아이디", "비밀번호")
        .build();
```
