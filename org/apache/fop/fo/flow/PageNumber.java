package org.apache.fop.fo.flow;

import java.awt.Color;
import java.util.Stack;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StringCharIterator;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.fo.properties.StructureTreeElementHolder;
import org.xml.sax.Locator;

public class PageNumber extends FObj implements StructureTreeElementHolder, CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private CommonBorderPaddingBackground commonBorderPaddingBackground;
   private CommonFont commonFont;
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private int dominantBaseline;
   private StructureTreeElement structureTreeElement;
   private SpaceProperty lineHeight;
   private CommonTextDecoration textDecoration;
   private Color color;

   public PageNumber(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
      this.commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
      this.commonFont = pList.getFontProps();
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.dominantBaseline = pList.get(88).getEnum();
      this.lineHeight = pList.get(144).getSpace();
      this.textDecoration = pList.getTextDecorationProps();
      this.color = pList.get(72).getColor(this.getUserAgent());
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startPageNumber(this);
   }

   public void endOfNode() throws FOPException {
      this.getFOEventHandler().endPageNumber(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public CommonFont getCommonFont() {
      return this.commonFont;
   }

   public Color getColor() {
      return this.color;
   }

   public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.commonBorderPaddingBackground;
   }

   public CommonTextDecoration getTextDecoration() {
      return this.textDecoration;
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

   public SpaceProperty getLineHeight() {
      return this.lineHeight;
   }

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   public String getLocalName() {
      return "page-number";
   }

   public int getNameId() {
      return 50;
   }

   public boolean isDelimitedTextRangeBoundary(int boundary) {
      return false;
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      if (currentRange != null) {
         currentRange.append(new StringCharIterator(this.defaultPageNumberString()), this);
      }

      return ranges;
   }

   private String defaultPageNumberString() {
      if (this.findAncestor(53) > 0) {
         for(FONode p = this.getParent(); p != null; p = p.getParent()) {
            if (p instanceof PageSequence) {
               return ((PageSequence)p).makeFormattedPageNumber(1);
            }
         }
      }

      return "1";
   }
}
