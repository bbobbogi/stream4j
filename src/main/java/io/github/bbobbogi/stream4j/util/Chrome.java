package io.github.bbobbogi.stream4j.util;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

/**
 * Utility methods for creating and configuring a Chrome WebDriver.
 *
 * @apiNote This is an internal API and may change without notice.
 * @since 1.0.0
 */
public class Chrome {
    /**
     * Prevents instantiation of this utility class.
     */
    private Chrome() {
    }

    /**
     * Explicitly sets the properties for the chrome driver.
     *
     * @param path Chrome driver path
     */
    public static void setDriverProperty(@NotNull String path) {
        System.setProperty("webdriver.chrome.driver", path);
    }

    /**
     * Get chrome web driver.
     *
     * @return Chrome WebDriver instance
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
     * Returns a WebDriver as a {@link JavascriptExecutor}.
     *
     * @return {@link JavascriptExecutor} instance
     */
    public static JavascriptExecutor getDriverAsJavascriptExecutor() {
        return (JavascriptExecutor) getDriver();
    }

}
