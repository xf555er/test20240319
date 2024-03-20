package org.apache.batik.bridge.svg12;

import java.util.HashMap;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractContentSelector {
   protected ContentManager contentManager;
   protected XBLOMContentElement contentElement;
   protected Element boundElement;
   protected static HashMap selectorFactories = new HashMap();

   public AbstractContentSelector(ContentManager cm, XBLOMContentElement content, Element bound) {
      this.contentManager = cm;
      this.contentElement = content;
      this.boundElement = bound;
   }

   public abstract NodeList getSelectedContent();

   abstract boolean update();

   protected boolean isSelected(Node n) {
      return this.contentManager.getContentElement(n) != null;
   }

   public static AbstractContentSelector createSelector(String selectorLanguage, ContentManager cm, XBLOMContentElement content, Element bound, String selector) {
      ContentSelectorFactory f = (ContentSelectorFactory)selectorFactories.get(selectorLanguage);
      if (f == null) {
         throw new RuntimeException("Invalid XBL content selector language '" + selectorLanguage + "'");
      } else {
         return f.createSelector(cm, content, bound, selector);
      }
   }

   static {
      ContentSelectorFactory f1 = new XPathPatternContentSelectorFactory();
      ContentSelectorFactory f2 = new XPathSubsetContentSelectorFactory();
      selectorFactories.put((Object)null, f1);
      selectorFactories.put("XPathPattern", f1);
      selectorFactories.put("XPathSubset", f2);
   }

   protected static class XPathPatternContentSelectorFactory implements ContentSelectorFactory {
      public AbstractContentSelector createSelector(ContentManager cm, XBLOMContentElement content, Element bound, String selector) {
         return new XPathPatternContentSelector(cm, content, bound, selector);
      }
   }

   protected static class XPathSubsetContentSelectorFactory implements ContentSelectorFactory {
      public AbstractContentSelector createSelector(ContentManager cm, XBLOMContentElement content, Element bound, String selector) {
         return new XPathSubsetContentSelector(cm, content, bound, selector);
      }
   }

   protected interface ContentSelectorFactory {
      AbstractContentSelector createSelector(ContentManager var1, XBLOMContentElement var2, Element var3, String var4);
   }
}
