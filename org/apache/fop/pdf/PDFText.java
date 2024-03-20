package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import org.apache.fop.util.CharUtilities;

public class PDFText extends PDFObject {
   private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private String text;

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   protected String toPDFString() {
      if (this.getText() == null) {
         throw new IllegalArgumentException("The text of this PDFText must not be empty");
      } else {
         StringBuffer sb = new StringBuffer(64);
         sb.append("(");
         sb.append(escapeText(this.getText()));
         sb.append(")");
         return sb.toString();
      }
   }

   public static final String escapeText(String text) {
      return escapeText(text, false);
   }

   public static final String escapeText(String text, boolean forceHexMode) {
      if (text != null && text.length() > 0) {
         boolean unicode = false;
         boolean hexMode = false;
         int l;
         if (forceHexMode) {
            hexMode = true;
         } else {
            int i = 0;

            for(l = text.length(); i < l; ++i) {
               if (text.charAt(i) >= 128) {
                  unicode = true;
                  hexMode = true;
                  break;
               }
            }
         }

         if (hexMode) {
            byte[] uniBytes;
            try {
               uniBytes = text.getBytes("UTF-16");
            } catch (UnsupportedEncodingException var10) {
               throw new RuntimeException("Incompatible VM", var10);
            }

            return toHex(uniBytes);
         } else {
            StringBuffer result = new StringBuffer(text.length() * 2);
            result.append("(");
            l = text.length();
            int i;
            char ch;
            if (unicode) {
               result.append("\\376\\377");

               for(i = 0; i < l; ++i) {
                  ch = text.charAt(i);
                  int high = (ch & '\uff00') >>> 8;
                  int low = ch & 255;
                  result.append("\\");
                  result.append(Integer.toOctalString(high));
                  result.append("\\");
                  result.append(Integer.toOctalString(low));
               }
            } else {
               for(i = 0; i < l; ++i) {
                  ch = text.charAt(i);
                  if (ch >= 256) {
                     throw new IllegalStateException("Can only treat text in 8-bit ASCII/PDFEncoding");
                  }

                  escapeStringChar(ch, result);
               }
            }

            result.append(")");
            return result.toString();
         }
      } else {
         return "()";
      }
   }

   public static final String toHex(byte[] data, boolean brackets) {
      StringBuffer sb = new StringBuffer(data.length * 2);
      if (brackets) {
         sb.append("<");
      }

      byte[] var3 = data;
      int var4 = data.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte aData = var3[var5];
         sb.append(DIGITS[aData >>> 4 & 15]);
         sb.append(DIGITS[aData & 15]);
      }

      if (brackets) {
         sb.append(">");
      }

      return sb.toString();
   }

   public static final String toHex(byte[] data) {
      return toHex(data, true);
   }

   public static final byte[] toUTF16(String text) {
      try {
         return text.getBytes("UnicodeBig");
      } catch (UnsupportedEncodingException var2) {
         throw new RuntimeException("Incompatible VM", var2);
      }
   }

   public static final String toUnicodeHex(char c) {
      StringBuffer buf = new StringBuffer(4);

      byte[] uniBytes;
      try {
         char[] a = new char[]{c};
         uniBytes = (new String(a)).getBytes("UTF-16BE");
      } catch (UnsupportedEncodingException var7) {
         throw new RuntimeException("Incompatible VM", var7);
      }

      byte[] var8 = uniBytes;
      int var4 = uniBytes.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte uniByte = var8[var5];
         buf.append(DIGITS[uniByte >>> 4 & 15]);
         buf.append(DIGITS[uniByte & 15]);
      }

      return buf.toString();
   }

   public static final void toUnicodeHex(int c, StringBuffer sb) {
      if (CharUtilities.isBmpCodePoint(c)) {
         sb.append(Integer.toHexString(c + 65536).substring(1).toUpperCase(Locale.US));
      } else {
         sb.append(Integer.toHexString(c + 16777216).substring(1).toUpperCase(Locale.US));
      }

   }

   public static final String escapeString(String s) {
      if (s != null && s.length() != 0) {
         StringBuffer sb = new StringBuffer(64);
         sb.append("(");

         for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            escapeStringChar(c, sb);
         }

         sb.append(")");
         return sb.toString();
      } else {
         return "()";
      }
   }

   public static final void escapeStringChar(char c, StringBuffer target) {
      if (c > 127) {
         target.append("\\");
         target.append(Integer.toOctalString(c));
      } else {
         switch (c) {
            case '\b':
               target.append("\\b");
               break;
            case '\t':
               target.append("\\t");
               break;
            case '\n':
               target.append("\\n");
               break;
            case '\f':
               target.append("\\f");
               break;
            case '\r':
               target.append("\\r");
               break;
            case '(':
               target.append("\\(");
               break;
            case ')':
               target.append("\\)");
               break;
            case '\\':
               target.append("\\\\");
               break;
            default:
               target.append(c);
         }
      }

   }

   public static final byte[] escapeByteArray(byte[] data) {
      ByteArrayOutputStream bout = new ByteArrayOutputStream(data.length);
      bout.write(40);
      byte[] var2 = data;
      int var3 = data.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         byte b = var2[var4];
         switch (b) {
            case 8:
               bout.write(92);
               bout.write(98);
               break;
            case 9:
               bout.write(92);
               bout.write(116);
               break;
            case 10:
               bout.write(92);
               bout.write(110);
               break;
            case 12:
               bout.write(92);
               bout.write(102);
               break;
            case 13:
               bout.write(92);
               bout.write(114);
               break;
            case 40:
               bout.write(92);
               bout.write(40);
               break;
            case 41:
               bout.write(92);
               bout.write(41);
               break;
            case 92:
               bout.write(92);
               bout.write(92);
               break;
            default:
               bout.write(b);
         }
      }

      bout.write(41);
      return bout.toByteArray();
   }

   public static String toPDFString(CharSequence text) {
      return toPDFString(text, '?');
   }

   public static String toPDFString(CharSequence text, char replacement) {
      StringBuffer sb = new StringBuffer();
      int i = 0;

      for(int c = text.length(); i < c; ++i) {
         char ch = text.charAt(i);
         if (ch > 127) {
            sb.append(replacement);
         } else {
            sb.append(ch);
         }
      }

      return sb.toString();
   }
}
