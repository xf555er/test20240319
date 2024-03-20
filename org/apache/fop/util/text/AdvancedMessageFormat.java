package org.apache.fop.util.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.xmlgraphics.util.Service;

public class AdvancedMessageFormat {
   static final Pattern COMMA_SEPARATOR_REGEX = Pattern.compile("(?<!\\\\),");
   private static final Map PART_FACTORIES = new HashMap();
   private static final List OBJECT_FORMATTERS = new ArrayList();
   private static final Map FUNCTIONS = new HashMap();
   private CompositePart rootPart;

   public AdvancedMessageFormat(CharSequence pattern) {
      this.parsePattern(pattern);
   }

   private void parsePattern(CharSequence pattern) {
      this.rootPart = new CompositePart(false);
      StringBuffer sb = new StringBuffer();
      this.parseInnerPattern(pattern, this.rootPart, sb, 0);
   }

   private int parseInnerPattern(CharSequence pattern, CompositePart parent, StringBuffer sb, int start) {
      assert sb.length() == 0;

      int i = start;
      int len = pattern.length();

      label63:
      while(i < len) {
         char ch = pattern.charAt(i);
         switch (ch) {
            case '[':
               if (sb.length() > 0) {
                  parent.addChild(new TextPart(sb.toString()));
                  sb.setLength(0);
               }

               ++i;
               CompositePart composite = new CompositePart(true);
               parent.addChild(composite);
               i += this.parseInnerPattern(pattern, composite, sb, i);
               break;
            case '\\':
               if (i < len - 1) {
                  ++i;
                  ch = pattern.charAt(i);
               }

               sb.append(ch);
               ++i;
               break;
            case ']':
               ++i;
               break label63;
            case '{':
               if (sb.length() > 0) {
                  parent.addChild(new TextPart(sb.toString()));
                  sb.setLength(0);
               }

               ++i;

               for(int nesting = 1; i < len; ++i) {
                  ch = pattern.charAt(i);
                  if (ch == '{') {
                     ++nesting;
                  } else if (ch == '}') {
                     --nesting;
                     if (nesting == 0) {
                        ++i;
                        break;
                     }
                  }

                  sb.append(ch);
               }

               parent.addChild(this.parseField(sb.toString()));
               sb.setLength(0);
               break;
            case '|':
               if (sb.length() > 0) {
                  parent.addChild(new TextPart(sb.toString()));
                  sb.setLength(0);
               }

               parent.newSection();
               ++i;
               break;
            default:
               sb.append(ch);
               ++i;
         }
      }

      if (sb.length() > 0) {
         parent.addChild(new TextPart(sb.toString()));
         sb.setLength(0);
      }

      return i - start;
   }

   private Part parseField(String field) {
      String[] parts = COMMA_SEPARATOR_REGEX.split(field, 3);
      String fieldName = parts[0];
      if (parts.length == 1) {
         return (Part)(fieldName.startsWith("#") ? new FunctionPart(fieldName.substring(1)) : new SimpleFieldPart(fieldName));
      } else {
         String format = parts[1];
         PartFactory factory = (PartFactory)PART_FACTORIES.get(format);
         if (factory == null) {
            throw new IllegalArgumentException("No PartFactory available under the name: " + format);
         } else {
            return parts.length == 2 ? factory.newPart(fieldName, (String)null) : factory.newPart(fieldName, parts[2]);
         }
      }
   }

   private static Function getFunction(String functionName) {
      return (Function)FUNCTIONS.get(functionName);
   }

   public String format(Map params) {
      StringBuffer sb = new StringBuffer();
      this.format(params, sb);
      return sb.toString();
   }

   public void format(Map params, StringBuffer target) {
      this.rootPart.write(target, params);
   }

   public static void formatObject(Object obj, StringBuffer target) {
      if (obj instanceof String) {
         target.append(obj);
      } else {
         boolean handled = false;
         Iterator var3 = OBJECT_FORMATTERS.iterator();

         while(var3.hasNext()) {
            ObjectFormatter formatter = (ObjectFormatter)var3.next();
            if (formatter.supportsObject(obj)) {
               formatter.format(target, obj);
               handled = true;
               break;
            }
         }

         if (!handled) {
            target.append(String.valueOf(obj));
         }
      }

   }

   static String unescapeComma(String string) {
      return string.replaceAll("\\\\,", ",");
   }

   static {
      Iterator iter = Service.providers(PartFactory.class);

      while(iter.hasNext()) {
         PartFactory factory = (PartFactory)iter.next();
         PART_FACTORIES.put(factory.getFormat(), factory);
      }

      iter = Service.providers(ObjectFormatter.class);

      while(iter.hasNext()) {
         OBJECT_FORMATTERS.add((ObjectFormatter)iter.next());
      }

      iter = Service.providers(Function.class);

      while(iter.hasNext()) {
         Function function = (Function)iter.next();
         FUNCTIONS.put(function.getName(), function);
      }

   }

   private static class CompositePart implements Part {
      protected List parts = new ArrayList();
      private boolean conditional;
      private boolean hasSections;

      public CompositePart(boolean conditional) {
         this.conditional = conditional;
      }

      private CompositePart(List parts) {
         this.parts.addAll(parts);
         this.conditional = true;
      }

      public void addChild(Part part) {
         if (part == null) {
            throw new NullPointerException("part must not be null");
         } else {
            if (this.hasSections) {
               CompositePart composite = (CompositePart)this.parts.get(this.parts.size() - 1);
               composite.addChild(part);
            } else {
               this.parts.add(part);
            }

         }
      }

      public void newSection() {
         if (!this.hasSections) {
            List p = this.parts;
            this.parts = new ArrayList();
            this.parts.add(new CompositePart(p));
            this.hasSections = true;
         }

         this.parts.add(new CompositePart(true));
      }

      public void write(StringBuffer sb, Map params) {
         Iterator var3;
         Part part;
         if (this.hasSections) {
            var3 = this.parts.iterator();

            while(var3.hasNext()) {
               part = (Part)var3.next();
               if (part.isGenerated(params)) {
                  part.write(sb, params);
                  break;
               }
            }
         } else if (this.isGenerated(params)) {
            var3 = this.parts.iterator();

            while(var3.hasNext()) {
               part = (Part)var3.next();
               part.write(sb, params);
            }
         }

      }

      public boolean isGenerated(Map params) {
         Iterator var2;
         Part part;
         if (this.hasSections) {
            var2 = this.parts.iterator();

            do {
               if (!var2.hasNext()) {
                  return false;
               }

               part = (Part)var2.next();
            } while(!part.isGenerated(params));

            return true;
         } else {
            if (this.conditional) {
               var2 = this.parts.iterator();

               while(var2.hasNext()) {
                  part = (Part)var2.next();
                  if (!part.isGenerated(params)) {
                     return false;
                  }
               }
            }

            return true;
         }
      }

      public String toString() {
         return this.parts.toString();
      }
   }

   private static class FunctionPart implements Part {
      private Function function;

      public FunctionPart(String functionName) {
         this.function = AdvancedMessageFormat.getFunction(functionName);
         if (this.function == null) {
            throw new IllegalArgumentException("Unknown function: " + functionName);
         }
      }

      public void write(StringBuffer sb, Map params) {
         Object obj = this.function.evaluate(params);
         AdvancedMessageFormat.formatObject(obj, sb);
      }

      public boolean isGenerated(Map params) {
         Object obj = this.function.evaluate(params);
         return obj != null;
      }

      public String toString() {
         return "{#" + this.function.getName() + "}";
      }
   }

   private static class SimpleFieldPart implements Part {
      private String fieldName;

      public SimpleFieldPart(String fieldName) {
         this.fieldName = fieldName;
      }

      public void write(StringBuffer sb, Map params) {
         if (!params.containsKey(this.fieldName)) {
            throw new IllegalArgumentException("Message pattern contains unsupported field name: " + this.fieldName);
         } else {
            Object obj = params.get(this.fieldName);
            AdvancedMessageFormat.formatObject(obj, sb);
         }
      }

      public boolean isGenerated(Map params) {
         Object obj = params.get(this.fieldName);
         return obj != null;
      }

      public String toString() {
         return "{" + this.fieldName + "}";
      }
   }

   private static class TextPart implements Part {
      private String text;

      public TextPart(String text) {
         this.text = text;
      }

      public void write(StringBuffer sb, Map params) {
         sb.append(this.text);
      }

      public boolean isGenerated(Map params) {
         return true;
      }

      public String toString() {
         return this.text;
      }
   }

   public interface Function {
      Object evaluate(Map var1);

      Object getName();
   }

   public interface ObjectFormatter {
      void format(StringBuffer var1, Object var2);

      boolean supportsObject(Object var1);
   }

   public interface PartFactory {
      Part newPart(String var1, String var2);

      String getFormat();
   }

   public interface Part {
      void write(StringBuffer var1, Map var2);

      boolean isGenerated(Map var1);
   }
}
