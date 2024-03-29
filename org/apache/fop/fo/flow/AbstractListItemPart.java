package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.KeepProperty;
import org.xml.sax.Locator;

public abstract class AbstractListItemPart extends FObj implements CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private KeepProperty keepTogether;
   private boolean blockItemFound;

   public AbstractListItemPart(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
      this.keepTogether = pList.get(131).getKeep();
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("marker")) {
            if (this.blockItemFound) {
               this.nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
         } else if (!this.isBlockItem(nsURI, localName)) {
            this.invalidChildError(loc, nsURI, localName);
         } else {
            this.blockItemFound = true;
         }
      }

   }

   public void endOfNode() throws FOPException {
      if (!this.blockItemFound) {
         String contentModel = "marker* (%block;)+";
         this.getFOValidationEventProducer().missingChildElement(this, this.getName(), contentModel, true, this.getLocator());
      }

   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public KeepProperty getKeepTogether() {
      return this.keepTogether;
   }
}
