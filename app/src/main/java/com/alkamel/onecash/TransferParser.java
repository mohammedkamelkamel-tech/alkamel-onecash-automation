package com.alkamel.onecash;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;

public final class TransferParser {
    private static final Pattern AMOUNT = Pattern.compile("استلمت\\s*([0-9٠-٩۰-۹]+(?:[.,][0-9٠-٩۰-۹]+)?)");
    private static final Pattern NAME = Pattern.compile("(?:\\n|\\r)\\s*من\\s+([^\\n\\r]+)");
    private TransferParser() {}

    public static Parsed parse(String body) {
        if (body == null) return null;
        String normalizedDigits = digitsToEnglish(body);
        Matcher am = AMOUNT.matcher(normalizedDigits);
        Matcher nm = NAME.matcher(body);
        if (!am.find() || !nm.find()) return null;
        try {
            BigDecimal value = new BigDecimal(am.group(1).replace(',', '.'));
            if (value.signum() <= 0 || value.stripTrailingZeros().scale() > 0) return null;
            int amount = value.intValueExact();
            String payer = normalizeName(nm.group(1));
            if (payer.isEmpty()) return null;
            return new Parsed(amount, payer);
        } catch (Exception ignored) { return null; }
    }

    public static String normalizeName(String value) {
        if (value == null) return "";
        String s = Normalizer.normalize(value, Normalizer.Form.NFKC);
        s = s.replaceAll("[\\u0640\\u064B-\\u065F\\u0670]", "");
        s = s.replace('أ','ا').replace('إ','ا').replace('آ','ا').replace('ى','ي');
        s = s.replaceAll("[،؛؟!:.\\-_/\\s\\p{Punct}]+", " ").trim();
        return s;
    }

    private static String digitsToEnglish(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '٠' && c <= '٩') out.append((char) ('0' + c - '٠'));
            else if (c >= '۰' && c <= '۹') out.append((char) ('0' + c - '۰'));
            else out.append(c);
        }
        return out.toString();
    }

    public static final class Parsed {
        public final int amount;
        public final String payerName;
        public Parsed(int amount, String payerName) { this.amount=amount; this.payerName=payerName; }
    }
}
