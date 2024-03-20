package org.apache.fop.fonts.type1;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.NamedCharacter;

abstract class CharMetricsHandler {
   private static final Log LOG = LogFactory.getLog(CharMetricsHandler.class);
   private static final String WHITE_SPACE = "\\s*";
   private static final String OPERATOR = "([A-Z0-9]{1,3})";
   private static final String OPERANDS = "(.*)";
   private static final Pattern METRICS_REGEX = Pattern.compile("\\s*([A-Z0-9]{1,3})\\s*(.*)\\s*");
   private static final Pattern SPLIT_REGEX = Pattern.compile("\\s*;\\s*");

   private CharMetricsHandler() {
   }

   abstract AFMCharMetrics parse(String var1, Stack var2, String var3) throws IOException;

   static CharMetricsHandler getHandler(Map valueParsers, String line) {
      return (CharMetricsHandler)(line != null && line.contains("AdobeStandardEncoding") ? new AdobeStandardCharMetricsHandler(valueParsers) : new DefaultCharMetricsHandler(valueParsers));
   }

   // $FF: synthetic method
   CharMetricsHandler(Object x0) {
      this();
   }

   private static final class AdobeStandardCharMetricsHandler extends CharMetricsHandler {
      private final DefaultCharMetricsHandler defaultHandler;

      private AdobeStandardCharMetricsHandler(Map valueParsers) {
         super(null);
         this.defaultHandler = new DefaultCharMetricsHandler(valueParsers);
      }

      AFMCharMetrics parse(String line, Stack stack, String afmFileName) throws IOException {
         AFMCharMetrics chm = this.defaultHandler.parse(line, stack, afmFileName);
         NamedCharacter namedChar = chm.getCharacter();
         if (namedChar != null) {
            String charName = namedChar.getName();
            int codePoint = AdobeStandardEncoding.getAdobeCodePoint(charName);
            if (chm.getCharCode() != codePoint && !".notdef".equals(charName)) {
               CharMetricsHandler.LOG.info(afmFileName + ": named character '" + charName + "' has an incorrect code point: " + chm.getCharCode() + ". Changed to " + codePoint);
               chm.setCharCode(codePoint);
            }
         }

         return chm;
      }

      // $FF: synthetic method
      AdobeStandardCharMetricsHandler(Map x0, Object x1) {
         this(x0);
      }
   }

   private static final class DefaultCharMetricsHandler extends CharMetricsHandler {
      private final Map valueParsers;

      private DefaultCharMetricsHandler(Map valueParsers) {
         super(null);
         this.valueParsers = valueParsers;
      }

      AFMCharMetrics parse(String line, Stack stack, String afmFileName) throws IOException {
         AFMCharMetrics chm = new AFMCharMetrics();
         stack.push(chm);
         String[] metrics = CharMetricsHandler.SPLIT_REGEX.split(line);
         String[] var6 = metrics;
         int var7 = metrics.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String metric = var6[var8];
            Matcher matcher = CharMetricsHandler.METRICS_REGEX.matcher(metric);
            if (matcher.matches()) {
               String operator = matcher.group(1);
               String operands = matcher.group(2);
               AFMParser.ValueHandler handler = (AFMParser.ValueHandler)this.valueParsers.get(operator);
               if (handler != null) {
                  handler.parse(operands, 0, stack);
               }
            }
         }

         stack.pop();
         return chm;
      }

      // $FF: synthetic method
      DefaultCharMetricsHandler(Map x0, Object x1) {
         this(x0);
      }
   }
}
