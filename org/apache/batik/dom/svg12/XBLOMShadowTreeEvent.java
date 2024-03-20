package org.apache.batik.dom.svg12;

import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.xbl.ShadowTreeEvent;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;

public class XBLOMShadowTreeEvent extends AbstractEvent implements ShadowTreeEvent {
   protected XBLShadowTreeElement xblShadowTree;

   public XBLShadowTreeElement getXblShadowTree() {
      return this.xblShadowTree;
   }

   public void initShadowTreeEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, XBLShadowTreeElement xblShadowTreeArg) {
      this.initEvent(typeArg, canBubbleArg, cancelableArg);
      this.xblShadowTree = xblShadowTreeArg;
   }

   public void initShadowTreeEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, XBLShadowTreeElement xblShadowTreeArg) {
      this.initEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg);
      this.xblShadowTree = xblShadowTreeArg;
   }
}
