package org.apache.fop.complexscripts.bidi;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.apache.fop.area.inline.Anchor;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.util.CharUtilities;

public class InlineRun {
   private InlineArea inline;
   private int[] levels;
   private int minLevel;
   private int maxLevel;
   private int reversals;

   public InlineRun(InlineArea inline, int[] levels) {
      assert inline != null;

      assert levels != null;

      this.inline = inline;
      this.levels = levels;
      this.setMinMax(levels);
   }

   public InlineRun(InlineArea inline, int level, int count) {
      this(inline, makeLevels(level, count));
   }

   public InlineArea getInline() {
      return this.inline;
   }

   public int getMinLevel() {
      return this.minLevel;
   }

   public int getMaxLevel() {
      return this.maxLevel;
   }

   private void setMinMax(int[] levels) {
      int mn = Integer.MAX_VALUE;
      int mx = Integer.MIN_VALUE;
      if (levels != null && levels.length > 0) {
         int[] var4 = levels;
         int var5 = levels.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int l = var4[var6];
            if (l < mn) {
               mn = l;
            }

            if (l > mx) {
               mx = l;
            }
         }
      } else {
         mx = -1;
         mn = -1;
      }

      this.minLevel = mn;
      this.maxLevel = mx;
   }

   public boolean isHomogenous() {
      return this.minLevel == this.maxLevel;
   }

   public List split() {
      List runs = new Vector();
      int i = 0;

      int e;
      for(int n = this.levels.length; i < n; i = e) {
         int l = this.levels[i];

         for(e = i; e < n && this.levels[e] == l; ++e) {
         }

         if (i < e) {
            runs.add(new InlineRun(this.inline, l, e - i));
         }
      }

      assert runs.size() < 2 : "heterogeneous inlines not yet supported!!";

      return runs;
   }

   public void updateMinMax(int[] mm) {
      if (this.minLevel < mm[0]) {
         mm[0] = this.minLevel;
      }

      if (this.maxLevel > mm[1]) {
         mm[1] = this.maxLevel;
      }

   }

   public boolean maybeNeedsMirroring() {
      return this.minLevel == this.maxLevel && this.minLevel > 0 && (this.minLevel & 1) != 0;
   }

   public void reverse() {
      ++this.reversals;
   }

   public void maybeReverseWord(boolean mirror) {
      if (this.inline instanceof WordArea) {
         WordArea w = (WordArea)this.inline;
         if (!w.isReversed()) {
            if ((this.reversals & 1) != 0) {
               w.reverse(mirror);
            } else if (mirror && this.maybeNeedsMirroring()) {
               w.mirror();
            }
         }
      }

   }

   public boolean equals(Object o) {
      if (!(o instanceof InlineRun)) {
         return false;
      } else {
         InlineRun ir = (InlineRun)o;
         if (ir.inline != this.inline) {
            return false;
         } else if (ir.minLevel != this.minLevel) {
            return false;
         } else if (ir.maxLevel != this.maxLevel) {
            return false;
         } else if (ir.levels != null && this.levels != null) {
            if (ir.levels.length != this.levels.length) {
               return false;
            } else {
               int i = 0;

               for(int n = this.levels.length; i < n; ++i) {
                  if (ir.levels[i] != this.levels[i]) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return ir.levels == null && this.levels == null;
         }
      }
   }

   public int hashCode() {
      int l = this.inline != null ? this.inline.hashCode() : 0;
      l = (l ^ this.minLevel) + (l << 19);
      l = (l ^ this.maxLevel) + (l << 11);
      return l;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("RR: { type = '");
      String content = null;
      char c;
      if (this.inline instanceof WordArea) {
         c = 'W';
         content = ((WordArea)this.inline).getWord();
      } else if (this.inline instanceof SpaceArea) {
         c = 'S';
         content = ((SpaceArea)this.inline).getSpace();
      } else if (this.inline instanceof Anchor) {
         c = 'A';
      } else if (this.inline instanceof Leader) {
         c = 'L';
      } else if (this.inline instanceof Space) {
         c = 'S';
      } else if (this.inline instanceof UnresolvedPageNumber) {
         c = '#';
         content = ((UnresolvedPageNumber)this.inline).getText();
      } else if (this.inline instanceof InlineBlockParent) {
         c = 'B';
      } else if (this.inline instanceof InlineViewport) {
         c = 'V';
      } else if (this.inline instanceof InlineParent) {
         c = 'I';
      } else {
         c = '?';
      }

      sb.append(c);
      sb.append("', levels = '");
      sb.append(this.generateLevels(this.levels));
      sb.append("', min = ");
      sb.append(this.minLevel);
      sb.append(", max = ");
      sb.append(this.maxLevel);
      sb.append(", reversals = ");
      sb.append(this.reversals);
      sb.append(", content = <");
      sb.append(CharUtilities.toNCRefs(content));
      sb.append("> }");
      return sb.toString();
   }

   private String generateLevels(int[] levels) {
      StringBuffer lb = new StringBuffer();
      int maxLevel = -1;
      int numLevels = levels.length;
      int[] var5 = levels;
      int var6 = levels.length;

      int var7;
      int level;
      for(var7 = 0; var7 < var6; ++var7) {
         level = var5[var7];
         if (level > maxLevel) {
            maxLevel = level;
         }
      }

      if (maxLevel >= 0) {
         if (maxLevel < 10) {
            var5 = levels;
            var6 = levels.length;

            for(var7 = 0; var7 < var6; ++var7) {
               level = var5[var7];
               lb.append((char)(48 + level));
            }
         } else {
            boolean first = true;
            int[] var11 = levels;
            var7 = levels.length;

            for(level = 0; level < var7; ++level) {
               int level = var11[level];
               if (first) {
                  first = false;
               } else {
                  lb.append(',');
               }

               lb.append(level);
            }
         }
      }

      return lb.toString();
   }

   private static int[] makeLevels(int level, int count) {
      int[] levels = new int[count > 0 ? count : 1];
      Arrays.fill(levels, level);
      return levels;
   }
}
