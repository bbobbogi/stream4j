package io.github.bbobbogi.stream4j.soop;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SOOPSessionCookiesTest {
    @Test
    void browserCookiesOnlyReachTheOfficialLiveApiWithinTheirOriginalScope() {
        String api = "https://live.sooplive.com/afreeca/player_live_api.php";
        Cookie session = new Cookie.Builder().name("AuthTicket").value("fixture")
                .domain("sooplive.com").path("/").secure().build();
        Cookie loginOnly = new Cookie.Builder().name("UserTicket").value("fixture")
                .hostOnlyDomain("login.sooplive.com").path("/").secure().build();
        Cookie expired = new Cookie.Builder().name("AuthTicket").value("expired")
                .domain("sooplive.com").expiresAt(1).build();
        var cookies = List.of(session, loginOnly, expired);
        assertEquals(List.of(session), SOOPChatBuilder.browserCookiesFor(HttpUrl.get(api + "?bjid=fixture"), cookies));
        for (String url : List.of(api.replace("https:", "http:"), api.replace(".com/", ".com:444/"),
                api.replace("live.", "chat."), api.replace(".com/", ".com.example/"), api + "/other")) {
            assertEquals(List.of(), SOOPChatBuilder.browserCookiesFor(HttpUrl.get(url), cookies), url);
        }
    }
}
