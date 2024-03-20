package org.apache.batik.anim.dom;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatableLengthListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.ListBuilder;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedLengthList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGLengthList;

public class SVGOMAnimatedLengthList extends AbstractSVGAnimatedValue implements SVGAnimatedLengthList {
   protected BaseSVGLengthList baseVal;
   protected AnimSVGLengthList animVal;
   protected boolean changing;
   protected String defaultValue;
   protected boolean emptyAllowed;
   protected short direction;

   public SVGOMAnimatedLengthList(AbstractElement elt, String ns, String ln, String defaultValue, boolean emptyAllowed, short direction) {
      super(elt, ns, ln);
      this.defaultValue = defaultValue;
      this.emptyAllowed = emptyAllowed;
      this.direction = direction;
   }

   public SVGLengthList getBaseVal() {
      if (this.baseVal == null) {
         this.baseVal = new BaseSVGLengthList();
      }

      return this.baseVal;
   }

   public SVGLengthList getAnimVal() {
      if (this.animVal == null) {
         this.animVal = new AnimSVGLengthList();
      }

      return this.animVal;
   }

   public void check() {
      if (!this.hasAnimVal) {
         if (this.baseVal == null) {
            this.baseVal = new BaseSVGLengthList();
         }

         this.baseVal.revalidate();
         if (this.baseVal.missing) {
            throw new LiveAttributeException(this.element, this.localName, (short)0, (String)null);
         }

         if (this.baseVal.malformed) {
            throw new LiveAttributeException(this.element, this.localName, (short)1, this.baseVal.getValueAsString());
         }
      }

   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      SVGLengthList ll = this.getBaseVal();
      int n = ll.getNumberOfItems();
      short[] types = new short[n];
      float[] values = new float[n];

      for(int i = 0; i < n; ++i) {
         SVGLength l = ll.getItem(i);
         types[i] = l.getUnitType();
         values[i] = l.getValueInSpecifiedUnits();
      }

      return new AnimatableLengthListValue(target, types, values, target.getPercentageInterpretation(this.getNamespaceURI(), this.getLocalName(), false));
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         AnimatableLengthListValue animLengths = (AnimatableLengthListValue)val;
         if (this.animVal == null) {
            this.animVal = new AnimSVGLengthList();
         }

         this.animVal.setAnimatedValue(animLengths.getLengthTypes(), animLengths.getLengthValues());
      }

      this.fireAnimatedAttributeListeners();
   }

   public void attrAdded(Attr node, String newv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrModified(Attr node, String oldv, String newv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrRemoved(Attr node, String oldv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   protected class AnimSVGLengthList extends AbstractSVGLengthList {
      public AnimSVGLengthList() {
         super(SVGOMAnimatedLengthList.this.direction);
         this.itemList = new ArrayList(1);
      }

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedLengthList.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedLengthList.this.element).createSVGException(type, key, args);
      }

      protected Element getElement() {
         return SVGOMAnimatedLengthList.this.element;
      }

      public int getNumberOfItems() {
         return SVGOMAnimatedLengthList.this.hasAnimVal ? super.getNumberOfItems() : SVGOMAnimatedLengthList.this.getBaseVal().getNumberOfItems();
      }

      public SVGLength getItem(int index) throws DOMException {
         return SVGOMAnimatedLengthList.this.hasAnimVal ? super.getItem(index) : SVGOMAnimatedLengthList.this.getBaseVal().getItem(index);
      }

      protected String getValueAsString() {
         if (this.itemList.size() == 0) {
            return "";
         } else {
            StringBuffer sb = new StringBuffer(this.itemList.size() * 8);
            Iterator i = this.itemList.iterator();
            if (i.hasNext()) {
               sb.append(((SVGItem)i.next()).getValueAsString());
            }

            while(i.hasNext()) {
               sb.append(this.getItemSeparator());
               sb.append(((SVGItem)i.next()).getValueAsString());
            }

            return sb.toString();
         }
      }

      protected void setAttributeValue(String value) {
      }

      public void clear() throws DOMException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      public SVGLength initialize(SVGLength newItem) throws DOMException, SVGException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      public SVGLength insertItemBefore(SVGLength newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      public SVGLength replaceItem(SVGLength newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      public SVGLength removeItem(int index) throws DOMException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      public SVGLength appendItem(SVGLength newItem) throws DOMException {
         throw SVGOMAnimatedLengthList.this.element.createDOMException((short)7, "readonly.length.list", (Object[])null);
      }

      protected void setAnimatedValue(short[] types, float[] values) {
         int size = this.itemList.size();

         int i;
         for(i = 0; i < size && i < types.length; ++i) {
            AbstractSVGLengthList.SVGLengthItem l = (AbstractSVGLengthList.SVGLengthItem)this.itemList.get(i);
            l.unitType = types[i];
            l.value = values[i];
            l.direction = this.direction;
         }

         while(i < types.length) {
            this.appendItemImpl(new AbstractSVGLengthList.SVGLengthItem(types[i], values[i], this.direction));
            ++i;
         }

         while(size > types.length) {
            --size;
            this.removeItemImpl(size);
         }

      }

      protected void resetAttribute() {
      }

      protected void resetAttribute(SVGItem item) {
      }

      protected void revalidate() {
         this.valid = true;
      }
   }

   public class BaseSVGLengthList extends AbstractSVGLengthList {
      protected boolean missing;
      protected boolean malformed;

      public BaseSVGLengthList() {
         super(SVGOMAnimatedLengthList.this.direction);
      }

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedLengthList.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedLengthList.this.element).createSVGException(type, key, args);
      }

      protected Element getElement() {
         return SVGOMAnimatedLengthList.this.element;
      }

      protected String getValueAsString() {
         Attr attr = SVGOMAnimatedLengthList.this.element.getAttributeNodeNS(SVGOMAnimatedLengthList.this.namespaceURI, SVGOMAnimatedLengthList.this.localName);
         return attr == null ? SVGOMAnimatedLengthList.this.defaultValue : attr.getValue();
      }

      protected void setAttributeValue(String value) {
         try {
            SVGOMAnimatedLengthList.this.changing = true;
            SVGOMAnimatedLengthList.this.element.setAttributeNS(SVGOMAnimatedLengthList.this.namespaceURI, SVGOMAnimatedLengthList.this.localName, value);
         } finally {
            SVGOMAnimatedLengthList.this.changing = false;
         }

      }

      protected void resetAttribute() {
         super.resetAttribute();
         this.missing = false;
         this.malformed = false;
      }

      protected void resetAttribute(SVGItem item) {
         super.resetAttribute(item);
         this.missing = false;
         this.malformed = false;
      }

      protected void revalidate() {
         if (!this.valid) {
            this.valid = true;
            this.missing = false;
            this.malformed = false;
            String s = this.getValueAsString();
            boolean isEmpty = s != null && s.length() == 0;
            if (s != null && (!isEmpty || SVGOMAnimatedLengthList.this.emptyAllowed)) {
               if (isEmpty) {
                  this.itemList = new ArrayList(1);
               } else {
                  try {
                     ListBuilder builder = new ListBuilder(this);
                     this.doParse(s, builder);
                     if (builder.getList() != null) {
                        this.clear(this.itemList);
                     }

                     this.itemList = builder.getList();
                  } catch (ParseException var4) {
                     this.itemList = new ArrayList(1);
                     this.valid = true;
                     this.malformed = true;
                  }
               }

            } else {
               this.missing = true;
            }
         }
      }
   }
}
