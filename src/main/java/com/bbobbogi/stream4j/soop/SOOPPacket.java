package com.bbobbogi.stream4j.soop;

import java.nio.charset.StandardCharsets;

public final class SOOPPacket {

    static final String STARTER = "\u001b\t";
    static final String SEPARATOR = "\f";

    static final String TYPE_PING = "0000";
    static final String TYPE_CONNECT = "0001";
    static final String TYPE_JOIN = "0002";
    static final String TYPE_CHAT = "0005";
    static final String TYPE_DISCONNECT = "0007";
    static final String TYPE_TEXT_DONATION = "0018";
    static final String TYPE_AD_BALLOON = "0087";
    static final String TYPE_SUBSCRIBE = "0093";
    static final String TYPE_NOTIFICATION = "0104";
    static final String TYPE_VIDEO_DONATION = "0105";
    static final String TYPE_EMOTICON = "0109";
    static final String TYPE_VIEWER = "0127";

    private SOOPPacket() {
    }

    static String buildPacket(String typeCode, String payload) {
        String safePayload = payload == null ? "" : payload;
        String length = String.format("%06d", safePayload.getBytes(StandardCharsets.UTF_8).length);
        return STARTER + typeCode + length + "00" + safePayload;
    }

    static String buildConnectPacket() {
        String payload = SEPARATOR.repeat(3) + "16" + SEPARATOR;
        return buildPacket(TYPE_CONNECT, payload);
    }

    static String buildJoinPacket(String chatNo) {
        String payload = SEPARATOR + (chatNo == null ? "" : chatNo) + SEPARATOR.repeat(5);
        return buildPacket(TYPE_JOIN, payload);
    }

    static String buildPingPacket() {
        return buildPacket(TYPE_PING, SEPARATOR);
    }

    static String parseTypeCode(String packet) {
        if (packet == null || packet.length() < 6 || !packet.startsWith(STARTER)) {
            return null;
        }
        return packet.substring(2, 6);
    }

    static String[] splitPayload(String packet) {
        return packet == null ? new String[0] : packet.split(SEPARATOR, -1);
    }
}
