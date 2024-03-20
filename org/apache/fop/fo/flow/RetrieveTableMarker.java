package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.flow.table.TableCell;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class RetrieveTableMarker extends AbstractRetrieveMarker {
   public RetrieveTableMarker(FONode parent) {
      super(parent);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      if (this.findAncestor(78) < 0 && this.findAncestor(77) < 0) {
         this.invalidChildError(locator, this.getParent().getName(), "http://www.w3.org/1999/XSL/Format", this.getName(), "rule.retrieveTableMarkerDescendantOfHeaderOrFooter");
      } else {
         super.processNode(elementName, locator, attlist, pList);
      }

   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.setPosition(pList.get(209).getEnum());
      this.setPositionLabel((String)pList.get(209).getObject());
      this.setBoundary(pList.get(206).getEnum());
      this.setBoundaryLabel((String)pList.get(206).getObject());
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startRetrieveTableMarker(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endRetrieveTableMarker(this);
   }

   protected int findAncestor(int ancestorID) {
      int found = 1;

      for(FONode temp = this.getParent(); temp != null; temp = temp.getParent()) {
         if (temp instanceof TableCell && (ancestorID == 78 || ancestorID == 77)) {
            ((TableCell)temp).flagAsHavingRetrieveTableMarker();
         }

         if (temp.getNameId() == ancestorID) {
            return found;
         }

         ++found;
      }

      return -1;
   }

   public int getRetrievePositionWithinTable() {
      return this.getPosition();
   }

   public int getRetrieveBoundaryWithinTable() {
      return this.getBoundary();
   }

   public String getLocalName() {
      return "retrieve-table-marker";
   }

   public int getNameId() {
      return 65;
   }

   public void clearChildNodes() {
      super.clearChildNodes();
      this.currentTextNode = null;
      this.lastFOTextProcessed = null;
   }

   protected void restoreFOEventHandlerState() {
      this.getFOEventHandler().restoreState(this);
   }
}
