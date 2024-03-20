package org.apache.fop.svg.font;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.Iterator;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.CharMirror;
import org.apache.fop.fonts.Font;

class ComplexGlyphVector extends FOPGVTGlyphVector {
   public static final AttributedCharacterIterator.Attribute WRITING_MODE;
   public static final Integer WRITING_MODE_RTL;
   private boolean reversed;
   private boolean mirrored;

   ComplexGlyphVector(FOPGVTFont font, CharacterIterator iter, FontRenderContext frc) {
      super(font, iter, frc);
   }

   public void performDefaultLayout() {
      super.performDefaultLayout();
   }

   public boolean isReversed() {
      return this.reversed;
   }

   public void maybeReverse(boolean mirror) {
      if (!this.reversed) {
         if (this.glyphs != null) {
            if (this.glyphs.length > 1) {
               reverse(this.glyphs);
               if (this.associations != null) {
                  Collections.reverse(this.associations);
               }

               if (this.gposAdjustments != null) {
                  reverse(this.gposAdjustments);
               }

               if (this.positions != null) {
                  reverse(this.positions);
               }

               if (this.boundingBoxes != null) {
                  reverse(this.boundingBoxes);
               }

               if (this.glyphTransforms != null) {
                  reverse(this.glyphTransforms);
               }

               if (this.glyphVisibilities != null) {
                  reverse(this.glyphVisibilities);
               }
            }

            if (this.maybeMirror()) {
               this.mirrored = true;
            }
         }

         this.reversed = true;
      }

   }

   private boolean maybeMirror() {
      boolean mirrored = false;
      String s = this.text.subSequence(this.text.getBeginIndex(), this.text.getEndIndex()).toString();
      if (CharMirror.hasMirrorable(s)) {
         String m = CharMirror.mirror(s);

         assert m.length() == s.length();

         int i = 0;

         for(int n = m.length(); i < n; ++i) {
            char cs = s.charAt(i);
            char cm = m.charAt(i);
            if (cm != cs && this.substituteMirroredGlyph(i, cm)) {
               mirrored = true;
            }
         }
      }

      return mirrored;
   }

   private boolean substituteMirroredGlyph(int index, char mirror) {
      Font f = this.font.getFont();
      int gi = 0;

      for(Iterator var5 = this.associations.iterator(); var5.hasNext(); ++gi) {
         CharAssociation ca = (CharAssociation)var5.next();
         if (ca.contained(index, 1)) {
            this.setGlyphCode(gi, f.mapChar(mirror));
            return true;
         }
      }

      return false;
   }

   private static void reverse(boolean[] ba) {
      int i = 0;
      int n = ba.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         boolean t = ba[k];
         ba[k] = ba[i];
         ba[i] = t;
      }

   }

   private static void reverse(int[] ia) {
      int i = 0;
      int n = ia.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         int t = ia[k];
         ia[k] = ia[i];
         ia[i] = t;
      }

   }

   private static void reverse(int[][] iaa) {
      int i = 0;
      int n = iaa.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         int[] t = iaa[k];
         iaa[k] = iaa[i];
         iaa[i] = t;
      }

   }

   private static void reverse(float[] fa) {
      int skip = 2;
      int numPositions = fa.length / skip;
      int i = 0;
      int i = numPositions;

      int n;
      int k;
      for(n = numPositions / 2; i < n; ++i) {
         k = i - i - 1;

         for(int k = 0; k < skip; ++k) {
            int l1 = i * skip + k;
            int l2 = k * skip + k;
            float t = fa[l2];
            fa[l2] = fa[l1];
            fa[l1] = t;
         }
      }

      float runAdvanceX = fa[0];
      i = 0;

      for(n = numPositions; i < n; ++i) {
         k = i * 2;
         fa[k + 0] = runAdvanceX - fa[k + 0];
         if (i > 0) {
            fa[k - 1] = fa[k + 1];
         }
      }

   }

   private static void reverse(Rectangle2D[] ra) {
      int i = 0;
      int n = ra.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         Rectangle2D t = ra[k];
         ra[k] = ra[i];
         ra[i] = t;
      }

   }

   private static void reverse(AffineTransform[] ta) {
      int i = 0;
      int n = ta.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         AffineTransform t = ta[k];
         ta[k] = ta[i];
         ta[i] = t;
      }

   }

   static {
      WRITING_MODE = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
      WRITING_MODE_RTL = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL;
   }
}
