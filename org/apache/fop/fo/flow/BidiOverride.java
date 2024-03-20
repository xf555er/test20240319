package org.apache.fop.fo.flow;

import java.util.Iterator;
import java.util.Stack;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.Property;

public class BidiOverride extends Inline {
   private Property letterSpacing;
   private Property wordSpacing;
   private int direction;
   private int unicodeBidi;

   public BidiOverride(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.letterSpacing = pList.get(141);
      this.wordSpacing = pList.get(265);
      this.direction = pList.get(86).getEnum();
      this.unicodeBidi = pList.get(255).getEnum();
   }

   public Property getLetterSpacing() {
      return this.letterSpacing;
   }

   public Property getWordSpacing() {
      return this.wordSpacing;
   }

   public int getDirection() {
      return this.direction;
   }

   public int getUnicodeBidi() {
      return this.unicodeBidi;
   }

   public String getLocalName() {
      return "bidi-override";
   }

   public int getNameId() {
      return 2;
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      char pfx = 0;
      char sfx = 0;
      int unicodeBidi = this.getUnicodeBidi();
      int direction = this.getDirection();
      if (unicodeBidi == 15) {
         pfx = direction == 122 ? 8238 : 8237;
         sfx = 8236;
      } else if (unicodeBidi == 38) {
         pfx = direction == 122 ? 8235 : 8234;
         sfx = 8236;
      }

      if (currentRange != null) {
         if (pfx != 0) {
            currentRange.append((char)pfx, this);
         }

         for(Iterator it = this.getChildNodes(); it != null && it.hasNext(); ranges = ((FONode)it.next()).collectDelimitedTextRanges(ranges)) {
         }

         if (sfx != 0) {
            currentRange.append(sfx, this);
         }
      }

      return ranges;
   }
}
