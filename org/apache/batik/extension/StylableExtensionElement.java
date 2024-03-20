package org.apache.batik.extension;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGStylable;

public abstract class StylableExtensionElement extends ExtensionElement implements CSSStylableElement, SVGStylable {
   protected ParsedURL cssBase;
   protected StyleMap computedStyleMap;

   protected StylableExtensionElement() {
   }

   protected StylableExtensionElement(String name, AbstractDocument owner) {
      super(name, owner);
   }

   public StyleMap getComputedStyleMap(String pseudoElement) {
      return this.computedStyleMap;
   }

   public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
      this.computedStyleMap = sm;
   }

   public String getXMLId() {
      return this.getAttributeNS((String)null, "id");
   }

   public String getCSSClass() {
      return this.getAttributeNS((String)null, "class");
   }

   public ParsedURL getCSSBase() {
      if (this.cssBase == null) {
         String bu = this.getBaseURI();
         if (bu == null) {
            return null;
         }

         this.cssBase = new ParsedURL(bu);
      }

      return this.cssBase;
   }

   public boolean isPseudoInstanceOf(String pseudoClass) {
      if (!pseudoClass.equals("first-child")) {
         return false;
      } else {
         Node n;
         for(n = this.getPreviousSibling(); n != null && n.getNodeType() != 1; n = n.getPreviousSibling()) {
         }

         return n == null;
      }
   }

   public StyleDeclarationProvider getOverrideStyleDeclarationProvider() {
      return null;
   }

   public CSSStyleDeclaration getStyle() {
      throw new UnsupportedOperationException("Not implemented");
   }

   public CSSValue getPresentationAttribute(String name) {
      throw new UnsupportedOperationException("Not implemented");
   }

   public SVGAnimatedString getClassName() {
      throw new UnsupportedOperationException("Not implemented");
   }
}
