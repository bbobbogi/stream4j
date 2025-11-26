package xyz.r2turntrue.chzzk4j.util;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

/**
 * Chrome WebDriver 유틸리티 클래스입니다.
 */
public class Chrome {
    /**
     * Chrome 인스턴스를 생성합니다.
     */
    private Chrome() {
    }

    /**
     * Explicitly sets the properties for the chrome driver.
     *
     * @param path Chrome 드라이버 경로
     */
    public static void setDriverProperty(@NotNull String path) {
        System.setProperty("webdriver.chrome.driver", path);
    }

    /**
     * Get chrome web driver.
     *
     * @return Chrome WebDriver 인스턴스
     */
    public static WebDriver getDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            return driver;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * WebDriver를 JavascriptExecutor로 반환합니다.
     *
     * @return JavascriptExecutor 인스턴스
     */
    public static JavascriptExecutor getDriverAsJavascriptExecutor() {
        return (JavascriptExecutor) getDriver();
    }

}
