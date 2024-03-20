package org.apache.batik.swing.svg;

import java.util.EventObject;
import org.apache.batik.gvt.GraphicsNode;

public class SVGLoadEventDispatcherEvent extends EventObject {
   protected GraphicsNode gvtRoot;

   public SVGLoadEventDispatcherEvent(Object source, GraphicsNode root) {
      super(source);
      this.gvtRoot = root;
   }

   public GraphicsNode getGVTRoot() {
      return this.gvtRoot;
   }
}
