package org.apache.batik.anim.dom;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.anim.values.AnimatableValue;

public abstract class AbstractSVGAnimatedValue implements AnimatedLiveAttributeValue {
   protected AbstractElement element;
   protected String namespaceURI;
   protected String localName;
   protected boolean hasAnimVal;
   protected LinkedList listeners = new LinkedList();

   public AbstractSVGAnimatedValue(AbstractElement elt, String ns, String ln) {
      this.element = elt;
      this.namespaceURI = ns;
      this.localName = ln;
   }

   public String getNamespaceURI() {
      return this.namespaceURI;
   }

   public String getLocalName() {
      return this.localName;
   }

   public boolean isSpecified() {
      return this.hasAnimVal || this.element.hasAttributeNS(this.namespaceURI, this.localName);
   }

   protected abstract void updateAnimatedValue(AnimatableValue var1);

   public void addAnimatedAttributeListener(AnimatedAttributeListener aal) {
      if (!this.listeners.contains(aal)) {
         this.listeners.add(aal);
      }

   }

   public void removeAnimatedAttributeListener(AnimatedAttributeListener aal) {
      this.listeners.remove(aal);
   }

   protected void fireBaseAttributeListeners() {
      if (this.element instanceof SVGOMElement) {
         ((SVGOMElement)this.element).fireBaseAttributeListeners(this.namespaceURI, this.localName);
      }

   }

   protected void fireAnimatedAttributeListeners() {
      Iterator var1 = this.listeners.iterator();

      while(var1.hasNext()) {
         Object listener1 = var1.next();
         AnimatedAttributeListener listener = (AnimatedAttributeListener)listener1;
         listener.animatedAttributeChanged(this.element, this);
      }

   }
}
