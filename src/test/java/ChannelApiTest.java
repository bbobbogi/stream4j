import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.exception.ChannelNotExistsException;
import xyz.r2turntrue.chzzk4j.exception.NotExistsException;
import xyz.r2turntrue.chzzk4j.exception.NotLoggedInException;
import xyz.r2turntrue.chzzk4j.types.ChzzkUser;
import xyz.r2turntrue.chzzk4j.types.channel.ChzzkChannel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import xyz.r2turntrue.chzzk4j.types.channel.ChzzkChannelFollowingData;
import xyz.r2turntrue.chzzk4j.types.channel.ChzzkChannelRules;
import xyz.r2turntrue.chzzk4j.types.channel.recommendation.ChzzkRecommendationChannels;

// FOLLOWED_CHANNEL_1, FOLLOWED_CHANNEL_2 채널을 팔로우한 뒤 테스트 진행해주세요.
// UNFOLLOWED_CHANNEL 채널은 팔로우 해제 후 테스트 진행해주세요.
// 채널 ID는 env.properties에서 설정합니다.
public class ChannelApiTest extends ChzzkTestBase {
    // 기본 테스트용 채널 ID (유명 스트리머 채널)
    private static final String DEFAULT_TEST_CHANNEL = "8a59b34b46271960c1bf172bb0fac758";

    // env.properties에서 가져오거나 기본값 사용
    public String getFollowedChannel1() {
        return properties.getProperty("FOLLOWED_CHANNEL_1", DEFAULT_TEST_CHANNEL);
    }

    public String getFollowedChannel2() {
        return properties.getProperty("FOLLOWED_CHANNEL_2", DEFAULT_TEST_CHANNEL);
    }

    public String getUnfollowedChannel() {
        return properties.getProperty("UNFOLLOWED_CHANNEL", DEFAULT_TEST_CHANNEL);
    }

    @Test
    void gettingNormalChannelInfo() throws IOException {
        AtomicReference<ChzzkChannel> channel = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() ->
                channel.set(chzzk.getChannel(getFollowedChannel2())));

        System.out.println(channel);
    }

    @Test
    void gettingInvalidChannelInfo() throws IOException {
        Assertions.assertThrowsExactly(ChannelNotExistsException.class, () -> {
            chzzk.getChannel("invalidchannelid");
        });
    }

    @Test
    void gettingNormalChannelRules() throws IOException {
        AtomicReference<ChzzkChannelRules> rule = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() ->
                rule.set(chzzk.getChannelChatRules(getFollowedChannel1())));

        System.out.println(rule);
    }

    @Test
    void gettingInvalidChannelRules() throws IOException {
        Assertions.assertThrowsExactly(NotExistsException.class, () -> {
            chzzk.getChannelChatRules("invalidchannel or no rule channel");
        });
    }

    @Test
    void gettingFollowStatusAnonymous() throws IOException {
        Assertions.assertThrowsExactly(NotLoggedInException.class, () ->
                chzzk.getFollowingStatus(getFollowedChannel1()));
    }

    @Test
    void gettingFollowStatus() throws IOException {
        // 로그인이 필요한 테스트
        Assumptions.assumeTrue(hasCredentials, "인증 정보가 없어 테스트를 건너뜁니다.");

        AtomicReference<ChzzkChannelFollowingData> followingStatus = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() ->
                followingStatus.set(loginChzzk.getFollowingStatus(getFollowedChannel1())));

        System.out.println(followingStatus);

        Assertions.assertEquals(followingStatus.get().isFollowing(), true);

        Assertions.assertDoesNotThrow(() ->
                followingStatus.set(loginChzzk.getFollowingStatus(getUnfollowedChannel())));

        System.out.println(followingStatus);

        Assertions.assertEquals(followingStatus.get().isFollowing(), false);
    }

    @Test
    void gettingUserInfo() throws IOException, NotLoggedInException {
        // 로그인이 필요한 테스트
        Assumptions.assumeTrue(hasCredentials, "인증 정보가 없어 테스트를 건너뜁니다.");

        ChzzkUser currentUser = loginChzzk.getLoggedUser();
        System.out.println(currentUser);
        Assertions.assertEquals(currentUser.getUserId(), currentUserId);
    }

    @Test
    void gettingRecommendationChannels() throws IOException {
        // 익명으로도 가능
        ChzzkRecommendationChannels channels = chzzk.getRecommendationChannels();
        System.out.println(channels);
    }
}
