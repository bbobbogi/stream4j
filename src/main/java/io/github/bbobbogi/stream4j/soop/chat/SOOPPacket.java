package io.github.bbobbogi.stream4j.soop.chat;

import java.nio.charset.StandardCharsets;

public final class SOOPPacket {

    public static final String STARTER = "\u001b\t";
    public static final String SEPARATOR = "\f";

    public static final String TYPE_PING = "0000";
    public static final String TYPE_CONNECT = "0001";
    public static final String TYPE_JOIN = "0002";
    public static final String TYPE_CHAT = "0005";
    public static final String TYPE_DISCONNECT = "0007";
    public static final String TYPE_TEXT_DONATION = "0018";
    public static final String TYPE_AD_BALLOON = "0087";
    public static final String TYPE_STREAM_CLOSED = "0088";
    public static final String TYPE_SUBSCRIPTION_NEW = "0091";
    public static final String TYPE_SUBSCRIBE = "0093";
    public static final String TYPE_VIDEO_DONATION = "0105";
    public static final String TYPE_STATION_AD_BALLOON = "0107";
    public static final String TYPE_SUBSCRIPTION_GIFT = "0108";
    public static final String TYPE_EMOTICON = "0109";
    public static final String TYPE_MISSION = "0121";
    public static final String TYPE_VIEWER = "0127";

    private static final java.util.Set<String> IGNORED_TYPES = java.util.Set.of(
            "0004", // 열혈팬 입장/강퇴
            "0008", // 채금
            "0012", // 유저 입장
            "0013", // 매니저 임명/해임
            "0014", // 닉네임 변경
            "0019", // 얼리기
            "0020", // 얼리기 해제
            "0021", // 얼리기
            "0023", // 저속모드
            "0036", // 설정 변경
            "0045", // 별풍선 랭킹
            "0050", // 투표
            "0054", // 블록 단어 목록
            "0090", // 채팅 설정
            "0094", // 구독 모드 설정
            "0110", // 채널 상태 플래그
            "0111", // 퀵뷰/아이템
            "0058", // 시스템 공지
            "0104", // 채널 공지
            "0109", // 이모티콘
            "0118", // OGQ 스티커 선물
            "0119", // 광고
            "0125"  // 팬 랭킹
    );

    public static boolean isIgnored(String typeCode) {
        return IGNORED_TYPES.contains(typeCode);
    }

    private SOOPPacket() {
    }

    public static String buildPacket(String typeCode, String payload) {
        String safePayload = payload == null ? "" : payload;
        String length = String.format("%06d", safePayload.getBytes(StandardCharsets.UTF_8).length);
        return STARTER + typeCode + length + "00" + safePayload;
    }

    public static String buildConnectPacket() {
        String payload = SEPARATOR.repeat(3) + "16" + SEPARATOR;
        return buildPacket(TYPE_CONNECT, payload);
    }

    public static String buildJoinPacket(String chatNo) {
        String payload = SEPARATOR + (chatNo == null ? "" : chatNo) + SEPARATOR.repeat(5);
        return buildPacket(TYPE_JOIN, payload);
    }

    public static String buildPingPacket() {
        return buildPacket(TYPE_PING, SEPARATOR);
    }

    public static String parseTypeCode(String packet) {
        if (packet == null || packet.length() < 6 || !packet.startsWith(STARTER)) {
            return null;
        }
        return packet.substring(2, 6);
    }

    public static String[] splitPayload(String packet) {
        return packet == null ? new String[0] : packet.split(SEPARATOR, -1);
    }
}
