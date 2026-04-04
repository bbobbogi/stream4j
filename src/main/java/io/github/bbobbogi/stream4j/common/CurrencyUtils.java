package io.github.bbobbogi.stream4j.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CurrencyUtils {

    public static final String CHZZK_CHEESE = "CHEESE";
    public static final String CIME_BEAM = "BEAM";
    public static final String SOOP_BALLOON = "BALLOON";
    public static final String TOONATION_WON = "KRW";

    private static final Map<String, String> SYMBOL_TO_CODE;
    private static final Map<String, String> CODE_TO_DISPLAY;
    private static final Map<String, Integer> KRW_RATE = Map.of(
            CHZZK_CHEESE, 1,
            CIME_BEAM, 1,
            SOOP_BALLOON, 100
    );

    static {
        Map<String, String> symbols = new HashMap<>();
        for (java.util.Currency c : java.util.Currency.getAvailableCurrencies()) {
            symbols.put(c.getSymbol(), c.getCurrencyCode());
        }
        symbols.put("NT$", "TWD");
        symbols.put("HK$", "HKD");
        symbols.put("CA$", "CAD");
        symbols.put("A$", "AUD");
        symbols.put("NZ$", "NZD");
        symbols.put("US$", "USD");
        symbols.put("R$", "BRL");
        symbols.put("MX$", "MXN");
        symbols.put("JP¥", "JPY");
        symbols.put("CN¥", "CNY");
        symbols.put("kr", "SEK");
        symbols.put("zł", "PLN");
        SYMBOL_TO_CODE = Collections.unmodifiableMap(symbols);

        Map<String, String> display = new HashMap<>();
        display.put("KRW", "원");
        display.put("JPY", "엔");
        display.put("CNY", "위안");
        display.put(CHZZK_CHEESE, "치즈");
        display.put(CIME_BEAM, "빔");
        display.put(SOOP_BALLOON, "별풍선");
        display.put(TOONATION_WON, "원");
        CODE_TO_DISPLAY = Collections.unmodifiableMap(display);
    }

    private CurrencyUtils() {}

    public static ParsedAmount parse(String raw) {
        if (raw == null || raw.isEmpty()) return new ParsedAmount("", "0", raw);
        String symbol = raw.replaceAll("[0-9,.]", "").trim();
        String amount = raw.replaceAll("[^0-9.]", "");
        String currencyCode = SYMBOL_TO_CODE.getOrDefault(symbol, symbol);
        return new ParsedAmount(currencyCode, amount, raw);
    }

    public static ParsedAmount of(String currencyCode, int amount) {
        return new ParsedAmount(currencyCode, String.valueOf(amount), null);
    }

    public static ParsedAmount of(String currencyCode, long amount) {
        return new ParsedAmount(currencyCode, String.valueOf(amount), null);
    }

    public static String format(ParsedAmount parsed) {
        String suffix = CODE_TO_DISPLAY.get(parsed.currencyCode());
        if (suffix != null) {
            String base = parsed.amount() + suffix;
            int krw = toKRW(parsed);
            if (krw > 0 && KRW_RATE.containsKey(parsed.currencyCode())) {
                base += " (" + krw + "원)";
            }
            return base;
        }
        return parsed.amount() + " " + parsed.currencyCode();
    }

    public static int toKRW(ParsedAmount parsed) {
        Integer rate = KRW_RATE.get(parsed.currencyCode());
        if (rate == null) return 0;
        try {
            return (int) (Double.parseDouble(parsed.amount()) * rate);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public record ParsedAmount(String currencyCode, String amount, String raw) {}
}
