package org.apache.fop.fo.pagination;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.xml.sax.Locator;

public class RepeatablePageMasterReference extends FObj implements SubSequenceSpecifier {
   private String masterReference;
   private SimplePageMaster master;
   private Property maximumRepeats;
   private static final int INFINITE = -1;
   private int numberConsumed;

   public RepeatablePageMasterReference(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      this.masterReference = pList.get(154).getString();
      this.maximumRepeats = pList.get(156);
      if (this.masterReference == null || this.masterReference.equals("")) {
         this.missingPropertyError("master-reference");
      }

   }

   public void startOfNode() throws FOPException {
      PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)this.parent;
      if (this.masterReference == null) {
         this.missingPropertyError("master-reference");
      } else {
         pageSequenceMaster.addSubsequenceSpecifier(this);
      }

   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      this.invalidChildError(loc, nsURI, localName);
   }

   public SimplePageMaster getNextPageMaster(boolean isOddPage, boolean isFirstPage, boolean isLastPage, boolean isEmptyPage) {
      if (this.getMaximumRepeats() != -1 && this.numberConsumed >= this.getMaximumRepeats()) {
         return null;
      } else {
         ++this.numberConsumed;
         return this.master;
      }
   }

   public SimplePageMaster getLastPageMaster(boolean isOddPage, boolean isFirstPage, boolean isEmptyPage, BlockLevelEventProducer blockLevelEventProducer) {
      return this.getNextPageMaster(isOddPage, isFirstPage, true, isEmptyPage);
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
      return false;
   }

   public boolean hasPagePositionOnly() {
      return false;
   }

   public String getLocalName() {
      return "repeatable-page-master-reference";
   }

   public int getNameId() {
      return 63;
   }

   public void resolveReferences(LayoutMasterSet layoutMasterSet) throws ValidationException {
      this.master = layoutMasterSet.getSimplePageMaster(this.masterReference);
      if (this.master == null) {
         BlockLevelEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster()).noMatchingPageMaster(this, this.parent.getName(), this.masterReference, this.getLocator());
      }

   }

   public boolean canProcess(String flowName) {
      assert this.master != null;

      return this.master.getRegion(58).getRegionName().equals(flowName);
   }

   public boolean isInfinite() {
      return this.getMaximumRepeats() == -1;
   }

   public boolean isReusable() {
      return false;
   }
}
