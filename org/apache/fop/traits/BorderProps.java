package org.apache.fop.traits;

import java.awt.Color;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.xmlgraphics.java2d.color.ColorUtil;

public class BorderProps implements Serializable {
   private static final long serialVersionUID = 8022237892391068187L;
   public final int style;
   public final Color color;
   public final int width;
   private final int radiusStart;
   private final int radiusEnd;
   private final Mode mode;

   public BorderProps(int style, int width, int radiusStart, int radiusEnd, Color color, Mode mode) {
      this.style = style;
      this.width = width;
      this.radiusStart = radiusStart;
      this.radiusEnd = radiusEnd;
      this.color = color;
      this.mode = mode;
   }

   public static BorderProps makeRectangular(int style, int width, Color color, Mode mode) {
      return new BorderProps(style, width, 0, 0, color, mode);
   }

   private BorderProps(String style, int width, int radiusStart, int radiusEnd, Color color, Mode mode) {
      this(getConstantForStyle(style), width, radiusStart, radiusEnd, color, mode);
   }

   public int getRadiusStart() {
      return this.radiusStart;
   }

   public int getRadiusEnd() {
      return this.radiusEnd;
   }

   public Mode getMode() {
      return this.mode;
   }

   public static int getClippedWidth(BorderProps bp) {
      return bp == null ? 0 : bp.mode.getClippedWidth(bp);
   }

   private String getStyleString() {
      return BorderStyle.valueOf(this.style).getName();
   }

   private static int getConstantForStyle(String style) {
      return BorderStyle.valueOf(style).getEnumValue();
   }

   public boolean isCollapseOuter() {
      return this.mode == BorderProps.Mode.COLLAPSE_OUTER;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof BorderProps)) {
         return false;
      } else {
         BorderProps other = (BorderProps)obj;
         return this.style == other.style && ColorUtil.isSameColor(this.color, other.color) && this.width == other.width && this.mode == other.mode && this.radiusStart == other.radiusStart && this.radiusEnd == other.radiusEnd;
      }
   }

   public static BorderProps valueOf(FOUserAgent foUserAgent, String s) {
      return BorderProps.BorderPropsDeserializer.INSTANCE.valueOf(foUserAgent, s);
   }

   public String toString() {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append('(').append(this.getStyleString()).append(',').append(org.apache.fop.util.ColorUtil.colorToString(this.color)).append(',').append(this.width);
      if (!this.mode.equals(BorderProps.Mode.SEPARATE)) {
         sbuf.append(",").append(this.mode.value);
      }

      if (this.radiusStart != 0 || this.radiusEnd != 0) {
         if (this.mode.equals(BorderProps.Mode.SEPARATE)) {
            sbuf.append(",").append(BorderProps.Mode.SEPARATE.value);
         }

         sbuf.append(',').append(this.radiusStart).append(',').append(this.radiusEnd);
      }

      sbuf.append(')');
      return sbuf.toString();
   }

   // $FF: synthetic method
   BorderProps(String x0, int x1, int x2, int x3, Color x4, Mode x5, Object x6) {
      this(x0, x1, x2, x3, x4, x5);
   }

   private static final class BorderPropsDeserializer {
      private static final BorderPropsDeserializer INSTANCE = new BorderPropsDeserializer();
      private static final Pattern PATTERN = Pattern.compile("([^,\\(]+(?:\\(.*\\))?)");

      public BorderProps valueOf(FOUserAgent foUserAgent, String s) {
         if (s.startsWith("(") && s.endsWith(")")) {
            s = s.substring(1, s.length() - 1);
            Matcher m = PATTERN.matcher(s);
            m.find();
            String style = m.group();
            m.find();
            String color = m.group();
            m.find();
            int width = Integer.parseInt(m.group());
            Mode mode = BorderProps.Mode.SEPARATE;
            if (m.find()) {
               String ms = m.group();
               if (BorderProps.Mode.COLLAPSE_INNER.value.equalsIgnoreCase(ms)) {
                  mode = BorderProps.Mode.COLLAPSE_INNER;
               } else if (BorderProps.Mode.COLLAPSE_OUTER.value.equalsIgnoreCase(ms)) {
                  mode = BorderProps.Mode.COLLAPSE_OUTER;
               }
            }

            Color c;
            try {
               c = org.apache.fop.util.ColorUtil.parseColorString(foUserAgent, color);
            } catch (PropertyException var11) {
               throw new IllegalArgumentException(var11.getMessage());
            }

            int startRadius = 0;
            int endRadius = 0;
            if (m.find()) {
               startRadius = Integer.parseInt(m.group());
               m.find();
               endRadius = Integer.parseInt(m.group());
            }

            return new BorderProps(style, width, startRadius, endRadius, c, mode);
         } else {
            throw new IllegalArgumentException("BorderProps must be surrounded by parentheses");
         }
      }
   }

   public static enum Mode {
      SEPARATE("separate") {
         int getClippedWidth(BorderProps bp) {
            return 0;
         }
      },
      COLLAPSE_INNER("collapse-inner"),
      COLLAPSE_OUTER("collapse-outer");

      private final String value;

      private Mode(String value) {
         this.value = value;
      }

      int getClippedWidth(BorderProps bp) {
         return bp.width / 2;
      }

      // $FF: synthetic method
      Mode(String x2, Object x3) {
         this(x2);
      }
   }
}
