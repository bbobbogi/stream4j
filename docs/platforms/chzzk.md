# 치지직 (Chzzk)

## 채팅 연결

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

## 채널 API

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

## 인증

치지직은 네이버 계정 인증이 선택적으로 필요합니다. 자세한 내용은 [인증 가이드](../auth.md)를 참고하세요.
