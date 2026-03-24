package com.bbobbogi.stream4j.youtube;

import com.bbobbogi.stream4j.youtube.chat.ChatItem;

public interface YouTubeChatEventListener {

    default void onConnect(YouTubeChat chat, boolean isReconnecting) {}

    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    default void onBroadcastEnd(YouTubeChat chat) {}

    default void onError(Exception ex) { ex.printStackTrace(); }

    default void onChat(ChatItem item) {}

    default void onSuperChat(ChatItem item) {}

    default void onSuperSticker(ChatItem item) {}

    default void onNewMember(ChatItem item) {}
}
