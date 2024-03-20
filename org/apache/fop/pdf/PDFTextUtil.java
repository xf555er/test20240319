package org.apache.fop.pdf;

import java.awt.geom.AffineTransform;

public abstract class PDFTextUtil {
   private static final int DEC = 8;
   public static final int TR_FILL = 0;
   public static final int TR_STROKE = 1;
   public static final int TR_FILL_STROKE = 2;
   public static final int TR_INVISIBLE = 3;
   public static final int TR_FILL_CLIP = 4;
   public static final int TR_STROKE_CLIP = 5;
   public static final int TR_FILL_STROKE_CLIP = 6;
   public static final int TR_CLIP = 7;
   private boolean inTextObject;
   private String startText;
   private String endText;
   private boolean useMultiByte;
   private boolean useCid;
   private StringBuffer bufTJ;
   private int textRenderingMode = 0;
   private String currentFontName;
   private double currentFontSize;

   protected abstract void write(String var1);

   protected abstract void write(StringBuffer var1);

   private void writeAffineTransform(AffineTransform at, StringBuffer sb) {
      double[] lt = new double[6];
      at.getMatrix(lt);
      PDFNumber.doubleOut(lt[0], 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(lt[1], 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(lt[2], 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(lt[3], 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(lt[4], 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(lt[5], 8, sb);
   }

   private static void writeChar(int codePoint, StringBuffer sb, boolean multibyte, boolean cid) {
      if (!multibyte) {
         if (!cid && codePoint >= 32 && codePoint <= 127) {
            switch (codePoint) {
               case 40:
               case 41:
               case 92:
                  sb.append('\\');
               default:
                  sb.appendCodePoint(codePoint);
            }
         } else {
            sb.append("\\").append(Integer.toOctalString(codePoint));
         }
      } else {
         PDFText.toUnicodeHex(codePoint, sb);
      }

   }

   private void writeChar(int codePoint, StringBuffer sb) {
      writeChar(codePoint, sb, this.useMultiByte, this.useCid);
   }

   private void checkInTextObject() {
      if (!this.inTextObject) {
         throw new IllegalStateException("Not in text object");
      }
   }

   public boolean isInTextObject() {
      return this.inTextObject;
   }

   public void beginTextObject() {
      if (this.inTextObject) {
         throw new IllegalStateException("Already in text object");
      } else {
         this.write("BT\n");
         this.inTextObject = true;
      }
   }

   public void endTextObject() {
      this.checkInTextObject();
      this.write("ET\n");
      this.inTextObject = false;
      this.initValues();
   }

   protected void initValues() {
      this.currentFontName = null;
      this.currentFontSize = 0.0;
      this.textRenderingMode = 0;
   }

   public void concatMatrix(AffineTransform at) {
      if (!at.isIdentity()) {
         this.writeTJ();
         StringBuffer sb = new StringBuffer();
         this.writeAffineTransform(at, sb);
         sb.append(" cm\n");
         this.write(sb);
      }

   }

   public void writeTf(String fontName, double fontSize) {
      this.checkInTextObject();
      StringBuffer sb = new StringBuffer();
      sb.append('/');
      sb.append(fontName);
      sb.append(' ');
      PDFNumber.doubleOut(fontSize, 6, sb);
      sb.append(" Tf\n");
      this.write(sb);
      this.startText = this.useMultiByte ? "<" : "(";
      this.endText = this.useMultiByte ? ">" : ")";
   }

   public void updateTf(String fontName, double fontSize, boolean multiByte, boolean cid) {
      this.checkInTextObject();
      if (!fontName.equals(this.currentFontName) || fontSize != this.currentFontSize) {
         this.writeTJ();
         this.currentFontName = fontName;
         this.currentFontSize = fontSize;
         this.useMultiByte = multiByte;
         this.useCid = cid;
         this.writeTf(fontName, fontSize);
      }

   }

   public void setTextRenderingMode(int mode) {
      if (mode >= 0 && mode <= 7) {
         if (mode != this.textRenderingMode) {
            this.writeTJ();
            this.textRenderingMode = mode;
            this.write(this.textRenderingMode + " Tr\n");
         }

      } else {
         throw new IllegalArgumentException("Illegal value for text rendering mode. Expected: 0-7");
      }
   }

   public void setTextRenderingMode(boolean fill, boolean stroke, boolean addToClip) {
      int mode;
      if (fill) {
         mode = stroke ? 2 : 0;
      } else {
         mode = stroke ? 1 : 3;
      }

      if (addToClip) {
         mode += 4;
      }

      this.setTextRenderingMode(mode);
   }

   public void writeTextMatrix(AffineTransform localTransform) {
      StringBuffer sb = new StringBuffer();
      this.writeAffineTransform(localTransform, sb);
      sb.append(" Tm ");
      this.write(sb);
   }

   public void writeTJMappedChar(char ch) {
      this.writeTJMappedCodePoint(ch);
   }

   public void writeTJMappedCodePoint(int codePoint) {
      if (this.bufTJ == null) {
         this.bufTJ = new StringBuffer();
      }

      if (this.bufTJ.length() == 0) {
         this.bufTJ.append('[');
         this.bufTJ.append(this.startText);
      }

      this.writeChar(codePoint, this.bufTJ);
   }

   public void adjustGlyphTJ(double adjust) {
      if (this.bufTJ == null) {
         this.bufTJ = new StringBuffer();
      }

      if (this.bufTJ.length() == 0) {
         this.bufTJ.append('[');
      } else {
         this.bufTJ.append(this.endText);
         this.bufTJ.append(' ');
      }

      PDFNumber.doubleOut(adjust, 4, this.bufTJ);
      this.bufTJ.append(' ');
      this.bufTJ.append(this.startText);
   }

   public void writeTJ() {
      if (this.isInString()) {
         this.bufTJ.append(this.endText);
         this.bufTJ.append("] TJ\n");
         this.write(this.bufTJ);
         this.bufTJ.setLength(0);
      }

   }

   private boolean isInString() {
      return this.bufTJ != null && this.bufTJ.length() > 0;
   }

   public void writeTd(double x, double y) {
      StringBuffer sb = new StringBuffer();
      PDFNumber.doubleOut(x, 8, sb);
      sb.append(' ');
      PDFNumber.doubleOut(y, 8, sb);
      sb.append(" Td\n");
      this.write(sb);
   }

   public void writeTj(char ch, boolean multibyte, boolean cid) {
      StringBuffer sb = new StringBuffer();
      sb.append(this.startText);
      writeChar(ch, sb, multibyte, cid);
      sb.append(this.endText);
      sb.append(" Tj\n");
      this.write(sb);
   }
}
