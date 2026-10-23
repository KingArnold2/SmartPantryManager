package com.richfield.smartpantry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Helpers for working with expiry dates stored as yyyy-MM-dd text. */
public final class ExpiryUtils {
    /** Items expiring within this many days count as "expiring soon". */
    public static final int SOON_DAYS = 3;

    private ExpiryUtils() { }

    /**
     * @return whole days from today until the date (negative if already past),
     *         or null if the date is empty or invalid.
     */
    public static Integer daysUntil(String iso) {
        if (iso == null || iso.trim().isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            Date target = f.parse(iso.trim());
            if (target == null) return null;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffMs = target.getTime() - today.getTimeInMillis();
            return (int) Math.round(diffMs / (24.0 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isExpiringSoon(String iso) {
        Integer d = daysUntil(iso);
        return d != null && d <= SOON_DAYS;
    }
}
