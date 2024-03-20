package org.apache.batik.swing.svg;

import java.util.EventObject;
import org.apache.batik.gvt.GraphicsNode;

public class GVTTreeBuilderEvent extends EventObject {
   protected GraphicsNode gvtRoot;

   public GVTTreeBuilderEvent(Object source, GraphicsNode root) {
      super(source);
      this.gvtRoot = root;
   }

   public GraphicsNode getGVTRoot() {
      return this.gvtRoot;
   }
}
