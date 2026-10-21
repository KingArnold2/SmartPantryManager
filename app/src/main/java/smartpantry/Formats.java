package com.richfield.smartpantry;

import java.util.Locale;

/** Small formatting helpers shared by several screens. */
public final class Formats {
    private Formats() { }

    /** Formats a quantity without useless decimals: 2.0 -> "2", 0.5 -> "0.5". */
    public static String qty(double value) {
        String s = String.format(Locale.US, "%.2f", value);
        s = s.replaceAll("0+$", "");   // strip trailing zeros
        s = s.replaceAll("\\.$", "");  // strip dangling decimal point
        return s;
    }
}
