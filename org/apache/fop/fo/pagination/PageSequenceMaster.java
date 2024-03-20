package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.xml.sax.Locator;

public class PageSequenceMaster extends FObj {
   private String masterName;
   private LayoutMasterSet layoutMasterSet;
   private List subSequenceSpecifiers;
   private SubSequenceSpecifier currentSubSequence;
   private int currentSubSequenceNumber = -1;
   private BlockLevelEventProducer blockLevelEventProducer;

   public PageSequenceMaster(FONode parent, BlockLevelEventProducer blockLevelEventProducer) {
      super(parent);
      this.blockLevelEventProducer = blockLevelEventProducer;
   }

   public void bind(PropertyList pList) throws FOPException {
      this.masterName = pList.get(153).getString();
      if (this.masterName == null || this.masterName.equals("")) {
         this.missingPropertyError("master-name");
      }

   }

   public void startOfNode() throws FOPException {
      this.subSequenceSpecifiers = new ArrayList();
      this.layoutMasterSet = this.parent.getRoot().getLayoutMasterSet();
      this.layoutMasterSet.addPageSequenceMaster(this.masterName, this);
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(single-page-master-reference|repeatable-page-master-reference|repeatable-page-master-alternatives)+");
      }

   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !"single-page-master-reference".equals(localName) && !"repeatable-page-master-reference".equals(localName) && !"repeatable-page-master-alternatives".equals(localName)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   protected void addSubsequenceSpecifier(SubSequenceSpecifier pageMasterReference) {
      this.subSequenceSpecifiers.add(pageMasterReference);
   }

   public LayoutMasterSet getLayoutMasterSet() {
      return this.layoutMasterSet;
   }

   private SubSequenceSpecifier getNextSubSequence() {
      ++this.currentSubSequenceNumber;
      return this.currentSubSequenceNumber >= 0 && this.currentSubSequenceNumber < this.subSequenceSpecifiers.size() ? (SubSequenceSpecifier)this.subSequenceSpecifiers.get(this.currentSubSequenceNumber) : null;
   }

   List getSubSequenceSpecifier() {
      return Collections.unmodifiableList(this.subSequenceSpecifiers);
   }

   public void reset() {
      this.currentSubSequenceNumber = -1;
      this.currentSubSequence = null;
      if (this.subSequenceSpecifiers != null) {
         Iterator var1 = this.subSequenceSpecifiers.iterator();

         while(var1.hasNext()) {
            SubSequenceSpecifier subSequenceSpecifier = (SubSequenceSpecifier)var1.next();
            subSequenceSpecifier.reset();
         }
      }

   }

   public boolean goToPreviousSimplePageMaster() {
      if (this.currentSubSequence != null) {
         boolean success = this.currentSubSequence.goToPrevious();
         if (!success) {
            if (this.currentSubSequenceNumber > 0) {
               --this.currentSubSequenceNumber;
               this.currentSubSequence = (SubSequenceSpecifier)this.subSequenceSpecifiers.get(this.currentSubSequenceNumber);
            } else {
               this.currentSubSequence = null;
            }
         }
      }

      return this.currentSubSequence != null;
   }

   public boolean hasPagePositionLast() {
      return this.currentSubSequence != null && this.currentSubSequence.hasPagePositionLast();
   }

   public boolean hasPagePositionOnly() {
      return this.currentSubSequence != null && this.currentSubSequence.hasPagePositionOnly();
   }

   public SimplePageMaster getNextSimplePageMaster(boolean isOddPage, boolean isFirstPage, boolean isLastPage, boolean isBlankPage, String mainFlowName) throws PageProductionException {
      if (this.currentSubSequence == null) {
         this.currentSubSequence = this.getNextSubSequence();
         if (this.currentSubSequence == null) {
            this.blockLevelEventProducer.missingSubsequencesInPageSequenceMaster(this, this.masterName, this.getLocator());
         }

         if (this.currentSubSequence.isInfinite() && !this.currentSubSequence.canProcess(mainFlowName)) {
            throw new PageProductionException("The current sub-sequence will not terminate whilst processing then main flow");
         }
      }

      SimplePageMaster pageMaster = this.currentSubSequence.getNextPageMaster(isOddPage, isFirstPage, isLastPage, isBlankPage);

      for(boolean canRecover = true; pageMaster == null; pageMaster = this.currentSubSequence.getNextPageMaster(isOddPage, isFirstPage, isLastPage, isBlankPage)) {
         SubSequenceSpecifier nextSubSequence = this.getNextSubSequence();
         if (nextSubSequence == null) {
            this.blockLevelEventProducer.pageSequenceMasterExhausted(this, this.masterName, canRecover & this.currentSubSequence.isReusable(), this.getLocator());
            this.currentSubSequence.reset();
            if (!this.currentSubSequence.canProcess(mainFlowName)) {
               throw new PageProductionException("The last simple-page-master does not reference the main flow");
            }

            canRecover = false;
         } else {
            this.currentSubSequence = nextSubSequence;
         }
      }

      return pageMaster;
   }

   public String getLocalName() {
      return "page-sequence-master";
   }

   public int getNameId() {
      return 54;
   }

   public SimplePageMaster getLastSimplePageMaster(boolean isOddPage, boolean isFirstPage, boolean isBlank, String flowName) {
      if (this.currentSubSequence == null) {
         this.currentSubSequence = this.getNextSubSequence();
         if (this.currentSubSequence == null) {
            this.blockLevelEventProducer.missingSubsequencesInPageSequenceMaster(this, this.masterName, this.getLocator());
         }

         if (this.currentSubSequence.isInfinite() && !this.currentSubSequence.canProcess(flowName)) {
            throw new PageProductionException("The current sub-sequence will not terminate whilst processing the main flow");
         }
      }

      SimplePageMaster pageMaster = this.currentSubSequence.getLastPageMaster(isOddPage, isFirstPage, isBlank, this.blockLevelEventProducer);
      return pageMaster;
   }
}
