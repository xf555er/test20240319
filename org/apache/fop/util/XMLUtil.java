package org.apache.fop.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class XMLUtil implements XMLConstants {
   private XMLUtil() {
   }

   public static boolean getAttributeAsBoolean(Attributes attributes, String name, boolean defaultValue) {
      String s = attributes.getValue(name);
      return s == null ? defaultValue : Boolean.valueOf(s);
   }

   public static int getAttributeAsInt(Attributes attributes, String name, int defaultValue) {
      String s = attributes.getValue(name);
      return s == null ? defaultValue : Integer.parseInt(s);
   }

   public static int getAttributeAsInt(Attributes attributes, String name) throws SAXException {
      String s = attributes.getValue(name);
      if (s == null) {
         throw new SAXException("Attribute '" + name + "' is missing");
      } else {
         return Integer.parseInt(s);
      }
   }

   public static Integer getAttributeAsInteger(Attributes attributes, String name) {
      String s = attributes.getValue(name);
      return s == null ? null : Integer.valueOf(s);
   }

   public static Rectangle2D getAttributeAsRectangle2D(Attributes attributes, String name) {
      String s = attributes.getValue(name).trim();
      double[] values = ConversionUtils.toDoubleArray(s, "\\s");
      if (values.length != 4) {
         throw new IllegalArgumentException("Rectangle must consist of 4 double values!");
      } else {
         return new Rectangle2D.Double(values[0], values[1], values[2], values[3]);
      }
   }

   public static Rectangle getAttributeAsRectangle(Attributes attributes, String name) {
      String s = attributes.getValue(name);
      if (s == null) {
         return null;
      } else {
         int[] values = ConversionUtils.toIntArray(s.trim(), "\\s");
         if (values.length != 4) {
            throw new IllegalArgumentException("Rectangle must consist of 4 int values!");
         } else {
            return new Rectangle(values[0], values[1], values[2], values[3]);
         }
      }
   }

   public static int[] getAttributeAsIntArray(Attributes attributes, String name) {
      String s = attributes.getValue(name);
      return s == null ? null : ConversionUtils.toIntArray(s.trim(), "\\s");
   }

   public static void addAttribute(AttributesImpl atts, org.apache.xmlgraphics.util.QName attribute, String value) {
      atts.addAttribute(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getQName(), "CDATA", value);
   }

   public static void addAttribute(AttributesImpl atts, String localName, String value) {
      atts.addAttribute("", localName, localName, "CDATA", value);
   }

   public static String encodePositionAdjustments(int[][] dp, int paCount) {
      assert dp != null;

      StringBuffer sb = new StringBuffer();
      int na = paCount;
      int nz = 0;
      sb.append(paCount);

      for(int i = 0; i < na; ++i) {
         int[] pa = dp[i];
         if (pa != null) {
            for(int k = 0; k < 4; ++k) {
               int a = pa[k];
               if (a != 0) {
                  encodeNextAdjustment(sb, nz, a);
                  nz = 0;
               } else {
                  ++nz;
               }
            }
         } else {
            nz += 4;
         }
      }

      encodeNextAdjustment(sb, nz, 0);
      return sb.toString();
   }

   public static String encodePositionAdjustments(int[][] dp) {
      assert dp != null;

      return encodePositionAdjustments(dp, dp.length);
   }

   private static void encodeNextAdjustment(StringBuffer sb, int nz, int a) {
      encodeZeroes(sb, nz);
      encodeAdjustment(sb, a);
   }

   private static void encodeZeroes(StringBuffer sb, int nz) {
      if (nz > 0) {
         sb.append(' ');
         if (nz == 1) {
            sb.append('0');
         } else {
            sb.append('Z');
            sb.append(nz);
         }
      }

   }

   private static void encodeAdjustment(StringBuffer sb, int a) {
      if (a != 0) {
         sb.append(' ');
         sb.append(a);
      }

   }

   public static int[][] decodePositionAdjustments(String value) {
      int[][] dp = (int[][])null;
      if (value != null) {
         String[] sa = value.split("\\s");
         if (sa != null && sa.length > 0) {
            int na = Integer.parseInt(sa[0]);
            dp = new int[na][4];
            int i = 1;
            int n = sa.length;

            for(int k = 0; i < n; ++i) {
               String s = sa[i];
               if (s.charAt(0) == 'Z') {
                  int nz = Integer.parseInt(s.substring(1));
                  k += nz;
               } else {
                  dp[k / 4][k % 4] = Integer.parseInt(s);
                  ++k;
               }
            }
         }
      }

      return dp;
   }

   public static int[][] getAttributeAsPositionAdjustments(Attributes attributes, String name) {
      String s = attributes.getValue(name);
      return s == null ? (int[][])null : decodePositionAdjustments(s.trim());
   }

   public static String escape(String unescaped) {
      int needsEscape = 0;
      int i = 0;

      int i;
      int n;
      for(i = unescaped.length(); i < i; ++i) {
         n = unescaped.charAt(i);
         if (n == 60 || n == 62 || n == 38) {
            ++needsEscape;
         }
      }

      if (needsEscape <= 0) {
         return unescaped;
      } else {
         StringBuffer sb = new StringBuffer(unescaped.length() + 6 * needsEscape);
         i = 0;

         for(n = unescaped.length(); i < n; ++i) {
            char c = unescaped.charAt(i);
            if (c != '<' && c != '>' && c != '&') {
               sb.append(c);
            } else {
               sb.append("&#x");
               sb.append(Integer.toString(c, 16));
               sb.append(';');
            }
         }

         return sb.toString();
      }
   }
}
