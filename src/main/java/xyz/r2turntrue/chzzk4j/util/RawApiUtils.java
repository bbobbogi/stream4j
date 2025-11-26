package xyz.r2turntrue.chzzk4j.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * API 요청을 위한 유틸리티 클래스입니다.
 */
public class RawApiUtils {
    /**
     * RawApiUtils 인스턴스 생성을 방지합니다.
     */
    private RawApiUtils() {
    }

    /**
     * HTTP GET 요청을 위한 Request.Builder를 생성합니다.
     *
     * @param url 요청할 URL
     * @return Request.Builder 인스턴스
     */
    public static Request.Builder httpGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .get();
    }

    /**
     * HTTP 요청을 실행하고 응답을 JsonObject로 반환합니다.
     *
     * @param httpClient OkHttpClient 인스턴스
     * @param request 실행할 요청
     * @param isDebug 디버그 모드 여부
     * @return 응답 JSON 객체
     * @throws IOException 요청 실패 시
     */
    public static JsonObject getRawJson(OkHttpClient httpClient, Request request, boolean isDebug) throws IOException {
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            assert body != null;
            String bodyString = body.string();
            //\System.out.println("BD: " + bodyString);

            return JsonParser.parseString(bodyString).getAsJsonObject();
        } else {
            System.err.println(response);
            if (isDebug) {
                ResponseBody body = response.body();
                if (body != null) {
                    String bodyString = body.string();
                    System.out.println("BD: " + bodyString);
                }
            }
            throw new IOException("Response was not successful!");
        }
    }

    /**
     * HTTP 요청을 실행하고 응답의 content 필드를 JsonElement로 반환합니다.
     *
     * @param httpClient OkHttpClient 인스턴스
     * @param request 실행할 요청
     * @param isDebug 디버그 모드 여부
     * @return 응답의 content JSON 요소
     * @throws IOException 요청 실패 시
     */
    public static JsonElement getContentJson(OkHttpClient httpClient, Request request, boolean isDebug) throws IOException {
        return getRawJson(httpClient, request, isDebug).get("content");
    }
}
