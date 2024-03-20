package org.apache.fop.fo.flow;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.StructureTreeElementHolder;
import org.xml.sax.Locator;

public class BasicLink extends InlineLevel implements StructureTreeElementHolder {
   private Length alignmentAdjust;
   private int alignmentBaseline;
   private Length baselineShift;
   private int dominantBaseline;
   private StructureTreeElement structureTreeElement;
   private String externalDestination;
   private String internalDestination;
   private int showDestination;
   private String altText;
   private boolean blockOrInlineItemFound;

   public BasicLink(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.alignmentAdjust = pList.get(3).getLength();
      this.alignmentBaseline = pList.get(4).getEnum();
      this.baselineShift = pList.get(15).getLength();
      this.dominantBaseline = pList.get(88).getEnum();
      this.externalDestination = pList.get(94).getString();
      this.internalDestination = pList.get(128).getString();
      this.showDestination = pList.get(219).getEnum();
      if (this.internalDestination.length() > 0) {
         this.externalDestination = null;
      } else if (this.externalDestination.length() == 0) {
         this.getFOValidationEventProducer().missingLinkDestination(this, this.getName(), this.locator);
      }

      if (this.getUserAgent().isAccessibilityEnabled()) {
         this.altText = pList.get(273).getString();
         if (this.altText.equals("") && this.getUserAgent().isPdfUAEnabled()) {
            this.getFOValidationEventProducer().altTextMissing(this, this.getLocalName(), this.getLocator());
         }
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startLink(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endLink(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("marker")) {
            if (this.blockOrInlineItemFound) {
               this.nodesOutOfOrderError(loc, "fo:marker", "(#PCDATA|%inline;|%block;)");
            }
         } else if (!this.isBlockOrInlineItem(nsURI, localName)) {
            this.invalidChildError(loc, nsURI, localName);
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

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   public String getInternalDestination() {
      return this.internalDestination;
   }

   public String getExternalDestination() {
      return this.externalDestination;
   }

   public boolean hasInternalDestination() {
      return this.internalDestination != null && this.internalDestination.length() > 0;
   }

   public boolean hasExternalDestination() {
      return this.externalDestination != null && this.externalDestination.length() > 0;
   }

   public int getShowDestination() {
      return this.showDestination;
   }

   public String getLocalName() {
      return "basic-link";
   }

   public int getNameId() {
      return 1;
   }

   public String getAltText() {
      return this.altText;
   }
}
