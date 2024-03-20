package org.apache.batik.dom;

import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class GenericDOMImplementation extends AbstractDOMImplementation {
   protected static final DOMImplementation DOM_IMPLEMENTATION = new GenericDOMImplementation();

   public static DOMImplementation getDOMImplementation() {
      return DOM_IMPLEMENTATION;
   }

   public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
      Document result = new GenericDocument(doctype, this);
      result.appendChild(result.createElementNS(namespaceURI, qualifiedName));
      return result;
   }

   public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) {
      if (qualifiedName == null) {
         qualifiedName = "";
      }

      int test = XMLUtilities.testXMLQName(qualifiedName);
      if ((test & 1) == 0) {
         throw new DOMException((short)5, this.formatMessage("xml.name", new Object[]{qualifiedName}));
      } else if ((test & 2) == 0) {
         throw new DOMException((short)5, this.formatMessage("invalid.qname", new Object[]{qualifiedName}));
      } else {
         return new GenericDocumentType(qualifiedName, publicId, systemId);
      }
   }
}
