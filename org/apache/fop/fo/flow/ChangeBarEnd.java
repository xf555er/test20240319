package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class ChangeBarEnd extends ChangeBar {
   public ChangeBarEnd(FONode parent) {
      super(parent);
   }

   public String getLocalName() {
      return "change-bar-end";
   }

   public int getNameId() {
      return 9;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      super.processNode(elementName, locator, attlist, pList);
      ChangeBar changeBarStart = this.getChangeBarBegin();
      if (changeBarStart == null) {
         this.getFOValidationEventProducer().changeBarNoBegin(this, this.getName(), locator);
      } else {
         this.pop();
      }

   }
}
