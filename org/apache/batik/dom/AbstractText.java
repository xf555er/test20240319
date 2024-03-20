package org.apache.batik.dom;

import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public abstract class AbstractText extends AbstractCharacterData implements Text {
   public Text splitText(int offset) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else {
         String v = this.getNodeValue();
         if (offset >= 0 && offset < v.length()) {
            Node n = this.getParentNode();
            if (n == null) {
               throw this.createDOMException((short)1, "need.parent", new Object[0]);
            } else {
               String t1 = v.substring(offset);
               Text t = this.createTextNode(t1);
               Node ns = this.getNextSibling();
               if (ns != null) {
                  n.insertBefore(t, ns);
               } else {
                  n.appendChild(t);
               }

               this.setNodeValue(v.substring(0, offset));
               return t;
            }
         } else {
            throw this.createDOMException((short)1, "offset", new Object[]{offset});
         }
      }
   }

   protected Node getPreviousLogicallyAdjacentTextNode(Node n) {
      Node p = n.getPreviousSibling();

      for(Node parent = n.getParentNode(); p == null && parent != null && parent.getNodeType() == 5; p = p.getPreviousSibling()) {
         p = parent;
         parent = parent.getParentNode();
      }

      while(p != null && p.getNodeType() == 5) {
         p = p.getLastChild();
      }

      if (p == null) {
         return null;
      } else {
         int nt = p.getNodeType();
         return nt != 3 && nt != 4 ? null : p;
      }
   }

   protected Node getNextLogicallyAdjacentTextNode(Node n) {
      Node p = n.getNextSibling();

      for(Node parent = n.getParentNode(); p == null && parent != null && parent.getNodeType() == 5; p = p.getNextSibling()) {
         p = parent;
         parent = parent.getParentNode();
      }

      while(p != null && p.getNodeType() == 5) {
         p = p.getFirstChild();
      }

      if (p == null) {
         return null;
      } else {
         int nt = p.getNodeType();
         return nt != 3 && nt != 4 ? null : p;
      }
   }

   public String getWholeText() {
      StringBuffer sb = new StringBuffer();

      for(Node n = this; n != null; n = this.getPreviousLogicallyAdjacentTextNode((Node)n)) {
         sb.insert(0, ((Node)n).getNodeValue());
      }

      for(Node n = this.getNextLogicallyAdjacentTextNode(this); n != null; n = this.getNextLogicallyAdjacentTextNode(n)) {
         sb.append(n.getNodeValue());
      }

      return sb.toString();
   }

   public boolean isElementContentWhitespace() {
      int len = this.nodeValue.length();

      for(int i = 0; i < len; ++i) {
         if (!XMLUtilities.isXMLSpace(this.nodeValue.charAt(i))) {
            return false;
         }
      }

      Node p = this.getParentNode();
      if (p.getNodeType() == 1) {
         String sp = XMLSupport.getXMLSpace((Element)p);
         return !sp.equals("preserve");
      } else {
         return true;
      }
   }

   public Text replaceWholeText(String s) throws DOMException {
      Node parent;
      AbstractNode an;
      for(parent = this.getPreviousLogicallyAdjacentTextNode(this); parent != null; parent = this.getPreviousLogicallyAdjacentTextNode(parent)) {
         an = (AbstractNode)parent;
         if (an.isReadonly()) {
            throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(parent.getNodeType()), parent.getNodeName()});
         }
      }

      for(parent = this.getNextLogicallyAdjacentTextNode(this); parent != null; parent = this.getNextLogicallyAdjacentTextNode(parent)) {
         an = (AbstractNode)parent;
         if (an.isReadonly()) {
            throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(parent.getNodeType()), parent.getNodeName()});
         }
      }

      parent = this.getParentNode();

      Node n;
      for(n = this.getPreviousLogicallyAdjacentTextNode(this); n != null; n = this.getPreviousLogicallyAdjacentTextNode(n)) {
         parent.removeChild(n);
      }

      for(n = this.getNextLogicallyAdjacentTextNode(this); n != null; n = this.getNextLogicallyAdjacentTextNode(n)) {
         parent.removeChild(n);
      }

      if (this.isReadonly()) {
         Text t = this.createTextNode(s);
         parent.replaceChild(t, this);
         return t;
      } else {
         this.setNodeValue(s);
         return this;
      }
   }

   public String getTextContent() {
      return this.isElementContentWhitespace() ? "" : this.getNodeValue();
   }

   protected abstract Text createTextNode(String var1);
}
