package org.apache.xerces.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SAXMessageFormatter {
   public static String formatMessage(Locale var0, String var1, Object[] var2) throws MissingResourceException {
      if (var0 == null) {
         var0 = Locale.getDefault();
      }

      ResourceBundle var3 = ResourceBundle.getBundle("org.apache.xerces.impl.msg.SAXMessages", var0);

      String var4;
      try {
         var4 = var3.getString(var1);
         if (var2 != null) {
            try {
               var4 = MessageFormat.format(var4, var2);
            } catch (Exception var7) {
               var4 = var3.getString("FormatFailed");
               var4 = var4 + " " + var3.getString(var1);
            }
         }
      } catch (MissingResourceException var8) {
         var4 = var3.getString("BadMessageKey");
         throw new MissingResourceException(var1, var4, var1);
      }

      if (var4 == null) {
         var4 = var1;
         if (var2.length > 0) {
            StringBuffer var5 = new StringBuffer(var1);
            var5.append('?');

            for(int var6 = 0; var6 < var2.length; ++var6) {
               if (var6 > 0) {
                  var5.append('&');
               }

               var5.append(String.valueOf(var2[var6]));
            }
         }
      }

      return var4;
   }
}
