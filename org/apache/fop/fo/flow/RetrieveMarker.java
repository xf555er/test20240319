package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class RetrieveMarker extends AbstractRetrieveMarker {
   public RetrieveMarker(FONode parent) {
      super(parent);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      if (this.findAncestor(70) < 0) {
         this.invalidChildError(locator, this.getParent().getName(), "http://www.w3.org/1999/XSL/Format", this.getLocalName(), "rule.retrieveMarkerDescendantOfStaticContent");
      } else {
         super.processNode(elementName, locator, attlist, pList);
      }

   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.setPosition(pList.get(208).getEnum());
      this.setPositionLabel((String)pList.get(208).getObject());
      this.setBoundary(pList.get(205).getEnum());
      this.setBoundaryLabel((String)pList.get(205).getObject());
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startRetrieveMarker(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endRetrieveMarker(this);
   }

   public int getRetrievePosition() {
      return this.getPosition();
   }

   public int getRetrieveBoundary() {
      return this.getBoundary();
   }

   public String getLocalName() {
      return "retrieve-marker";
   }

   public int getNameId() {
      return 64;
   }

   protected void restoreFOEventHandlerState() {
      this.getFOEventHandler().restoreState(this);
   }
}
