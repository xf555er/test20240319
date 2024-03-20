package org.apache.batik.bridge.svg12;

import java.util.EventObject;
import org.apache.batik.anim.dom.XBLOMContentElement;

public class ContentSelectionChangedEvent extends EventObject {
   public ContentSelectionChangedEvent(XBLOMContentElement c) {
      super(c);
   }

   public XBLOMContentElement getContentElement() {
      return (XBLOMContentElement)this.source;
   }
}
