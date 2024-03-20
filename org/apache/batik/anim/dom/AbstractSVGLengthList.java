package org.apache.batik.anim.dom;

import org.apache.batik.dom.svg.AbstractSVGList;
import org.apache.batik.dom.svg.ListHandler;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.parser.LengthListHandler;
import org.apache.batik.parser.LengthListParser;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGLengthList;

public abstract class AbstractSVGLengthList extends AbstractSVGList implements SVGLengthList {
   protected short direction;
   public static final String SVG_LENGTH_LIST_SEPARATOR = " ";

   protected String getItemSeparator() {
      return " ";
   }

   protected abstract SVGException createSVGException(short var1, String var2, Object[] var3);

   protected abstract Element getElement();

   protected AbstractSVGLengthList(short direction) {
      this.direction = direction;
   }

   public SVGLength initialize(SVGLength newItem) throws DOMException, SVGException {
      return (SVGLength)this.initializeImpl(newItem);
   }

   public SVGLength getItem(int index) throws DOMException {
      return (SVGLength)this.getItemImpl(index);
   }

   public SVGLength insertItemBefore(SVGLength newItem, int index) throws DOMException, SVGException {
      return (SVGLength)this.insertItemBeforeImpl(newItem, index);
   }

   public SVGLength replaceItem(SVGLength newItem, int index) throws DOMException, SVGException {
      return (SVGLength)this.replaceItemImpl(newItem, index);
   }

   public SVGLength removeItem(int index) throws DOMException {
      return (SVGLength)this.removeItemImpl(index);
   }

   public SVGLength appendItem(SVGLength newItem) throws DOMException, SVGException {
      return (SVGLength)this.appendItemImpl(newItem);
   }

   protected SVGItem createSVGItem(Object newItem) {
      SVGLength l = (SVGLength)newItem;
      return new SVGLengthItem(l.getUnitType(), l.getValueInSpecifiedUnits(), this.direction);
   }

   protected void doParse(String value, ListHandler handler) throws ParseException {
      LengthListParser lengthListParser = new LengthListParser();
      LengthListBuilder builder = new LengthListBuilder(handler);
      lengthListParser.setLengthListHandler(builder);
      lengthListParser.parse(value);
   }

   protected void checkItemType(Object newItem) throws SVGException {
      if (!(newItem instanceof SVGLength)) {
         this.createSVGException((short)0, "expected.length", (Object[])null);
      }

   }

   protected class LengthListBuilder implements LengthListHandler {
      protected ListHandler listHandler;
      protected float currentValue;
      protected short currentType;

      public LengthListBuilder(ListHandler listHandler) {
         this.listHandler = listHandler;
      }

      public void startLengthList() throws ParseException {
         this.listHandler.startList();
      }

      public void startLength() throws ParseException {
         this.currentType = 1;
         this.currentValue = 0.0F;
      }

      public void lengthValue(float v) throws ParseException {
         this.currentValue = v;
      }

      public void em() throws ParseException {
         this.currentType = 3;
      }

      public void ex() throws ParseException {
         this.currentType = 4;
      }

      public void in() throws ParseException {
         this.currentType = 8;
      }

      public void cm() throws ParseException {
         this.currentType = 6;
      }

      public void mm() throws ParseException {
         this.currentType = 7;
      }

      public void pc() throws ParseException {
         this.currentType = 10;
      }

      public void pt() throws ParseException {
         this.currentType = 3;
      }

      public void px() throws ParseException {
         this.currentType = 5;
      }

      public void percentage() throws ParseException {
         this.currentType = 2;
      }

      public void endLength() throws ParseException {
         this.listHandler.item(AbstractSVGLengthList.this.new SVGLengthItem(this.currentType, this.currentValue, AbstractSVGLengthList.this.direction));
      }

      public void endLengthList() throws ParseException {
         this.listHandler.endList();
      }
   }

   protected class SVGLengthItem extends AbstractSVGLength implements SVGItem {
      protected AbstractSVGList parentList;

      public SVGLengthItem(short type, float value, short direction) {
         super(direction);
         this.unitType = type;
         this.value = value;
      }

      protected SVGOMElement getAssociatedElement() {
         return (SVGOMElement)AbstractSVGLengthList.this.getElement();
      }

      public void setParent(AbstractSVGList list) {
         this.parentList = list;
      }

      public AbstractSVGList getParent() {
         return this.parentList;
      }

      protected void reset() {
         if (this.parentList != null) {
            this.parentList.itemChanged();
         }

      }
   }
}
