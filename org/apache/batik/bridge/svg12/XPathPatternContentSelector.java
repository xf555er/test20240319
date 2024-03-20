package org.apache.batik.bridge.svg12;

import java.util.ArrayList;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.dom.AbstractDocument;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathPatternContentSelector extends AbstractContentSelector {
   protected NSPrefixResolver prefixResolver = new NSPrefixResolver();
   protected XPath xpath;
   protected XPathContext context;
   protected SelectedNodes selectedContent;
   protected String expression;

   public XPathPatternContentSelector(ContentManager cm, XBLOMContentElement content, Element bound, String selector) {
      super(cm, content, bound);
      this.expression = selector;
      this.parse();
   }

   protected void parse() {
      this.context = new XPathContext();

      try {
         this.xpath = new XPath(this.expression, (SourceLocator)null, this.prefixResolver, 1);
      } catch (TransformerException var3) {
         AbstractDocument doc = (AbstractDocument)this.contentElement.getOwnerDocument();
         throw doc.createXPathException((short)51, "xpath.invalid.expression", new Object[]{this.expression, var3.getMessage()});
      }
   }

   public NodeList getSelectedContent() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
      }

      return this.selectedContent;
   }

   boolean update() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
         return true;
      } else {
         this.parse();
         return this.selectedContent.update();
      }
   }

   protected class NSPrefixResolver implements PrefixResolver {
      public String getBaseIdentifier() {
         return null;
      }

      public String getNamespaceForPrefix(String prefix) {
         return XPathPatternContentSelector.this.contentElement.lookupNamespaceURI(prefix);
      }

      public String getNamespaceForPrefix(String prefix, Node context) {
         return XPathPatternContentSelector.this.contentElement.lookupNamespaceURI(prefix);
      }

      public boolean handlesNullPrefixes() {
         return false;
      }
   }

   protected class SelectedNodes implements NodeList {
      protected ArrayList nodes = new ArrayList(10);

      public SelectedNodes() {
         this.update();
      }

      protected boolean update() {
         ArrayList oldNodes = (ArrayList)this.nodes.clone();
         this.nodes.clear();

         for(Node n = XPathPatternContentSelector.this.boundElement.getFirstChild(); n != null; n = n.getNextSibling()) {
            this.update(n);
         }

         int nodesSize = this.nodes.size();
         if (oldNodes.size() != nodesSize) {
            return true;
         } else {
            for(int i = 0; i < nodesSize; ++i) {
               if (oldNodes.get(i) != this.nodes.get(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      protected boolean descendantSelected(Node n) {
         for(n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (XPathPatternContentSelector.this.isSelected(n) || this.descendantSelected(n)) {
               return true;
            }
         }

         return false;
      }

      protected void update(Node n) {
         if (!XPathPatternContentSelector.this.isSelected(n)) {
            try {
               double matchScore = XPathPatternContentSelector.this.xpath.execute(XPathPatternContentSelector.this.context, n, XPathPatternContentSelector.this.prefixResolver).num();
               if (matchScore != Double.NEGATIVE_INFINITY) {
                  if (!this.descendantSelected(n)) {
                     this.nodes.add(n);
                  }
               } else {
                  for(n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
                     this.update(n);
                  }
               }
            } catch (TransformerException var4) {
               AbstractDocument doc = (AbstractDocument)XPathPatternContentSelector.this.contentElement.getOwnerDocument();
               throw doc.createXPathException((short)51, "xpath.error", new Object[]{XPathPatternContentSelector.this.expression, var4.getMessage()});
            }
         }

      }

      public Node item(int index) {
         return index >= 0 && index < this.nodes.size() ? (Node)this.nodes.get(index) : null;
      }

      public int getLength() {
         return this.nodes.size();
      }
   }
}
