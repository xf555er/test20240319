package org.apache.batik.gvt.text;

import java.awt.Composite;
import java.awt.Paint;
import java.awt.Stroke;

public class TextPaintInfo {
   public boolean visible;
   public Paint fillPaint;
   public Paint strokePaint;
   public Stroke strokeStroke;
   public Composite composite;
   public Paint underlinePaint;
   public Paint underlineStrokePaint;
   public Stroke underlineStroke;
   public Paint overlinePaint;
   public Paint overlineStrokePaint;
   public Stroke overlineStroke;
   public Paint strikethroughPaint;
   public Paint strikethroughStrokePaint;
   public Stroke strikethroughStroke;
   public int startChar;
   public int endChar;

   public TextPaintInfo() {
   }

   public TextPaintInfo(TextPaintInfo pi) {
      this.set(pi);
   }

   public void set(TextPaintInfo pi) {
      if (pi == null) {
         this.fillPaint = null;
         this.strokePaint = null;
         this.strokeStroke = null;
         this.composite = null;
         this.underlinePaint = null;
         this.underlineStrokePaint = null;
         this.underlineStroke = null;
         this.overlinePaint = null;
         this.overlineStrokePaint = null;
         this.overlineStroke = null;
         this.strikethroughPaint = null;
         this.strikethroughStrokePaint = null;
         this.strikethroughStroke = null;
         this.visible = false;
      } else {
         this.fillPaint = pi.fillPaint;
         this.strokePaint = pi.strokePaint;
         this.strokeStroke = pi.strokeStroke;
         this.composite = pi.composite;
         this.underlinePaint = pi.underlinePaint;
         this.underlineStrokePaint = pi.underlineStrokePaint;
         this.underlineStroke = pi.underlineStroke;
         this.overlinePaint = pi.overlinePaint;
         this.overlineStrokePaint = pi.overlineStrokePaint;
         this.overlineStroke = pi.overlineStroke;
         this.strikethroughPaint = pi.strikethroughPaint;
         this.strikethroughStrokePaint = pi.strikethroughStrokePaint;
         this.strikethroughStroke = pi.strikethroughStroke;
         this.visible = pi.visible;
      }

   }

   public static boolean equivilent(TextPaintInfo tpi1, TextPaintInfo tpi2) {
      if (tpi1 == null) {
         return tpi2 == null;
      } else if (tpi2 == null) {
         return false;
      } else if (tpi1.fillPaint == null != (tpi2.fillPaint == null)) {
         return false;
      } else if (tpi1.visible != tpi2.visible) {
         return false;
      } else {
         boolean tpi1Stroke = tpi1.strokePaint != null && tpi1.strokeStroke != null;
         boolean tpi2Stroke = tpi2.strokePaint != null && tpi2.strokeStroke != null;
         return tpi1Stroke == tpi2Stroke;
      }
   }
}
