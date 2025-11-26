package xyz.r2turntrue.chzzk4j.naver;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import xyz.r2turntrue.chzzk4j.util.Chrome;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 네이버 로그인을 처리하는 클래스입니다.
 * Selenium을 사용하여 네이버에 로그인하고 쿠키를 획득합니다.
 */
public class Naver {

    private final @NotNull String id;
    private final @NotNull String password;
    private final @NotNull Map<Cookie, String> cookies;

    /**
     * Log in to Naver using ID and password.
     * @param id naver id
     * @param password password of the naver id
     */
    public Naver(@NotNull String id, @NotNull String password) {
        this.id = id;
        this.password = password;
        this.cookies = Maps.newConcurrentMap();
    }

    /**
     * 네이버에 ID와 비밀번호로 로그인합니다.
     *
     * @return 로그인 완료 후 반환되는 CompletableFuture
     */
    public CompletableFuture<Void> login() {
        return CompletableFuture.runAsync(() -> {
            WebDriver driver = Chrome.getDriver();
            driver.get("https://nid.naver.com/nidlogin.login");
            try {
                // Write id and pw fields
                if (driver instanceof JavascriptExecutor js) {
                    js.executeScript(String.format("document.getElementById('id').value='%s';", id));
                    js.executeScript(String.format("document.getElementById('pw').value='%s';", password));
                }

                // Click login button
                WebElement loginBtn = driver.findElement(By.id("log.login"));
                loginBtn.click();

                // Find cookies
                cookies.clear();
                for (Cookie key : Cookie.values()) {
                    org.openqa.selenium.Cookie cookie = driver.manage().getCookieNamed(key.toString());
                    if (cookie != null) {
                        cookies.put(key, cookie.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.quit();
            }
        });
    }

    /**
     * 네이버 ID를 반환합니다.
     *
     * @return 네이버 ID
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * 네이버 비밀번호를 반환합니다.
     *
     * @return 네이버 비밀번호
     */
    public @NotNull String getPassword() {
        return password;
    }

    /**
     * 네이버 로그인 후 쿠키 값을 반환합니다.
     * 로그인하지 않은 경우 빈 문자열을 반환합니다.
     *
     * @param key {@link Cookie} 쿠키 키
     * @return 쿠키 값 (로그인하지 않은 경우 빈 문자열)
     */
    public @NotNull String getCookie(@NotNull Naver.Cookie key) {
        return cookies.getOrDefault(key, "");
    }

    /**
     * 네이버 쿠키 타입을 나타내는 열거형입니다.
     */
    public enum Cookie {
        /**
         * 네이버 인증 쿠키
         */
        NID_AUT,

        /**
         * 네이버 세션 쿠키
         */
        NID_SES
    }

}
