import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.ChzzkBuilder;
import xyz.r2turntrue.chzzk4j.exception.NotLoggedInException;
import xyz.r2turntrue.chzzk4j.naver.Naver;
import xyz.r2turntrue.chzzk4j.util.Chrome;

import java.io.IOException;
import java.util.concurrent.CompletionException;

public class NaverLoginTest extends NaverTestBase {

    @Test
    public void testNaverLogin() {
        Assumptions.assumeTrue(hasNaverCredentials, "네이버 인증 정보가 없어 테스트를 건너뜁니다.");
        Assertions.assertDoesNotThrow(() -> {
            naver.login().thenRun(() -> {
                for (Naver.Cookie value : Naver.Cookie.values()) {
                    System.out.println(value.toString() + ": " + naver.getCookie(value));
                }
            }).join();
        });
    }

    @Test
    public void testNaverLoginFailed() {
        Assumptions.assumeTrue(hasNaverCredentials, "네이버 인증 정보가 없어 테스트를 건너뜁니다.");
        Assertions.assertThrowsExactly(CompletionException.class, () -> {
            Chrome.setDriverProperty("");
            naver.login().join();
        });
    }

    @Test
    public void testNaverLoginChzzk() {
        Assumptions.assumeTrue(hasNaverCredentials, "네이버 인증 정보가 없어 테스트를 건너뜁니다.");
        Assertions.assertDoesNotThrow(() -> {
            naver.login().thenRun(() -> {
                Chzzk chzzk = new ChzzkBuilder()
                        .withAuthorization(naver)
                        .build();

                try {
                    System.out.println(chzzk.getLoggedUser());
                } catch (IOException | NotLoggedInException e) {
                    throw new RuntimeException(e);
                }
            }).join();
        });
    }

}
