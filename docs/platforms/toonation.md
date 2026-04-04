# 투네이션 (Toonation)

## 후원 연결

투네이션은 **후원 알림 전용**입니다. 채팅 기능은 지원하지 않습니다.

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

## 참고사항

- 투네이션은 alertbox 위젯 기반으로 동작하며, 방송 상태와 독립적입니다.
- 방송 종료 감지 기능은 지원하지 않습니다.
- idle timeout은 60초입니다.
