package org.apache.fop.events;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.util.XMLResourceBundle;
import org.apache.fop.util.text.AdvancedMessageFormat;

public final class EventFormatter {
   private static final Pattern INCLUDES_PATTERN = Pattern.compile("\\{\\{.+\\}\\}");
   private static Log log = LogFactory.getLog(EventFormatter.class);

   private EventFormatter() {
   }

   private static ResourceBundle getBundle(String groupID, Locale locale) {
      String baseName = groupID != null ? groupID : EventFormatter.class.getName();

      ResourceBundle bundle;
      try {
         ClassLoader classLoader = EventFormatter.class.getClassLoader();
         bundle = XMLResourceBundle.getXMLBundle(baseName, locale, classLoader);
      } catch (MissingResourceException var5) {
         if (log.isTraceEnabled()) {
            log.trace("No XMLResourceBundle for " + baseName + " available.");
         }

         bundle = null;
      }

      return bundle;
   }

   public static String format(Event event) {
      return format(event, event.getLocale());
   }

   public static String format(Event event, Locale locale) {
      return format(event, getBundle(event.getEventGroupID(), locale));
   }

   private static String format(Event event, ResourceBundle bundle) {
      assert event != null;

      String key = event.getEventKey();
      String template;
      if (bundle != null) {
         template = bundle.getString(key);
      } else {
         template = "Missing bundle. Can't lookup event key: '" + key + "'.";
      }

      return format(event, processIncludes(template, bundle));
   }

   private static String processIncludes(String template, ResourceBundle bundle) {
      CharSequence input = template;

      int replacements;
      StringBuffer sb;
      do {
         sb = new StringBuffer(Math.max(16, ((CharSequence)input).length()));
         replacements = processIncludesInner((CharSequence)input, sb, bundle);
         input = sb;
      } while(replacements > 0);

      String s = sb.toString();
      return s;
   }

   private static int processIncludesInner(CharSequence template, StringBuffer sb, ResourceBundle bundle) {
      int replacements = 0;
      if (bundle != null) {
         Matcher m;
         for(m = INCLUDES_PATTERN.matcher(template); m.find(); ++replacements) {
            String include = m.group();
            include = include.substring(2, include.length() - 2);
            m.appendReplacement(sb, bundle.getString(include));
         }

         m.appendTail(sb);
      }

      return replacements;
   }

   public static String format(Event event, String pattern) {
      AdvancedMessageFormat format = new AdvancedMessageFormat(pattern);
      Map params = new HashMap(event.getParams());
      params.put("source", event.getSource());
      params.put("severity", event.getSeverity());
      params.put("groupID", event.getEventGroupID());
      params.put("locale", event.getLocale());
      return format.format(params);
   }

   public static class LookupFieldPartFactory implements AdvancedMessageFormat.PartFactory {
      public AdvancedMessageFormat.Part newPart(String fieldName, String values) {
         return new LookupFieldPart(fieldName);
      }

      public String getFormat() {
         return "lookup";
      }
   }

   private static class LookupFieldPart implements AdvancedMessageFormat.Part {
      private String fieldName;

      public LookupFieldPart(String fieldName) {
         this.fieldName = fieldName;
      }

      public boolean isGenerated(Map params) {
         return this.getKey(params) != null;
      }

      public void write(StringBuffer sb, Map params) {
         String groupID = (String)params.get("groupID");
         Locale locale = (Locale)params.get("locale");
         ResourceBundle bundle = EventFormatter.getBundle(groupID, locale);
         if (bundle != null) {
            sb.append(bundle.getString(this.getKey(params)));
         }

      }

      private String getKey(Map params) {
         return (String)params.get(this.fieldName);
      }

      public String toString() {
         return "{" + this.fieldName + ", lookup}";
      }
   }
}
