package org.apache.fop.fo.flow;

import java.awt.Color;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.SpaceProperty;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public abstract class ChangeBar extends FObj {
   protected String changeBarClass;
   protected Color color;
   protected Length offset;
   protected int placement = -1;
   protected int style = -1;
   protected Length width;
   protected SpaceProperty lineHeight;

   public ChangeBar(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.changeBarClass = pList.get(63).getString();
      this.color = pList.get(64).getColor(this.getUserAgent());
      this.offset = pList.get(65).getLength();
      this.placement = pList.get(66).getEnum();
      this.style = pList.get(67).getEnum();
      this.width = pList.get(68).getLength();
      this.lineHeight = pList.get(144).getSpace();
   }

   protected void validateChildNode(Locator loc, String namespaceURI, String localName) throws ValidationException {
      this.invalidChildError(loc, namespaceURI, localName);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
      super.processNode(elementName, locator, attlist, pList);
      if (this.inMarker()) {
         PropertyList newPList = new StaticPropertyList(this, (PropertyList)null);
         newPList.addAttributesToList(attlist);
         this.bind(newPList);
      }

      if (this.changeBarClass == null || this.changeBarClass.isEmpty()) {
         this.missingPropertyError("change-bar-class");
      }

      if (this.findAncestor(16) == -1 && this.findAncestor(70) == -1) {
         this.getFOValidationEventProducer().changeBarWrongAncestor(this, this.getName(), locator);
      }

   }

   protected void push() {
      this.getRoot().getLastPageSequence().pushChangeBar(this);
   }

   protected void pop() {
      this.getRoot().getLastPageSequence().popChangeBar(this);
   }

   protected ChangeBar getChangeBarBegin() {
      return this.getRoot().getLastPageSequence().getChangeBarBegin(this);
   }

   public String getChangeBarClass() {
      return this.changeBarClass;
   }

   public Color getColor() {
      return this.color;
   }

   public Length getOffset() {
      return this.offset;
   }

   public int getPlacement() {
      return this.placement;
   }

   public int getStyle() {
      return this.style;
   }

   public Length getWidth() {
      return this.width;
   }

   public SpaceProperty getLineHeight() {
      return this.lineHeight;
   }
}
