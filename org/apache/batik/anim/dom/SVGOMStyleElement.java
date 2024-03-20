package org.apache.batik.anim.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.svg.SVGStyleElement;

public class SVGOMStyleElement extends SVGOMElement implements CSSStyleSheetNode, SVGStyleElement, LinkStyle {
   protected static final AttributeInitializer attributeInitializer = new AttributeInitializer(1);
   protected transient StyleSheet sheet;
   protected transient org.apache.batik.css.engine.StyleSheet styleSheet;
   protected transient EventListener domCharacterDataModifiedListener = new DOMCharacterDataModifiedListener();

   protected SVGOMStyleElement() {
   }

   public SVGOMStyleElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "style";
   }

   public org.apache.batik.css.engine.StyleSheet getCSSStyleSheet() {
      if (this.styleSheet == null && this.getType().equals("text/css")) {
         SVGOMDocument doc = (SVGOMDocument)this.getOwnerDocument();
         CSSEngine e = doc.getCSSEngine();
         String text = "";
         Node n = this.getFirstChild();
         if (n != null) {
            StringBuffer sb = new StringBuffer();

            while(true) {
               if (n == null) {
                  text = sb.toString();
                  break;
               }

               if (n.getNodeType() == 4 || n.getNodeType() == 3) {
                  sb.append(n.getNodeValue());
               }

               n = n.getNextSibling();
            }
         }

         ParsedURL burl = null;
         String bu = this.getBaseURI();
         if (bu != null) {
            burl = new ParsedURL(bu);
         }

         String media = this.getAttributeNS((String)null, "media");
         this.styleSheet = e.parseStyleSheet(text, burl, media);
         this.addEventListenerNS("http://www.w3.org/2001/xml-events", "DOMCharacterDataModified", this.domCharacterDataModifiedListener, false, (Object)null);
      }

      return this.styleSheet;
   }

   public StyleSheet getSheet() {
      throw new UnsupportedOperationException("LinkStyle.getSheet() is not implemented");
   }

   public String getXMLspace() {
      return XMLSupport.getXMLSpace(this);
   }

   public void setXMLspace(String space) throws DOMException {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", space);
   }

   public String getType() {
      return this.hasAttributeNS((String)null, "type") ? this.getAttributeNS((String)null, "type") : "text/css";
   }

   public void setType(String type) throws DOMException {
      this.setAttributeNS((String)null, "type", type);
   }

   public String getMedia() {
      return this.getAttribute("media");
   }

   public void setMedia(String media) throws DOMException {
      this.setAttribute("media", media);
   }

   public String getTitle() {
      return this.getAttribute("title");
   }

   public void setTitle(String title) throws DOMException {
      this.setAttribute("title", title);
   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMStyleElement();
   }

   static {
      attributeInitializer.addAttribute("http://www.w3.org/XML/1998/namespace", "xml", "space", "preserve");
   }

   protected class DOMCharacterDataModifiedListener implements EventListener {
      public void handleEvent(Event evt) {
         SVGOMStyleElement.this.styleSheet = null;
      }
   }
}
