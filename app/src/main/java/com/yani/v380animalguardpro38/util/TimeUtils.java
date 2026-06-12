package com.yani.v380animalguardpro38.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeUtils {
    private TimeUtils() {}

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public static String nowShort() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public static String eventTime(long seconds) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(seconds * 1000L));
        } catch (Throwable e) {
            return "rec_time=" + seconds;
        }
    }
}
