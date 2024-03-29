package org.apache.xerces.xinclude;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.xerces.util.MessageFormatter;

public class XIncludeMessageFormatter implements MessageFormatter {
   public static final String XINCLUDE_DOMAIN = "http://www.w3.org/TR/xinclude";
   private Locale fLocale = null;
   private ResourceBundle fResourceBundle = null;

   public String formatMessage(Locale var1, String var2, Object[] var3) throws MissingResourceException {
      if (var1 == null) {
         var1 = Locale.getDefault();
      }

      if (var1 != this.fLocale) {
         this.fResourceBundle = ResourceBundle.getBundle("org.apache.xerces.impl.msg.XIncludeMessages", var1);
         this.fLocale = var1;
      }

      String var4 = this.fResourceBundle.getString(var2);
      if (var3 != null) {
         try {
            var4 = MessageFormat.format(var4, var3);
         } catch (Exception var6) {
            var4 = this.fResourceBundle.getString("FormatFailed");
            var4 = var4 + " " + this.fResourceBundle.getString(var2);
         }
      }

      if (var4 == null) {
         var4 = this.fResourceBundle.getString("BadMessageKey");
         throw new MissingResourceException(var4, "org.apache.xerces.impl.msg.XIncludeMessages", var2);
      } else {
         return var4;
      }
   }
}
