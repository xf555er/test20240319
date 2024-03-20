package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.xml.sax.Locator;

public class RepeatablePageMasterAlternatives extends FObj implements SubSequenceSpecifier {
   private Property maximumRepeats;
   private static final int INFINITE = -1;
   private int numberConsumed;
   private List conditionalPageMasterRefs;
   private boolean hasPagePositionLast;
   private boolean hasPagePositionOnly;

   public RepeatablePageMasterAlternatives(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      this.maximumRepeats = pList.get(156);
   }

   public void startOfNode() throws FOPException {
      this.conditionalPageMasterRefs = new ArrayList();

      assert this.parent.getName().equals("fo:page-sequence-master");

      PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)this.parent;
      pageSequenceMaster.addSubsequenceSpecifier(this);
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(conditional-page-master-reference+)");
      }

   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !localName.equals("conditional-page-master-reference")) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public int getMaximumRepeats() {
      if (this.maximumRepeats.getEnum() == 89) {
         return -1;
      } else {
         int mr = this.maximumRepeats.getNumeric().getValue();
         if (mr < 0) {
            log.debug("negative maximum-repeats: " + this.maximumRepeats);
            mr = 0;
         }

         return mr;
      }
   }

   public SimplePageMaster getNextPageMaster(boolean isOddPage, boolean isFirstPage, boolean isLastPage, boolean isBlankPage) {
      if (!this.isInfinite() && this.numberConsumed >= this.getMaximumRepeats()) {
         return null;
      } else {
         ++this.numberConsumed;
         Iterator var5 = this.conditionalPageMasterRefs.iterator();

         ConditionalPageMasterReference cpmr;
         do {
            if (!var5.hasNext()) {
               return null;
            }

            cpmr = (ConditionalPageMasterReference)var5.next();
         } while(!cpmr.isValid(isOddPage, isFirstPage, isLastPage, isBlankPage));

         return cpmr.getMaster();
      }
   }

   public SimplePageMaster getLastPageMaster(boolean isOddPage, boolean isFirstPage, boolean isBlankPage, BlockLevelEventProducer blockLevelEventProducer) {
      Iterator var5 = this.conditionalPageMasterRefs.iterator();

      ConditionalPageMasterReference cpmr;
      do {
         if (!var5.hasNext()) {
            blockLevelEventProducer.lastPageMasterReferenceMissing(this, this.getLocator());
            var5 = this.conditionalPageMasterRefs.iterator();

            do {
               if (!var5.hasNext()) {
                  throw new PageProductionException("Last page master not found: oddpage=" + isOddPage + " firstpage=" + isFirstPage + " blankpage=" + isBlankPage);
               }

               cpmr = (ConditionalPageMasterReference)var5.next();
            } while(!cpmr.isValid(isOddPage, isFirstPage, false, isBlankPage));

            return cpmr.getMaster();
         }

         cpmr = (ConditionalPageMasterReference)var5.next();
      } while(!cpmr.isValid(isOddPage, isFirstPage, true, isBlankPage));

      return cpmr.getMaster();
   }

   public void addConditionalPageMasterReference(ConditionalPageMasterReference cpmr) {
      this.conditionalPageMasterRefs.add(cpmr);
      if (cpmr.getPagePosition() == 72) {
         this.hasPagePositionLast = true;
      }

      if (cpmr.getPagePosition() == 184) {
         this.hasPagePositionOnly = true;
      }

   }

   public void reset() {
      this.numberConsumed = 0;
   }

   public boolean goToPrevious() {
      if (this.numberConsumed == 0) {
         return false;
      } else {
         --this.numberConsumed;
         return true;
      }
   }

   public boolean hasPagePositionLast() {
      return this.hasPagePositionLast;
   }

   public boolean hasPagePositionOnly() {
      return this.hasPagePositionOnly;
   }

   public String getLocalName() {
      return "repeatable-page-master-alternatives";
   }

   public int getNameId() {
      return 62;
   }

   public void resolveReferences(LayoutMasterSet layoutMasterSet) throws ValidationException {
      Iterator var2 = this.conditionalPageMasterRefs.iterator();

      while(var2.hasNext()) {
         ConditionalPageMasterReference conditionalPageMasterReference = (ConditionalPageMasterReference)var2.next();
         conditionalPageMasterReference.resolveReferences(layoutMasterSet);
      }

   }

   public boolean canProcess(String flowName) {
      boolean willTerminate = true;
      ArrayList rest = new ArrayList();
      Iterator var4 = this.conditionalPageMasterRefs.iterator();

      while(true) {
         ConditionalPageMasterReference cpmr;
         do {
            if (!var4.hasNext()) {
               if (!rest.isEmpty()) {
                  willTerminate = false;

                  for(var4 = rest.iterator(); var4.hasNext(); willTerminate |= cpmr.getMaster().getRegion(58).getRegionName().equals(flowName)) {
                     cpmr = (ConditionalPageMasterReference)var4.next();
                  }
               }

               return willTerminate;
            }

            cpmr = (ConditionalPageMasterReference)var4.next();
         } while(!cpmr.isValid(true, false, false, false) && !cpmr.isValid(false, false, false, false));

         rest.add(cpmr);
      }
   }

   public boolean isInfinite() {
      return this.getMaximumRepeats() == -1;
   }

   public boolean isReusable() {
      return false;
   }
}
