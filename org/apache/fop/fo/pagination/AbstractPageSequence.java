package org.apache.fop.fo.pagination;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

public abstract class AbstractPageSequence extends FObj implements CommonAccessibilityHolder {
   protected Numeric initialPageNumber;
   protected int forcePageCount;
   private String format;
   private int letterValue;
   private char groupingSeparator;
   private int groupingSize;
   private Numeric referenceOrientation;
   private String language;
   private String country;
   private String numberConversionFeatures;
   private CommonAccessibility commonAccessibility;
   private PageNumberGenerator pageNumberGenerator;
   protected int startingPageNumber;

   public AbstractPageSequence(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.initialPageNumber = pList.get(126).getNumeric();
      this.forcePageCount = pList.get(109).getEnum();
      this.format = pList.get(110).getString();
      this.letterValue = pList.get(142).getEnum();
      this.groupingSeparator = pList.get(113).getCharacter();
      this.groupingSize = pList.get(114).getNumber().intValue();
      this.referenceOrientation = pList.get(197).getNumeric();
      this.language = pList.get(134).getString();
      this.country = pList.get(81).getString();
      this.numberConversionFeatures = pList.get(288).getString();
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
   }

   public void startOfNode() throws FOPException {
      this.pageNumberGenerator = new PageNumberGenerator(this.format, this.groupingSeparator, this.groupingSize, this.letterValue, this.numberConversionFeatures, this.language, this.country);
   }

   public void initPageNumber() {
      int pageNumberType = false;
      if (this.initialPageNumber.getEnum() != 0) {
         this.startingPageNumber = this.getRoot().getEndingPageNumberOfPreviousSequence() + 1;
         int pageNumberType = this.initialPageNumber.getEnum();
         if (pageNumberType == 11) {
            if (this.startingPageNumber % 2 == 0) {
               ++this.startingPageNumber;
            }
         } else if (pageNumberType == 10 && this.startingPageNumber % 2 != 0) {
            ++this.startingPageNumber;
         }
      } else {
         int pageStart = this.initialPageNumber.getValue();
         this.startingPageNumber = pageStart > 0 ? pageStart : 1;
      }

   }

   public int getStartingPageNumber() {
      return this.startingPageNumber;
   }

   public String makeFormattedPageNumber(int pageNumber) {
      return this.pageNumberGenerator.makeFormattedPageNumber(pageNumber);
   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public Root getRoot() {
      return (Root)this.getParent();
   }

   public int getForcePageCount() {
      return this.forcePageCount;
   }

   public Numeric getInitialPageNumber() {
      return this.initialPageNumber;
   }

   public int getReferenceOrientation() {
      return this.referenceOrientation.getValue();
   }
}
