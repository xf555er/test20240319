package org.apache.fop.fo.flow.table;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.xml.sax.Locator;

public class TableCaption extends FObj implements CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private boolean blockItemFound;
   static boolean notImplementedWarningGiven;

   public TableCaption(FONode parent) {
      super(parent);
      if (!notImplementedWarningGiven) {
         this.getFOValidationEventProducer().unimplementedFeature(this, this.getName(), "fo:table-caption", this.getLocator());
         notImplementedWarningGiven = true;
      }

   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("marker* (%block;)");
      }

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

   public String getLocalName() {
      return "table-caption";
   }

   public int getNameId() {
      return 74;
   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }
}
