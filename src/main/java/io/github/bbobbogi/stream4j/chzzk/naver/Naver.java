package io.github.bbobbogi.stream4j.chzzk.naver;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import io.github.bbobbogi.stream4j.util.Chrome;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Class for handling NAVER login.
 * Logs in to NAVER with Selenium and retrieves cookies.
 *
 * @since 1.0.0
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
     * Logs in to NAVER with ID and password.
     *
     * @return {@link CompletableFuture} completed after login
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
     * Returns the NAVER ID.
     *
     * @return NAVER ID
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Returns the NAVER password.
     *
     * @return NAVER password
     */
    public @NotNull String getPassword() {
        return password;
    }

    /**
     * Returns the cookie value after NAVER login.
     * Returns an empty string if not logged in.
     *
     * @param key {@link Cookie} key
     * @return cookie value (empty string if not logged in)
     */
    public @NotNull String getCookie(@NotNull Naver.Cookie key) {
        return cookies.getOrDefault(key, "");
    }

    /**
     * Enum for NAVER cookie types.
     *
     * @since 1.0.0
     */
    public enum Cookie {
        /**
         * NAVER auth cookie.
         */
        NID_AUT,

        /**
         * NAVER session cookie.
         */
        NID_SES
    }

}
