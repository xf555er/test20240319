package org.apache.fop.fo.pagination;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.xml.sax.Locator;

public class SinglePageMasterReference extends FObj implements SubSequenceSpecifier {
   private String masterReference;
   private SimplePageMaster master;
   private static final int FIRST = 0;
   private static final int DONE = 1;
   private int state = 0;

   public SinglePageMasterReference(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      this.masterReference = pList.get(154).getString();
      if (this.masterReference == null || this.masterReference.equals("")) {
         this.missingPropertyError("master-reference");
      }

   }

   public void startOfNode() throws FOPException {
      PageSequenceMaster pageSequenceMaster = (PageSequenceMaster)this.parent;
      pageSequenceMaster.addSubsequenceSpecifier(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public SimplePageMaster getNextPageMaster(boolean isOddPage, boolean isFirstPage, boolean isLastPage, boolean isBlankPage) {
      if (this.state == 0) {
         this.state = 1;
         return this.master;
      } else {
         return null;
      }
   }

   public SimplePageMaster getLastPageMaster(boolean isOddPage, boolean isFirstPage, boolean isBlankPage, BlockLevelEventProducer blockLevelEventProducer) {
      return this.getNextPageMaster(isOddPage, isFirstPage, true, isBlankPage);
   }

   public void reset() {
      this.state = 0;
   }

   public boolean goToPrevious() {
      if (this.state == 0) {
         return false;
      } else {
         this.state = 0;
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
      return "single-page-master-reference";
   }

   public int getNameId() {
      return 69;
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
      return false;
   }

   public boolean isReusable() {
      return true;
   }
}
