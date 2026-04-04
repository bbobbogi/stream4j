# YouTube

## 채팅 연결

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

## 폴링 간격

YouTube는 WebSocket이 아닌 폴링 방식으로 채팅을 가져옵니다. 기본 간격은 5000ms(5초)이며, 필요에 따라 조정할 수 있습니다.

```java
// 개별 API
.withPollInterval(3000)

// 통합 API
.withYouTubePollInterval(3000)
```
