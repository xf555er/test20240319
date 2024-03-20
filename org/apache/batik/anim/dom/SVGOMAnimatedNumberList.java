package org.apache.batik.anim.dom;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatableNumberListValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.AbstractSVGNumberList;
import org.apache.batik.dom.svg.ListBuilder;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGItem;
import org.apache.batik.dom.svg.SVGNumberItem;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGNumberList;

public class SVGOMAnimatedNumberList extends AbstractSVGAnimatedValue implements SVGAnimatedNumberList {
   protected BaseSVGNumberList baseVal;
   protected AnimSVGNumberList animVal;
   protected boolean changing;
   protected String defaultValue;
   protected boolean emptyAllowed;

   public SVGOMAnimatedNumberList(AbstractElement elt, String ns, String ln, String defaultValue, boolean emptyAllowed) {
      super(elt, ns, ln);
      this.defaultValue = defaultValue;
      this.emptyAllowed = emptyAllowed;
   }

   public SVGNumberList getBaseVal() {
      if (this.baseVal == null) {
         this.baseVal = new BaseSVGNumberList();
      }

      return this.baseVal;
   }

   public SVGNumberList getAnimVal() {
      if (this.animVal == null) {
         this.animVal = new AnimSVGNumberList();
      }

      return this.animVal;
   }

   public void check() {
      if (!this.hasAnimVal) {
         if (this.baseVal == null) {
            this.baseVal = new BaseSVGNumberList();
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
      SVGNumberList nl = this.getBaseVal();
      int n = nl.getNumberOfItems();
      float[] numbers = new float[n];

      for(int i = 0; i < n; ++i) {
         numbers[i] = nl.getItem(n).getValue();
      }

      return new AnimatableNumberListValue(target, numbers);
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         AnimatableNumberListValue animNumList = (AnimatableNumberListValue)val;
         if (this.animVal == null) {
            this.animVal = new AnimSVGNumberList();
         }

         this.animVal.setAnimatedValue(animNumList.getNumbers());
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

   protected class AnimSVGNumberList extends AbstractSVGNumberList {
      public AnimSVGNumberList() {
         this.itemList = new ArrayList(1);
      }

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedNumberList.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedNumberList.this.element).createSVGException(type, key, args);
      }

      protected Element getElement() {
         return SVGOMAnimatedNumberList.this.element;
      }

      public int getNumberOfItems() {
         return SVGOMAnimatedNumberList.this.hasAnimVal ? super.getNumberOfItems() : SVGOMAnimatedNumberList.this.getBaseVal().getNumberOfItems();
      }

      public SVGNumber getItem(int index) throws DOMException {
         return SVGOMAnimatedNumberList.this.hasAnimVal ? super.getItem(index) : SVGOMAnimatedNumberList.this.getBaseVal().getItem(index);
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
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      public SVGNumber initialize(SVGNumber newItem) throws DOMException, SVGException {
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      public SVGNumber insertItemBefore(SVGNumber newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      public SVGNumber replaceItem(SVGNumber newItem, int index) throws DOMException, SVGException {
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      public SVGNumber removeItem(int index) throws DOMException {
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      public SVGNumber appendItem(SVGNumber newItem) throws DOMException {
         throw SVGOMAnimatedNumberList.this.element.createDOMException((short)7, "readonly.number.list", (Object[])null);
      }

      protected void setAnimatedValue(float[] values) {
         int size = this.itemList.size();

         int i;
         for(i = 0; i < size && i < values.length; ++i) {
            SVGNumberItem n = (SVGNumberItem)this.itemList.get(i);
            n.setValue(values[i]);
         }

         while(i < values.length) {
            this.appendItemImpl(new SVGNumberItem(values[i]));
            ++i;
         }

         while(size > values.length) {
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

   public class BaseSVGNumberList extends AbstractSVGNumberList {
      protected boolean missing;
      protected boolean malformed;

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedNumberList.this.element.createDOMException(type, key, args);
      }

      protected SVGException createSVGException(short type, String key, Object[] args) {
         return ((SVGOMElement)SVGOMAnimatedNumberList.this.element).createSVGException(type, key, args);
      }

      protected Element getElement() {
         return SVGOMAnimatedNumberList.this.element;
      }

      protected String getValueAsString() {
         Attr attr = SVGOMAnimatedNumberList.this.element.getAttributeNodeNS(SVGOMAnimatedNumberList.this.namespaceURI, SVGOMAnimatedNumberList.this.localName);
         return attr == null ? SVGOMAnimatedNumberList.this.defaultValue : attr.getValue();
      }

      protected void setAttributeValue(String value) {
         try {
            SVGOMAnimatedNumberList.this.changing = true;
            SVGOMAnimatedNumberList.this.element.setAttributeNS(SVGOMAnimatedNumberList.this.namespaceURI, SVGOMAnimatedNumberList.this.localName, value);
         } finally {
            SVGOMAnimatedNumberList.this.changing = false;
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
            if (s != null && (!isEmpty || SVGOMAnimatedNumberList.this.emptyAllowed)) {
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
