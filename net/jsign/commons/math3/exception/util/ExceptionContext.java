package net.jsign.commons.math3.exception.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExceptionContext implements Serializable {
   private Throwable throwable;
   private List msgPatterns;
   private List msgArguments;
   private Map context;

   public ExceptionContext(Throwable throwable) {
      this.throwable = throwable;
      this.msgPatterns = new ArrayList();
      this.msgArguments = new ArrayList();
      this.context = new HashMap();
   }

   public void addMessage(Localizable pattern, Object... arguments) {
      this.msgPatterns.add(pattern);
      this.msgArguments.add(ArgUtils.flatten(arguments));
   }

   public String getMessage() {
      return this.getMessage(Locale.US);
   }

   public String getLocalizedMessage() {
      return this.getMessage(Locale.getDefault());
   }

   public String getMessage(Locale locale) {
      return this.buildMessage(locale, ": ");
   }

   private String buildMessage(Locale locale, String separator) {
      StringBuilder sb = new StringBuilder();
      int count = 0;
      int len = this.msgPatterns.size();

      for(int i = 0; i < len; ++i) {
         Localizable pat = (Localizable)this.msgPatterns.get(i);
         Object[] args = (Object[])this.msgArguments.get(i);
         MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale), locale);
         sb.append(fmt.format(args));
         ++count;
         if (count < len) {
            sb.append(separator);
         }
      }

      return sb.toString();
   }
}
