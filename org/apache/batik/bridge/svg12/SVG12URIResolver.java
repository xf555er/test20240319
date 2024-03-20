package org.apache.batik.bridge.svg12;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

public class SVG12URIResolver extends URIResolver {
   public SVG12URIResolver(SVGDocument doc, DocumentLoader dl) {
      super(doc, dl);
   }

   protected String getRefererBaseURI(Element ref) {
      AbstractNode aref = (AbstractNode)ref;
      return aref.getXblBoundElement() != null ? null : aref.getBaseURI();
   }

   protected Node getNodeByFragment(String frag, Element ref) {
      NodeXBL refx = (NodeXBL)ref;
      NodeXBL boundElt = (NodeXBL)refx.getXblBoundElement();
      if (boundElt != null) {
         XBLShadowTreeElement shadow = (XBLShadowTreeElement)boundElt.getXblShadowTree();
         Node n = shadow.getElementById(frag);
         if (n != null) {
            return n;
         }

         NodeList nl = refx.getXblDefinitions();

         for(int i = 0; i < nl.getLength(); ++i) {
            n = nl.item(i).getOwnerDocument().getElementById(frag);
            if (n != null) {
               return n;
            }
         }
      }

      return super.getNodeByFragment(frag, ref);
   }
}
