package com.projeto.barberconnect.util;

import java.util.Locale;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static String trimToNull(String value) {
        String trimmedValue = trim(value);

        return trimmedValue == null || trimmedValue.isEmpty() ? null : trimmedValue;
    }

    public static String normalizeEmail(String email) {
        return trim(email).toLowerCase(Locale.ROOT);
    }
}
