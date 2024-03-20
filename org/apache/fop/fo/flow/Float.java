package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class Float extends FObj {
   private int foFloat;
   private int clear;
   private boolean inWhiteSpace;
   private boolean disabled;

   public Float(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.foFloat = pList.get(95).getEnum();
      this.clear = pList.get(70).getEnum();
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !this.isBlockItem(nsURI, localName)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(%block;)+");
      }

   }

   public String getLocalName() {
      return "float";
   }

   public int getNameId() {
      return 15;
   }

   public int getFloat() {
      return this.foFloat;
   }

   public void setInWhiteSpace(boolean iws) {
      this.inWhiteSpace = iws;
   }

   public boolean getInWhiteSpace() {
      return this.inWhiteSpace;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      if (this.findAncestor(71) > 0) {
         this.disabled = true;
         this.getFOValidationEventProducer().unimplementedFeature(this, "fo:table", this.getName(), this.getLocator());
      } else {
         super.processNode(elementName, locator, attlist, pList);
      }

   }

   public boolean isDisabled() {
      return this.disabled;
   }
}
