package com.richfield.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;

/** Wrapper around SharedPreferences for the settings screen. */
public final class Prefs {
    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    private Prefs() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context c) {
        return prefs(c).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }
}
