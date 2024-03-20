package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.flow.ChangeBar;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;
import org.xml.sax.Locator;

public class PageSequence extends AbstractPageSequence implements WritingModeTraitsGetter {
   private String masterReference;
   private Numeric referenceOrientation;
   private WritingModeTraits writingModeTraits;
   private Locale locale;
   private Map flowMap;
   private SimplePageMaster simplePageMaster;
   private PageSequenceMaster pageSequenceMaster;
   private Title titleFO;
   private Flow mainFlow;
   private final List changeBarList = new LinkedList();

   public PageSequence(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      String country = pList.get(81).getString();
      String language = pList.get(134).getString();
      this.locale = CommonHyphenation.toLocale(language, country);
      this.masterReference = pList.get(154).getString();
      this.referenceOrientation = pList.get(197).getNumeric();
      this.writingModeTraits = new WritingModeTraits(WritingMode.valueOf(pList.get(267).getEnum()), pList.getExplicit(267) != null);
      if (this.masterReference == null || this.masterReference.equals("")) {
         this.missingPropertyError("master-reference");
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.flowMap = new HashMap();
      this.simplePageMaster = this.getRoot().getLayoutMasterSet().getSimplePageMaster(this.masterReference);
      if (this.simplePageMaster == null) {
         this.pageSequenceMaster = this.getRoot().getLayoutMasterSet().getPageSequenceMaster(this.masterReference);
         if (this.pageSequenceMaster == null) {
            this.getFOValidationEventProducer().masterNotFound(this, this.getName(), this.masterReference, this.getLocator());
         }
      }

      this.getRoot().addPageSequence(this);
      this.getFOEventHandler().startPageSequence(this);
   }

   public void endOfNode() throws FOPException {
      if (this.mainFlow == null) {
         this.missingChildElementError("(title?,static-content*,flow)");
      }

      this.getFOEventHandler().endPageSequence(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if ("title".equals(localName)) {
            if (this.titleFO != null) {
               this.tooManyNodesError(loc, "fo:title");
            } else if (!this.flowMap.isEmpty()) {
               this.nodesOutOfOrderError(loc, "fo:title", "fo:static-content");
            } else if (this.mainFlow != null) {
               this.nodesOutOfOrderError(loc, "fo:title", "fo:flow");
            }
         } else if ("static-content".equals(localName)) {
            if (this.mainFlow != null) {
               this.nodesOutOfOrderError(loc, "fo:static-content", "fo:flow");
            }
         } else if ("flow".equals(localName)) {
            if (this.mainFlow != null) {
               this.tooManyNodesError(loc, "fo:flow");
            }
         } else {
            this.invalidChildError(loc, nsURI, localName);
         }
      }

   }

   public void addChildNode(FONode child) throws FOPException {
      int childId = child.getNameId();
      switch (childId) {
         case 16:
            this.mainFlow = (Flow)child;
            this.addFlow(this.mainFlow);
            break;
         case 70:
            this.addFlow((StaticContent)child);
            this.flowMap.put(((Flow)child).getFlowName(), (Flow)child);
            break;
         case 80:
            this.titleFO = (Title)child;
            break;
         default:
            super.addChildNode(child);
      }

   }

   private void addFlow(Flow flow) throws ValidationException {
      String flowName = flow.getFlowName();
      if (this.hasFlowName(flowName)) {
         this.getFOValidationEventProducer().duplicateFlowNameInPageSequence(this, flow.getName(), flowName, flow.getLocator());
      }

      if (!this.hasRegion(flowName) && !flowName.equals("xsl-before-float-separator") && !flowName.equals("xsl-footnote-separator")) {
         this.getFOValidationEventProducer().flowNameNotMapped(this, flow.getName(), flowName, flow.getLocator());
      }

   }

   private boolean hasRegion(String flowName) {
      LayoutMasterSet set = this.getRoot().getLayoutMasterSet();
      PageSequenceMaster psm = set.getPageSequenceMaster(this.masterReference);
      return psm != null ? psm.getLayoutMasterSet().regionNameExists(flowName) : set.getSimplePageMaster(this.masterReference).regionNameExists(flowName);
   }

   public StaticContent getStaticContent(String name) {
      return (StaticContent)this.flowMap.get(name);
   }

   public Title getTitleFO() {
      return this.titleFO;
   }

   public Flow getMainFlow() {
      return this.mainFlow;
   }

   public boolean hasFlowName(String flowName) {
      return this.flowMap.containsKey(flowName);
   }

   public Map getFlowMap() {
      return this.flowMap;
   }

   public SimplePageMaster getNextSimplePageMaster(int page, boolean isFirstPage, boolean isLastPage, boolean isBlank) throws PageProductionException {
      if (this.pageSequenceMaster == null) {
         return this.simplePageMaster;
      } else {
         boolean isOddPage = page % 2 != 0;
         if (log.isDebugEnabled()) {
            log.debug("getNextSimplePageMaster(page=" + page + " isOdd=" + isOddPage + " isFirst=" + isFirstPage + " isLast=" + isLastPage + " isBlank=" + isBlank + ")");
         }

         return this.pageSequenceMaster.getNextSimplePageMaster(isOddPage, isFirstPage, isLastPage, isBlank, this.getMainFlow().getFlowName());
      }
   }

   public boolean goToPreviousSimplePageMaster() {
      return this.pageSequenceMaster == null || this.pageSequenceMaster.goToPreviousSimplePageMaster();
   }

   public boolean hasPagePositionLast() {
      return this.pageSequenceMaster != null && this.pageSequenceMaster.hasPagePositionLast();
   }

   public boolean hasPagePositionOnly() {
      return this.pageSequenceMaster != null && this.pageSequenceMaster.hasPagePositionOnly();
   }

   public String getMasterReference() {
      return this.masterReference;
   }

   public String getLocalName() {
      return "page-sequence";
   }

   public int getNameId() {
      return 53;
   }

   public Locale getLocale() {
      return this.locale;
   }

   public int getReferenceOrientation() {
      return this.referenceOrientation != null ? this.referenceOrientation.getValue() : 0;
   }

   public Direction getInlineProgressionDirection() {
      return this.writingModeTraits != null ? this.writingModeTraits.getInlineProgressionDirection() : Direction.LR;
   }

   public Direction getBlockProgressionDirection() {
      return this.writingModeTraits != null ? this.writingModeTraits.getBlockProgressionDirection() : Direction.TB;
   }

   public Direction getColumnProgressionDirection() {
      return this.writingModeTraits != null ? this.writingModeTraits.getColumnProgressionDirection() : Direction.LR;
   }

   public Direction getRowProgressionDirection() {
      return this.writingModeTraits != null ? this.writingModeTraits.getRowProgressionDirection() : Direction.TB;
   }

   public Direction getShiftDirection() {
      return this.writingModeTraits != null ? this.writingModeTraits.getShiftDirection() : Direction.TB;
   }

   public WritingMode getWritingMode() {
      return this.writingModeTraits != null ? this.writingModeTraits.getWritingMode() : WritingMode.LR_TB;
   }

   public boolean getExplicitWritingMode() {
      return this.writingModeTraits != null ? this.writingModeTraits.getExplicitWritingMode() : false;
   }

   protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
      Map flows = this.getFlowMap();
      if (flows != null) {
         Iterator var4 = flows.values().iterator();

         while(var4.hasNext()) {
            FONode fn = (FONode)var4.next();
            if (fn instanceof StaticContent) {
               ranges = ((StaticContent)fn).collectDelimitedTextRanges(ranges);
            }
         }
      }

      Flow main = this.getMainFlow();
      if (main != null) {
         ranges = main.collectDelimitedTextRanges(ranges);
      }

      return ranges;
   }

   protected boolean isBidiBoundary(boolean propagate) {
      return true;
   }

   public void releasePageSequence() {
      this.mainFlow = null;
      this.flowMap.clear();
   }

   public SimplePageMaster getLastSimplePageMaster(int page, boolean isFirstPage, boolean isBlank) {
      boolean isOddPage = page % 2 != 0;
      log.debug("getNextSimplePageMaster(page=" + page + " isOdd=" + isOddPage + " isFirst=" + isFirstPage + " isLast=true isBlank=" + isBlank + ")");
      return this.pageSequenceMaster == null ? this.simplePageMaster : this.pageSequenceMaster.getLastSimplePageMaster(isOddPage, isFirstPage, isBlank, this.getMainFlow().getFlowName());
   }

   public void pushChangeBar(ChangeBar changeBarBegin) {
      this.changeBarList.add(changeBarBegin);
   }

   public void popChangeBar(ChangeBar changeBarEnd) {
      ChangeBar changeBarBegin = this.getChangeBarBegin(changeBarEnd);
      if (changeBarBegin != null) {
         this.changeBarList.remove(changeBarBegin);
      }

   }

   public ChangeBar getChangeBarBegin(ChangeBar changeBarEnd) {
      if (this.changeBarList.isEmpty()) {
         return null;
      } else {
         String changeBarClass = changeBarEnd.getChangeBarClass();

         for(int i = this.changeBarList.size() - 1; i >= 0; --i) {
            ChangeBar changeBar = (ChangeBar)this.changeBarList.get(i);
            if (changeBar.getChangeBarClass().equals(changeBarClass)) {
               return changeBar;
            }
         }

         return null;
      }
   }

   public boolean hasChangeBars() {
      return !this.changeBarList.isEmpty();
   }

   public List getChangeBarList() {
      return this.changeBarList;
   }

   public List getClonedChangeBarList() {
      return new LinkedList(this.changeBarList);
   }
}
