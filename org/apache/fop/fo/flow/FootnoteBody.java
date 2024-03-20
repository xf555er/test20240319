package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.xml.sax.Locator;

public class FootnoteBody extends FObj implements CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;

   public FootnoteBody(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startFootnoteBody(this);
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(%block;)+");
      }

      this.getFOEventHandler().endFootnoteBody(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !this.isBlockItem(nsURI, localName)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public String getLocalName() {
      return "footnote-body";
   }

   public int getNameId() {
      return 25;
   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }
}
