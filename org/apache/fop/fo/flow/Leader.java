package org.apache.fop.fo.flow;

import java.util.Stack;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.xml.sax.Locator;

public class Leader extends InlineLevel {
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private int dominantBaseline;
   private int leaderAlignment;
   private LengthRangeProperty leaderLength;
   private int leaderPattern;
   private Length leaderPatternWidth;
   private int ruleStyle;
   private Length ruleThickness;

   public Leader(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.dominantBaseline = pList.get(88).getEnum();
      this.leaderAlignment = pList.get(136).getEnum();
      this.leaderLength = pList.get(137).getLengthRange();
      this.leaderPattern = pList.get(138).getEnum();
      this.leaderPatternWidth = pList.get(139).getLength();
      this.ruleThickness = getPropertyMakerFor(214).make(pList).getLength();
      switch (this.leaderPattern) {
         case 123:
            this.ruleStyle = pList.get(213).getEnum();
            this.ruleThickness = pList.get(214).getLength();
         case 35:
         case 134:
         case 158:
            return;
         default:
            throw new RuntimeException("Invalid leader pattern: " + this.leaderPattern);
      }
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && (localName.equals("leader") || localName.equals("inline-container") || localName.equals("block-container") || localName.equals("float") || localName.equals("marker") || !this.isInlineItem(nsURI, localName))) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public int getRuleStyle() {
      return this.ruleStyle;
   }

   public Length getRuleThickness() {
      return this.ruleThickness;
   }

   public int getLeaderAlignment() {
      return this.leaderAlignment;
   }

   public LengthRangeProperty getLeaderLength() {
      return this.leaderLength;
   }

   public int getLeaderPattern() {
      return this.leaderPattern;
   }

   public Length getLeaderPatternWidth() {
      return this.leaderPatternWidth;
   }

   public Length getAlignmentAdjust() {
      return this.alignmentAdjust;
   }

   public int getAlignmentBaseline() {
      return this.alignmentBaseline;
   }

   public Length getBaselineShift() {
      return this.baselineShift;
   }

   public int getDominantBaseline() {
      return this.dominantBaseline;
   }

   public String getLocalName() {
      return "leader";
   }

   public int getNameId() {
      return 39;
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startLeader(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endLeader(this);
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      if (currentRange != null) {
         if (this.leaderPattern == 158) {
            ranges = super.collectDelimitedTextRanges(ranges, currentRange);
         } else {
            currentRange.append('￼', this);
         }
      }

      return ranges;
   }
}
