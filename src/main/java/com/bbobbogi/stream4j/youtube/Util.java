package com.bbobbogi.stream4j.youtube;

import com.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("unchecked")
public class Util {

    private static final Gson gson = new Gson();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public static String toJSON(Map<String, Object> json) {
        StringBuilder js = new StringBuilder();
        js.append("{");
        for (String key : json.keySet()) {
            js.append("'").append(key).append("': ");
            Object d = json.get(key);
            if (d instanceof Byte ||
                    d instanceof Character ||
                    d instanceof Short ||
                    d instanceof Integer ||
                    d instanceof Long ||
                    d instanceof Float ||
                    d instanceof Double ||
                    d instanceof Boolean) {
                js.append(d);
            } else if (d instanceof Map) {
                js.append(toJSON((Map<String, Object>) d));
            } else {
                js.append("\"").append(d.toString().replace("\"", "\\\"").replace("\\", "\\\\")).append("\"");
            }
            js.append(", ");
        }
        return js.substring(0, js.length() - 2) + "}";
    }

    public static Map<String, Object> toJSON(String json) {
        if (!json.startsWith("{")) {
            String preview = json.length() > 200 ? json.substring(0, 200) + "..." : json;
            throw new IllegalArgumentException("Expected JSON but got: " + preview);
        }
        return gson.fromJson(json, Map.class);
    }

    public static Map<String, Object> getJSONMap(Map<String, Object> json, String... keys) {
        Map<String, Object> map = json;
        for (String key : keys) {
            if (map.containsKey(key)) {
                map = (Map<String, Object>) map.get(key);
            } else {
                return null;
            }
        }
        return map;
    }

    public static Map<String, Object> getJSONMap(Map<String, Object> json, Object... keys) {
        Map<String, Object> map = json;
        List<Object> list = null;
        for (Object key : keys) {
            if (map != null) {
                if (map.containsKey(key.toString())) {
                    Object value = map.get(key.toString());
                    if (value instanceof List) {
                        list = (List<Object>) value;
                        map = null;
                    } else {
                        map = (Map<String, Object>) value;
                    }
                } else {
                    return null;
                }
            } else {
                map = (Map<String, Object>) list.get((int) key);
                list = null;
            }
        }
        return map;
    }

    public static List<Object> getJSONList(Map<String, Object> json, String listKey, String... keys) {
        Map<String, Object> map = getJSONMap(json, keys);
        if (map != null && map.containsKey(listKey)) {
            return (List<Object>) map.get(listKey);
        }
        return null;
    }

    public static Object getJSONValue(Map<String, Object> json, String key) {
        if (json != null && json.containsKey(key)) {
            return json.get(key);
        }
        return null;
    }

    public static String getJSONValueString(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        return value != null ? value.toString() : null;
    }

    public static boolean getJSONValueBoolean(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        return value != null && (boolean) value;
    }

    public static long getJSONValueLong(Map<String, Object> json, String key) {
        Object value = getJSONValue(json, key);
        return value != null ? ((Double) value).longValue() : 0;
    }

    public static int getJSONValueInt(Map<String, Object> json, String key) {
        return (int) getJSONValueLong(json, key);
    }

    public static String getPageContent(String url, Map<String, String> header) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        try (Response response = SharedHttpClient.get().newCall(builder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }
        return null;
    }

    public static String getPageContentWithJson(String url, String data, Map<String, String> header) throws IOException {
        RequestBody body = RequestBody.create(data, JSON_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        try (Response response = SharedHttpClient.get().newCall(builder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("HTTP error code: " + response.code());
            }
        }
    }

    public static void sendHttpRequestWithJson(String url, String data, Map<String, String> header) throws IOException {
        RequestBody body = RequestBody.create(data, JSON_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        try (Response response = SharedHttpClient.get().newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException(errorBody);
            }
        }
    }

    public static String generateClientMessageId() {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 26; i++) {
            sb.append(base.charAt(random.nextInt(base.length())));
        }
        return sb.toString();
    }

    public static JsonElement searchJsonElementByKey(String key, JsonElement jsonElement) {
        JsonElement value = null;
        if (jsonElement.isJsonArray()) {
            for (JsonElement el : jsonElement.getAsJsonArray()) {
                value = searchJsonElementByKey(key, el);
                if (value != null) return value;
            }
        } else if (jsonElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals(key)) return entry.getValue();
                value = searchJsonElementByKey(key, entry.getValue());
                if (value != null) return value;
            }
        } else {
            if (jsonElement.toString().equals(key)) return jsonElement;
        }
        return value;
    }
}
