package net.jsign.poi.util;

import java.nio.charset.Charset;
import java.util.TimeZone;

public final class LocaleUtil {
   public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");
   public static final Charset CHARSET_1252 = Charset.forName("CP1252");
   private static final ThreadLocal userTimeZone = new ThreadLocal();
   private static final ThreadLocal userLocale = new ThreadLocal();
}
