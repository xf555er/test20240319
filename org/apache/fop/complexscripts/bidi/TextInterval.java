package org.apache.fop.complexscripts.bidi;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.PageNumber;

class TextInterval {
   private FONode fn;
   private int textStart;
   private int start;
   private int end;
   private int level;

   TextInterval(FONode fn, int start, int end) {
      this(fn, start, start, end, -1);
   }

   TextInterval(FONode fn, int textStart, int start, int end, int level) {
      this.fn = fn;
      this.textStart = textStart;
      this.start = start;
      this.end = end;
      this.level = level;
   }

   FONode getNode() {
      return this.fn;
   }

   int getTextStart() {
      return this.textStart;
   }

   int getStart() {
      return this.start;
   }

   int getEnd() {
      return this.end;
   }

   int getLevel() {
      return this.level;
   }

   void setLevel(int level) {
      this.level = level;
   }

   public int length() {
      return this.end - this.start;
   }

   public String getText() {
      if (this.fn instanceof FOText) {
         return ((FOText)this.fn).getCharSequence().toString();
      } else {
         return this.fn instanceof Character ? new String(new char[]{((Character)this.fn).getCharacter()}) : null;
      }
   }

   public void assignTextLevels() {
      if (this.fn instanceof FOText) {
         ((FOText)this.fn).setBidiLevel(this.level, this.start - this.textStart, this.end - this.textStart);
      } else if (this.fn instanceof Character) {
         ((Character)this.fn).setBidiLevel(this.level);
      } else if (this.fn instanceof AbstractPageNumberCitation) {
         ((AbstractPageNumberCitation)this.fn).setBidiLevel(this.level);
      } else if (this.fn instanceof AbstractGraphics) {
         ((AbstractGraphics)this.fn).setBidiLevel(this.level);
      } else if (this.fn instanceof Leader) {
         ((Leader)this.fn).setBidiLevel(this.level);
      } else if (this.fn instanceof PageNumber) {
         ((PageNumber)this.fn).setBidiLevel(this.level);
      }

   }

   public boolean equals(Object o) {
      if (o instanceof TextInterval) {
         TextInterval ti = (TextInterval)o;
         if (ti.getNode() != this.fn) {
            return false;
         } else if (ti.getStart() != this.start) {
            return false;
         } else {
            return ti.getEnd() == this.end;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int l = this.fn != null ? this.fn.hashCode() : 0;
      l = (l ^ this.start) + (l << 19);
      l = (l ^ this.end) + (l << 11);
      return l;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      char c;
      if (this.fn instanceof FOText) {
         c = 'T';
      } else if (this.fn instanceof Character) {
         c = 'C';
      } else if (this.fn instanceof BidiOverride) {
         c = 'B';
      } else if (this.fn instanceof AbstractPageNumberCitation) {
         c = '#';
      } else if (this.fn instanceof AbstractGraphics) {
         c = 'G';
      } else if (this.fn instanceof Leader) {
         c = 'L';
      } else if (this.fn instanceof PageNumber) {
         c = '#';
      } else {
         c = '?';
      }

      sb.append(c);
      sb.append("[" + this.start + "," + this.end + "][" + this.textStart + "](" + this.level + ")");
      return sb.toString();
   }
}
