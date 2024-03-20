package org.apache.batik.gvt.event;

import java.util.EventObject;
import org.apache.batik.gvt.GraphicsNode;

public class GraphicsNodeEvent extends EventObject {
   private boolean consumed = false;
   protected int id;

   public GraphicsNodeEvent(GraphicsNode source, int id) {
      super(source);
      this.id = id;
   }

   public int getID() {
      return this.id;
   }

   public GraphicsNode getGraphicsNode() {
      return (GraphicsNode)this.source;
   }

   public void consume() {
      this.consumed = true;
   }

   public boolean isConsumed() {
      return this.consumed;
   }
}
