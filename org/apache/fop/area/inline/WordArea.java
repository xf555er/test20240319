package org.apache.fop.area.inline;

import java.util.Arrays;
import java.util.List;
import org.apache.fop.complexscripts.bidi.InlineRun;
import org.apache.fop.complexscripts.util.CharMirror;

public class WordArea extends InlineArea {
   private static final long serialVersionUID = 6444644662158970942L;
   protected String word;
   protected int[] letterAdjust;
   protected int[] levels;
   protected int[][] gposAdjustments;
   protected boolean reversed;
   private boolean nextIsSpace;

   public WordArea(int blockProgressionOffset, int level, String word, int[] letterAdjust, int[] levels, int[][] gposAdjustments, boolean reversed, boolean nextIsSpace) {
      super(blockProgressionOffset, level);
      int length = word != null ? word.length() : 0;
      this.word = word;
      this.letterAdjust = maybeAdjustLength(letterAdjust, length);
      this.levels = maybePopulateLevels(levels, level, length);
      this.gposAdjustments = maybeAdjustLength(gposAdjustments, length);
      this.reversed = reversed;
      this.nextIsSpace = nextIsSpace;
   }

   public WordArea(int blockProgressionOffset, int level, String word, int[] letterAdjust, int[] levels, int[][] gposAdjustments, boolean reversed) {
      this(blockProgressionOffset, level, word, letterAdjust, levels, gposAdjustments, reversed, false);
   }

   public WordArea(int blockProgressionOffset, int level, String word, int[] letterAdjust, int[] levels, int[][] gposAdjustments) {
      this(blockProgressionOffset, level, word, letterAdjust, levels, gposAdjustments, false);
   }

   public String getWord() {
      return this.word;
   }

   public int[] getLetterAdjustArray() {
      return this.letterAdjust;
   }

   public int[] getBidiLevels() {
      return this.levels;
   }

   public int[] getBidiLevels(int start, int end) {
      assert start <= end;

      if (this.levels != null) {
         int n = end - start;
         int[] levels = new int[n];
         System.arraycopy(this.levels, start + 0, levels, 0, n);
         return levels;
      } else {
         return null;
      }
   }

   public int bidiLevelAt(int position) {
      if (position > this.word.length()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.levels != null ? this.levels[position] : -1;
      }
   }

   public List collectInlineRuns(List runs) {
      assert runs != null;

      int[] levels = this.getBidiLevels();
      InlineRun r;
      if (levels != null && levels.length > 0) {
         r = new InlineRun(this, levels);
      } else {
         r = new InlineRun(this, this.getBidiLevel(), this.word.length());
      }

      runs.add(r);
      return runs;
   }

   public int[][] getGlyphPositionAdjustments() {
      return this.gposAdjustments;
   }

   public int[] glyphPositionAdjustmentsAt(int position) {
      if (position > this.word.length()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.gposAdjustments != null ? this.gposAdjustments[position] : null;
      }
   }

   public void reverse(boolean mirror) {
      if (this.word.length() > 0) {
         this.word = (new StringBuffer(this.word)).reverse().toString();
         if (this.levels != null) {
            reverse(this.levels);
         }

         if (this.gposAdjustments != null) {
            reverse(this.gposAdjustments);
         }

         this.reversed = !this.reversed;
         if (mirror) {
            this.word = CharMirror.mirror(this.word);
         }
      }

   }

   public void mirror() {
      if (this.word.length() > 0) {
         this.word = CharMirror.mirror(this.word);
      }

   }

   public boolean isReversed() {
      return this.reversed;
   }

   public boolean isNextIsSpace() {
      return this.nextIsSpace;
   }

   private static int[] maybeAdjustLength(int[] ia, int length) {
      if (ia == null) {
         return ia;
      } else if (ia.length == length) {
         return ia;
      } else {
         int[] iaNew = new int[length];
         int i = 0;

         for(int n = ia.length; i < n && i < length; ++i) {
            iaNew[i] = ia[i];
         }

         return iaNew;
      }
   }

   private static int[][] maybeAdjustLength(int[][] im, int length) {
      if (im == null) {
         return im;
      } else if (im.length == length) {
         return im;
      } else {
         int[][] imNew = new int[length][];
         int i = 0;

         for(int n = im.length; i < n && i < length; ++i) {
            imNew[i] = im[i];
         }

         return imNew;
      }
   }

   private static int[] maybePopulateLevels(int[] levels, int level, int count) {
      if (levels == null && level >= 0) {
         levels = new int[count];
         Arrays.fill(levels, level);
      }

      return maybeAdjustLength(levels, count);
   }

   private static void reverse(int[] a) {
      int i = 0;
      int n = a.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         int t = a[k];
         a[k] = a[i];
         a[i] = t;
      }

   }

   private static void reverse(int[][] aa) {
      int i = 0;
      int n = aa.length;

      for(int m = n / 2; i < m; ++i) {
         int k = n - i - 1;
         int[] t = aa[k];
         aa[k] = aa[i];
         aa[i] = t;
      }

   }
}
