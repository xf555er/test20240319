package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.xml.sax.Locator;

public class Inline extends InlineLevel {
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private int dominantBaseline;
   private boolean blockOrInlineItemFound;
   private boolean canHaveBlockLevelChildren = true;
   private String abbreviation;

   public Inline(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.dominantBaseline = pList.get(88).getEnum();
      if (this.getUserAgent().isAccessibilityEnabled()) {
         this.abbreviation = pList.get(294).getString();
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      int lvlLeader = this.findAncestor(39);
      int lvlFootnote = this.findAncestor(24);
      int lvlInCntr = this.findAncestor(36);
      if (lvlLeader > 0) {
         if (lvlInCntr < 0 || lvlInCntr > 0 && lvlInCntr > lvlLeader) {
            this.canHaveBlockLevelChildren = false;
         }
      } else if (lvlFootnote > 0 && (lvlInCntr < 0 || lvlInCntr > lvlFootnote)) {
         this.canHaveBlockLevelChildren = false;
      }

      this.getFOEventHandler().startInline(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endInline(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("marker")) {
            if (this.blockOrInlineItemFound) {
               this.nodesOutOfOrderError(loc, "fo:marker", "(#PCDATA|%inline;|%block;)");
            }
         } else if (!this.isBlockOrInlineItem(nsURI, localName)) {
            this.invalidChildError(loc, nsURI, localName);
         } else if (!this.canHaveBlockLevelChildren && this.isBlockItem(nsURI, localName) && !this.isNeutralItem(nsURI, localName)) {
            this.invalidChildError(loc, this.getName(), nsURI, localName, "rule.inlineContent");
         } else {
            this.blockOrInlineItemFound = true;
         }
      }

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
      return "inline";
   }

   public int getNameId() {
      return 35;
   }

   public String getAbbreviation() {
      return this.abbreviation;
   }
}
