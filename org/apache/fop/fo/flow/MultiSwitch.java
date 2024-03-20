package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.xml.sax.Locator;

public class MultiSwitch extends FObj {
   private int autoToggle;

   public MultiSwitch(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.autoToggle = pList.get(291).getEnum();
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(multi-case+)");
      }

      super.endOfNode();
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !localName.equals("multi-case")) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public String getLocalName() {
      return "multi-switch";
   }

   public int getNameId() {
      return 48;
   }

   public int getAutoToggle() {
      return this.autoToggle;
   }
}
