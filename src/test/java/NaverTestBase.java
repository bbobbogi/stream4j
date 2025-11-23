import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import xyz.r2turntrue.chzzk4j.naver.Naver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class NaverTestBase {

    protected static Properties properties = new Properties();
    protected static String naverId;
    protected static String naverPw;
    protected static boolean hasNaverCredentials = false;

    protected Naver naver;

    @BeforeAll
    public static void setupOnce() {
        try {
            File envFile = new File("env.properties");
            if (envFile.exists()) {
                try (FileInputStream fis = new FileInputStream(envFile)) {
                    properties.load(fis);
                } catch (IOException e) {
                    System.out.println("env.properties 로드 실패: " + e.getMessage());
                }
            } else {
                System.out.println("env.properties 파일이 없습니다. 네이버 로그인 테스트를 건너뜁니다.");
            }

            naverId = properties.getProperty("NAVER_ID");
            naverPw = properties.getProperty("NAVER_PW");

            if (naverId != null && !naverId.isEmpty() && naverPw != null && !naverPw.isEmpty()) {
                hasNaverCredentials = true;
            }
        } catch (Exception e) {
            System.out.println("테스트 초기화 실패: " + e.getMessage());
            hasNaverCredentials = false;
        }

        // Skip all tests in this class if credentials are not available
        Assumptions.assumeTrue(hasNaverCredentials, "네이버 인증 정보가 없어 테스트를 건너뜁니다.");
    }

    @BeforeEach
    public void setup() {
        // Create a fresh Naver instance for each test to ensure isolation
        naver = new Naver(naverId, naverPw);
    }

}
