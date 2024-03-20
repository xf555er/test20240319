package org.apache.fop.area.inline;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.fop.util.CharUtilities;

public class TextArea extends AbstractTextArea {
   private static final long serialVersionUID = 7315900267242540809L;
   private boolean isHyphenated;

   public TextArea() {
   }

   public TextArea(int stretch, int shrink, int adj) {
      super(stretch, shrink, adj);
   }

   public void removeText() {
      this.inlines.clear();
   }

   public void addWord(String word, int offset) {
      this.addWord(word, 0, (int[])null, (int[])null, (int[][])null, offset);
   }

   public void addWord(String word, int offset, int level) {
      this.addWord(word, 0, (int[])null, this.makeLevels(level, word.length()), (int[][])null, offset);
   }

   public void addWord(String word, int ipd, int[] letterAdjust, int[] levels, int[][] gposAdjustments, int blockProgressionOffset, boolean nextIsSpace) {
      int minWordLevel = findMinLevel(levels, this.getBidiLevel());
      WordArea wordArea = new WordArea(blockProgressionOffset, minWordLevel, word, letterAdjust, levels, gposAdjustments, false, nextIsSpace);
      wordArea.setIPD(ipd);
      wordArea.setChangeBarList(this.getChangeBarList());
      this.addChildArea(wordArea);
      wordArea.setParentArea(this);
      this.updateLevel(minWordLevel);
   }

   public void addWord(String word, int ipd, int[] letterAdjust, int[] levels, int[][] gposAdjustments, int blockProgressionOffset) {
      this.addWord(word, ipd, letterAdjust, levels, gposAdjustments, blockProgressionOffset, false);
   }

   public void addSpace(char space, int ipd, boolean adjustable, int blockProgressionOffset, int level) {
      SpaceArea spaceArea = new SpaceArea(blockProgressionOffset, level, space, adjustable);
      spaceArea.setIPD(ipd);
      spaceArea.setChangeBarList(this.getChangeBarList());
      this.addChildArea(spaceArea);
      spaceArea.setParentArea(this);
      this.updateLevel(level);
   }

   public void setHyphenated() {
      this.isHyphenated = true;
   }

   public boolean isHyphenated() {
      return this.isHyphenated;
   }

   public String getText() {
      StringBuilder text = new StringBuilder();
      Iterator var2 = this.inlines.iterator();

      while(var2.hasNext()) {
         InlineArea inline = (InlineArea)var2.next();
         if (inline instanceof WordArea) {
            text.append(((WordArea)inline).getWord());
         } else {
            assert inline instanceof SpaceArea;

            text.append(((SpaceArea)inline).getSpace());
         }
      }

      return text.toString();
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append(" {text=\"");
      sb.append(CharUtilities.toNCRefs(this.getText()));
      sb.append("\"");
      sb.append("}");
      return sb.toString();
   }

   public void updateLevel(int newLevel) {
      if (newLevel >= 0) {
         int curLevel = this.getBidiLevel();
         if (curLevel >= 0) {
            if (newLevel < curLevel) {
               this.setBidiLevel(newLevel);
            }
         } else {
            this.setBidiLevel(newLevel);
         }
      }

   }

   private static int findMinLevel(int[] levels, int defaultLevel) {
      if (levels != null) {
         int lMin = Integer.MAX_VALUE;
         int[] var3 = levels;
         int var4 = levels.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            int l = var3[var5];
            if (l >= 0 && l < lMin) {
               lMin = l;
            }
         }

         if (lMin == Integer.MAX_VALUE) {
            return -1;
         } else {
            return lMin;
         }
      } else {
         return defaultLevel;
      }
   }

   private int[] makeLevels(int level, int count) {
      if (level >= 0) {
         int[] levels = new int[count];
         Arrays.fill(levels, level);
         return levels;
      } else {
         return null;
      }
   }

   public int getEffectiveIPD() {
      return this.getIPD();
   }
}
