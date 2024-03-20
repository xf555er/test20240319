package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.traits.WritingMode;
import org.xml.sax.Locator;

public class SimplePageMaster extends FObj {
   private CommonMarginBlock commonMarginBlock;
   private String masterName;
   private Length pageHeight;
   private Length pageWidth;
   private Numeric referenceOrientation;
   private WritingMode writingMode;
   private Map regions;
   private boolean hasRegionBody;
   private boolean hasRegionBefore;
   private boolean hasRegionAfter;
   private boolean hasRegionStart;
   private boolean hasRegionEnd;

   public SimplePageMaster(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      this.commonMarginBlock = pList.getMarginBlockProps();
      this.masterName = pList.get(153).getString();
      this.pageHeight = pList.get(183).getLength();
      this.pageWidth = pList.get(186).getLength();
      this.referenceOrientation = pList.get(197).getNumeric();
      this.writingMode = WritingMode.valueOf(pList.get(267).getEnum());
      if (this.masterName == null || this.masterName.equals("")) {
         this.missingPropertyError("master-name");
      }

   }

   public void startOfNode() throws FOPException {
      LayoutMasterSet layoutMasterSet = (LayoutMasterSet)this.parent;
      if (this.masterName == null) {
         this.missingPropertyError("master-name");
      } else {
         layoutMasterSet.addSimplePageMaster(this);
      }

      this.regions = new HashMap(5);
   }

   public void endOfNode() throws FOPException {
      if (!this.hasRegionBody) {
         this.missingChildElementError("(region-body, region-before?, region-after?, region-start?, region-end?)");
      }

   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("region-body")) {
            if (this.hasRegionBody) {
               this.tooManyNodesError(loc, "fo:region-body");
            } else {
               this.hasRegionBody = true;
            }
         } else if (localName.equals("region-before")) {
            if (!this.hasRegionBody) {
               this.nodesOutOfOrderError(loc, "fo:region-body", "fo:region-before");
            } else if (this.hasRegionBefore) {
               this.tooManyNodesError(loc, "fo:region-before");
            } else if (this.hasRegionAfter) {
               this.nodesOutOfOrderError(loc, "fo:region-before", "fo:region-after");
            } else if (this.hasRegionStart) {
               this.nodesOutOfOrderError(loc, "fo:region-before", "fo:region-start");
            } else if (this.hasRegionEnd) {
               this.nodesOutOfOrderError(loc, "fo:region-before", "fo:region-end");
            } else {
               this.hasRegionBefore = true;
            }
         } else if (localName.equals("region-after")) {
            if (!this.hasRegionBody) {
               this.nodesOutOfOrderError(loc, "fo:region-body", "fo:region-after");
            } else if (this.hasRegionAfter) {
               this.tooManyNodesError(loc, "fo:region-after");
            } else if (this.hasRegionStart) {
               this.nodesOutOfOrderError(loc, "fo:region-after", "fo:region-start");
            } else if (this.hasRegionEnd) {
               this.nodesOutOfOrderError(loc, "fo:region-after", "fo:region-end");
            } else {
               this.hasRegionAfter = true;
            }
         } else if (localName.equals("region-start")) {
            if (!this.hasRegionBody) {
               this.nodesOutOfOrderError(loc, "fo:region-body", "fo:region-start");
            } else if (this.hasRegionStart) {
               this.tooManyNodesError(loc, "fo:region-start");
            } else if (this.hasRegionEnd) {
               this.nodesOutOfOrderError(loc, "fo:region-start", "fo:region-end");
            } else {
               this.hasRegionStart = true;
            }
         } else if (localName.equals("region-end")) {
            if (!this.hasRegionBody) {
               this.nodesOutOfOrderError(loc, "fo:region-body", "fo:region-end");
            } else if (this.hasRegionEnd) {
               this.tooManyNodesError(loc, "fo:region-end");
            } else {
               this.hasRegionEnd = true;
            }
         } else {
            this.invalidChildError(loc, nsURI, localName);
         }
      }

   }

   public boolean generatesReferenceAreas() {
      return true;
   }

   protected void addChildNode(FONode child) throws FOPException {
      if (child instanceof Region) {
         this.addRegion((Region)child);
      } else {
         super.addChildNode(child);
      }

   }

   protected void addRegion(Region region) {
      this.regions.put(String.valueOf(region.getNameId()), region);
   }

   protected final PercentBaseContext getPageWidthContext(int lengthBase) {
      return this.referenceOrientation.getValue() % 180 == 0 ? new SimplePercentBaseContext((PercentBaseContext)null, lengthBase, this.getPageWidth().getValue()) : new SimplePercentBaseContext((PercentBaseContext)null, lengthBase, this.getPageHeight().getValue());
   }

   protected final PercentBaseContext getPageHeightContext(int lengthBase) {
      return this.referenceOrientation.getValue() % 180 == 0 ? new SimplePercentBaseContext((PercentBaseContext)null, lengthBase, this.getPageHeight().getValue()) : new SimplePercentBaseContext((PercentBaseContext)null, lengthBase, this.getPageWidth().getValue());
   }

   public Region getRegion(int regionId) {
      return (Region)this.regions.get(String.valueOf(regionId));
   }

   public Map getRegions() {
      return this.regions;
   }

   protected boolean regionNameExists(String regionName) {
      Iterator var2 = this.regions.values().iterator();

      Region r;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         r = (Region)var2.next();
      } while(!r.getRegionName().equals(regionName));

      return true;
   }

   public CommonMarginBlock getCommonMarginBlock() {
      return this.commonMarginBlock;
   }

   public String getMasterName() {
      return this.masterName;
   }

   public Length getPageWidth() {
      return this.pageWidth;
   }

   public Length getPageHeight() {
      return this.pageHeight;
   }

   public int getReferenceOrientation() {
      return this.referenceOrientation.getValue();
   }

   public WritingMode getWritingMode() {
      return this.writingMode;
   }

   public String getLocalName() {
      return "simple-page-master";
   }

   public int getNameId() {
      return 68;
   }
}
