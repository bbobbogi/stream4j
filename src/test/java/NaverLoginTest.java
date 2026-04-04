import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.github.bbobbogi.stream4j.chzzk.Chzzk;
import io.github.bbobbogi.stream4j.chzzk.ChzzkBuilder;
import io.github.bbobbogi.stream4j.chzzk.exception.NotLoggedInException;
import io.github.bbobbogi.stream4j.chzzk.naver.Naver;
import io.github.bbobbogi.stream4j.util.Chrome;

import java.io.IOException;
import java.util.concurrent.CompletionException;

public class NaverLoginTest extends NaverTestBase {

    @Test
    public void testNaverLogin() {
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
        Assertions.assertThrowsExactly(CompletionException.class, () -> {
            Chrome.setDriverProperty("");
            naver.login().join();
        });
    }

    @Test
    public void testNaverLoginChzzk() {
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
