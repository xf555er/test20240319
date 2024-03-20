package org.apache.batik.gvt.event;

import org.apache.batik.gvt.GraphicsNode;

public class GraphicsNodeMouseWheelEvent extends GraphicsNodeInputEvent {
   public static final int MOUSE_WHEEL = 600;
   protected int wheelDelta;

   public GraphicsNodeMouseWheelEvent(GraphicsNode source, int id, long when, int modifiers, int lockState, int wheelDelta) {
      super(source, id, when, modifiers, lockState);
      this.wheelDelta = wheelDelta;
   }

   public int getWheelDelta() {
      return this.wheelDelta;
   }
}
