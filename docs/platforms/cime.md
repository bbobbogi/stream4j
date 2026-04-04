# CiMe

## 채팅 연결

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

## 지원 이벤트

- `DONATION_CHAT` — 채팅 후원
- `DONATION_VIDEO` — 영상 후원
- `DONATION_MISSION` — 미션 후원
- `SUBSCRIPTION_MESSAGE` — 구독 메시지
- `LIVE_ENDED` — 방송 종료
