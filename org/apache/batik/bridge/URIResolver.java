package org.apache.batik.bridge;

import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

public class URIResolver {
   protected SVGOMDocument document;
   protected String documentURI;
   protected DocumentLoader documentLoader;

   public URIResolver(SVGDocument doc, DocumentLoader dl) {
      this.document = (SVGOMDocument)doc;
      this.documentLoader = dl;
   }

   public Element getElement(String uri, Element ref) throws MalformedURLException, IOException {
      Node n = this.getNode(uri, ref);
      if (n == null) {
         return null;
      } else if (n.getNodeType() == 9) {
         throw new IllegalArgumentException();
      } else {
         return (Element)n;
      }
   }

   public Node getNode(String uri, Element ref) throws MalformedURLException, IOException, SecurityException {
      String baseURI = this.getRefererBaseURI(ref);
      if (baseURI == null && uri.charAt(0) == '#') {
         return this.getNodeByFragment(uri.substring(1), ref);
      } else {
         ParsedURL purl = new ParsedURL(baseURI, uri);
         if (this.documentURI == null) {
            this.documentURI = this.document.getURL();
         }

         String frag = purl.getRef();
         ParsedURL pDocURL;
         if (frag != null && this.documentURI != null) {
            pDocURL = new ParsedURL(this.documentURI);
            if (pDocURL.sameFile(purl)) {
               return this.document.getElementById(frag);
            }
         }

         pDocURL = null;
         if (this.documentURI != null) {
            pDocURL = new ParsedURL(this.documentURI);
         }

         UserAgent userAgent = this.documentLoader.getUserAgent();
         userAgent.checkLoadExternalResource(purl, pDocURL);
         String purlStr = purl.toString();
         if (frag != null) {
            purlStr = purlStr.substring(0, purlStr.length() - (frag.length() + 1));
         }

         Document doc = this.documentLoader.loadDocument(purlStr);
         return (Node)(frag != null ? doc.getElementById(frag) : doc);
      }
   }

   protected String getRefererBaseURI(Element ref) {
      return ref.getBaseURI();
   }

   protected Node getNodeByFragment(String frag, Element ref) {
      return ref.getOwnerDocument().getElementById(frag);
   }
}
