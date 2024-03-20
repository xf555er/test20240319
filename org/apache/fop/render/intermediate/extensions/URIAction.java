package org.apache.fop.render.intermediate.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class URIAction extends AbstractAction implements DocumentNavigationExtensionConstants {
   private String uri;
   private boolean newWindow;

   public URIAction(String uri, boolean newWindow) {
      if (uri == null) {
         throw new NullPointerException("uri must not be null");
      } else {
         this.uri = uri;
         this.newWindow = newWindow;
         this.setID(this.getIDPrefix() + (uri + newWindow).hashCode());
      }
   }

   public String getURI() {
      return this.uri;
   }

   public boolean isNewWindow() {
      return this.newWindow;
   }

   public boolean isSame(AbstractAction other) {
      if (other == null) {
         throw new NullPointerException("other must not be null");
      } else if (!(other instanceof URIAction)) {
         return false;
      } else {
         URIAction otherAction = (URIAction)other;
         if (!this.getURI().equals(otherAction.getURI())) {
            return false;
         } else if (this.isNewWindow() != otherAction.isNewWindow()) {
            return false;
         } else {
            return this.getStructureTreeElement() != null ? this.getStructureTreeElement().equals(other.getStructureTreeElement()) : true;
         }
      }
   }

   public String getIDPrefix() {
      return "fop-" + GOTO_URI.getLocalName();
   }

   public void toSAX(ContentHandler handler) throws SAXException {
      AttributesImpl atts = new AttributesImpl();
      if (this.hasID()) {
         atts.addAttribute("", "id", "id", "CDATA", this.getID());
      }

      atts.addAttribute("", "uri", "uri", "CDATA", this.getURI());
      if (this.isNewWindow()) {
         atts.addAttribute("", "show-destination", "show-destination", "CDATA", "new");
      }

      handler.startElement(GOTO_URI.getNamespaceURI(), GOTO_URI.getLocalName(), GOTO_URI.getQName(), atts);
      handler.endElement(GOTO_URI.getNamespaceURI(), GOTO_URI.getLocalName(), GOTO_URI.getQName());
   }
}
