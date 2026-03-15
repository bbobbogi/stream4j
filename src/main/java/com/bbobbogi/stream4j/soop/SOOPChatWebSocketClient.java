package com.bbobbogi.stream4j.soop;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SOOPChatWebSocketClient extends WebSocketClient {

    private final SOOPChat chat;
    private final ScheduledExecutorService pingScheduler;

    SOOPChatWebSocketClient(SOOPChat chat, URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders, 10000);
        setConnectionLostTimeout(0);
        this.chat = chat;
        this.pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "soop-ping");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(SOOPPacket.buildConnectPacket());
    }

    @Override
    public void onMessage(String message) {
        chat.handleMessage(message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] data = new byte[bytes.remaining()];
        bytes.get(data);
        chat.handleMessage(new String(data, StandardCharsets.UTF_8));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        pingScheduler.shutdownNow();
        chat.handleClose(reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        chat.handleError(ex);
    }

    void stopPing() {
        pingScheduler.shutdownNow();
    }

    void startPing() {
        pingScheduler.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                send(SOOPPacket.buildPingPacket());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    void sendPacket(String packet) {
        if (isOpen()) {
            send(packet);
        }
    }
}
