
import xyz.r2turntrue.chzzk4j.naver.Naver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class NaverTestBase {

    protected Properties properties = new Properties();
    protected Naver naver;
    protected boolean hasNaverCredentials = false;

    public NaverTestBase() {
        File envFile = new File("env.properties");
        if (envFile.exists()) {
            try {
                properties.load(new FileInputStream(envFile));
            } catch (IOException e) {
                System.out.println("env.properties 로드 실패: " + e.getMessage());
            }
        } else {
            System.out.println("env.properties 파일이 없습니다. 네이버 로그인 테스트를 건너뜁니다.");
        }

        String naverId = properties.getProperty("NAVER_ID");
        String naverPw = properties.getProperty("NAVER_PW");

        if (naverId != null && !naverId.isEmpty() && naverPw != null && !naverPw.isEmpty()) {
            this.naver = new Naver(naverId, naverPw);
            hasNaverCredentials = true;
        }
    }

}
