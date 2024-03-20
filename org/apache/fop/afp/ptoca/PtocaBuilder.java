package org.apache.fop.afp.ptoca;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.afp.fonts.CharactersetEncoder;
import org.apache.fop.afp.modca.AxisOrientation;
import org.apache.fop.util.OCAColor;
import org.apache.fop.util.OCAColorSpace;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

public abstract class PtocaBuilder implements PtocaConstants {
   private ByteArrayOutputStream baout = new ByteArrayOutputStream(256);
   private int currentX = -1;
   private int currentY = -1;
   private int currentFont = Integer.MIN_VALUE;
   private int currentOrientation;
   private Color currentColor;
   private int currentVariableSpaceCharacterIncrement;
   private int currentInterCharacterAdjustment;

   public PtocaBuilder() {
      this.currentColor = Color.BLACK;
   }

   protected abstract OutputStream getOutputStreamForControlSequence(int var1);

   private static byte chained(byte functionType) {
      return (byte)(functionType | 1);
   }

   private void newControlSequence() {
      this.baout.reset();
   }

   private void commit(byte functionType) throws IOException {
      int length = this.baout.size() + 2;

      assert length < 256;

      OutputStream out = this.getOutputStreamForControlSequence(length);
      out.write(length);
      out.write(functionType);
      this.baout.writeTo(out);
   }

   private void writeBytes(int... data) {
      int[] var2 = data;
      int var3 = data.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int d = var2[var4];
         this.baout.write(d);
      }

   }

   private void writeShort(int data) {
      this.baout.write(data >>> 8 & 255);
      this.baout.write(data & 255);
   }

   public void writeIntroducer() throws IOException {
      OutputStream out = this.getOutputStreamForControlSequence(ESCAPE.length);
      out.write(ESCAPE);
   }

   public void setCodedFont(byte font) throws IOException {
      if (this.currentFont != font) {
         this.currentFont = font;
         this.newControlSequence();
         this.writeBytes(font);
         this.commit(chained((byte)-16));
      }
   }

   public void absoluteMoveInline(int coordinate) throws IOException {
      if (coordinate != this.currentX) {
         this.newControlSequence();
         this.writeShort(coordinate);
         this.commit(chained((byte)-58));
         this.currentX = coordinate;
      }
   }

   public void relativeMoveInline(int increment) throws IOException {
      this.newControlSequence();
      this.writeShort(increment);
      this.commit(chained((byte)-56));
   }

   public void absoluteMoveBaseline(int coordinate) throws IOException {
      if (coordinate != this.currentY) {
         this.newControlSequence();
         this.writeShort(coordinate);
         this.commit(chained((byte)-46));
         this.currentY = coordinate;
         this.currentX = -1;
      }
   }

   public void addTransparentData(CharactersetEncoder.EncodedChars encodedChars) throws IOException {
      Iterator var2 = (new TransparentDataControlSequence(encodedChars)).iterator();

      while(var2.hasNext()) {
         TransparentDataControlSequence.TransparentData trn = (TransparentDataControlSequence.TransparentData)var2.next();
         this.newControlSequence();
         trn.writeTo(this.baout);
         this.commit(chained((byte)-38));
      }

   }

   public void drawBaxisRule(int length, int width) throws IOException {
      this.newControlSequence();
      this.writeShort(length);
      this.writeShort(width);
      this.writeBytes(0);
      this.commit(chained((byte)-26));
   }

   public void drawIaxisRule(int length, int width) throws IOException {
      this.newControlSequence();
      this.writeShort(length);
      this.writeShort(width);
      this.writeBytes(0);
      this.commit(chained((byte)-28));
   }

   public void setTextOrientation(int orientation) throws IOException {
      if (orientation != this.currentOrientation) {
         this.newControlSequence();
         AxisOrientation.getRightHandedAxisOrientationFor(orientation).writeTo(this.baout);
         this.commit(chained((byte)-10));
         this.currentOrientation = orientation;
         this.currentX = -1;
         this.currentY = -1;
      }
   }

   public void setExtendedTextColor(Color col) throws IOException {
      if (!ColorUtil.isSameColor(col, this.currentColor)) {
         if (col instanceof ColorWithAlternatives) {
            ColorWithAlternatives cwa = (ColorWithAlternatives)col;
            Color alt = cwa.getFirstAlternativeOfType(9);
            if (alt != null) {
               col = alt;
            }
         }

         ColorSpace cs = col.getColorSpace();
         this.newControlSequence();
         int i;
         int component;
         float[] comps;
         if (col.getColorSpace().getType() == 9) {
            this.writeBytes(0, 4, 0, 0, 0, 0);
            this.writeBytes(8, 8, 8, 8);
            comps = col.getColorComponents((float[])null);

            assert comps.length == 4;

            for(i = 0; i < 4; ++i) {
               component = Math.round(comps[i] * 255.0F);
               this.writeBytes(component);
            }
         } else if (cs instanceof CIELabColorSpace) {
            this.writeBytes(0, 8, 0, 0, 0, 0);
            this.writeBytes(8, 8, 8, 0);
            comps = col.getColorComponents((float[])null);
            i = Math.round(comps[0] * 255.0F);
            component = Math.round(comps[1] * 255.0F) - 128;
            int b = Math.round(comps[2] * 255.0F) - 128;
            this.writeBytes(i, component, b);
         } else if (cs instanceof OCAColorSpace) {
            this.writeBytes(0, 64, 0, 0, 0, 0);
            this.writeBytes(16, 0, 0, 0);
            int ocaColor = ((OCAColor)col).getOCA();
            this.writeBytes((ocaColor & '\uff00') >> 8, ocaColor & 255);
         } else {
            this.writeBytes(0, 1, 0, 0, 0, 0);
            this.writeBytes(8, 8, 8, 0);
            this.writeBytes(col.getRed(), col.getGreen(), col.getBlue());
         }

         this.commit(chained((byte)-128));
         this.currentColor = col;
      }
   }

   public void setVariableSpaceCharacterIncrement(int incr) throws IOException {
      if (incr != this.currentVariableSpaceCharacterIncrement) {
         assert incr >= 0 && incr < 65536;

         this.newControlSequence();
         this.writeShort(Math.abs(incr));
         this.commit(chained((byte)-60));
         this.currentVariableSpaceCharacterIncrement = incr;
      }
   }

   public void setInterCharacterAdjustment(int incr) throws IOException {
      if (incr != this.currentInterCharacterAdjustment) {
         assert incr >= -32768 && incr <= 32767;

         this.newControlSequence();
         this.writeShort(Math.abs(incr));
         this.writeBytes(incr >= 0 ? 0 : 1);
         this.commit(chained((byte)-62));
         this.currentInterCharacterAdjustment = incr;
      }
   }

   public void endChainedControlSequence() throws IOException {
      this.newControlSequence();
      this.commit((byte)-8);
   }
}
