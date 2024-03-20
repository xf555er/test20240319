package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;

public abstract class SVGDescriptiveElement extends SVGStylableElement {
   protected SVGDescriptiveElement() {
   }

   protected SVGDescriptiveElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getXMLlang() {
      return XMLSupport.getXMLLang(this);
   }

   public void setXMLlang(String lang) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", lang);
   }

   public String getXMLspace() {
      return XMLSupport.getXMLSpace(this);
   }

   public void setXMLspace(String space) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", space);
   }
}
