package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class ChangeBarBegin extends ChangeBar {
   public ChangeBarBegin(FONode parent) {
      super(parent);
   }

   public String getLocalName() {
      return "change-bar-begin";
   }

   public int getNameId() {
      return 8;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      super.processNode(elementName, locator, attlist, pList);
      this.push();
   }
}
