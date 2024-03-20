package org.apache.fop.svg.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.Set;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.fop.complexscripts.bidi.UnicodeBidiAlgorithm;
import org.apache.fop.traits.Direction;

public class BidiAttributedCharacterIterator implements AttributedCharacterIterator {
   private AttributedCharacterIterator aci;

   protected BidiAttributedCharacterIterator(AttributedCharacterIterator aci) {
      this.aci = aci;
   }

   public BidiAttributedCharacterIterator(AttributedCharacterIterator aci, int defaultBidiLevel) {
      this(annotateBidiLevels(aci, defaultBidiLevel));
   }

   private static AttributedCharacterIterator annotateBidiLevels(AttributedCharacterIterator aci, int defaultBidiLevel) {
      int start = aci.getBeginIndex();
      int end = aci.getEndIndex();
      int numChars = end - start;
      StringBuffer sb = new StringBuffer(numChars);

      for(int i = 0; i < numChars; ++i) {
         char ch = aci.setIndex(i);

         assert ch != '\uffff';

         sb.append(ch);
      }

      int[] levels = UnicodeBidiAlgorithm.resolveLevels(sb, (defaultBidiLevel & 1) == 1 ? Direction.RL : Direction.LR);
      if (levels != null) {
         assert levels.length == numChars;

         AttributedString as = new AttributedString(aci, start, end);
         int runStart = 0;
         int nextRunLevel = true;
         int currRunLevel = -1;
         int i = 0;

         for(int n = levels.length; i < n; ++i) {
            int nextRunLevel = levels[i];
            if (currRunLevel < 0) {
               currRunLevel = nextRunLevel;
            } else if (nextRunLevel != currRunLevel) {
               as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL, currRunLevel, runStart, i);
               runStart = i;
               currRunLevel = nextRunLevel;
            }
         }

         if (currRunLevel >= 0 && end > runStart) {
            as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL, currRunLevel, runStart, end);
         }

         return as.getIterator();
      } else {
         return aci;
      }
   }

   public char first() {
      return this.aci.first();
   }

   public char last() {
      return this.aci.last();
   }

   public char current() {
      return this.aci.current();
   }

   public char next() {
      return this.aci.next();
   }

   public char previous() {
      return this.aci.previous();
   }

   public char setIndex(int position) {
      return this.aci.setIndex(position);
   }

   public int getBeginIndex() {
      return this.aci.getBeginIndex();
   }

   public int getEndIndex() {
      return this.aci.getEndIndex();
   }

   public int getIndex() {
      return this.aci.getIndex();
   }

   public Object clone() {
      return new BidiAttributedCharacterIterator((AttributedCharacterIterator)this.aci.clone());
   }

   public int getRunStart() {
      return this.aci.getRunStart();
   }

   public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
      return this.aci.getRunStart(attribute);
   }

   public int getRunStart(Set attributes) {
      return this.aci.getRunStart(attributes);
   }

   public int getRunLimit() {
      return this.aci.getRunLimit();
   }

   public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
      return this.aci.getRunLimit(attribute);
   }

   public int getRunLimit(Set attributes) {
      return this.aci.getRunLimit(attributes);
   }

   public Map getAttributes() {
      return this.aci.getAttributes();
   }

   public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
      return this.aci.getAttribute(attribute);
   }

   public Set getAllAttributeKeys() {
      return this.aci.getAllAttributeKeys();
   }
}
