package org.apache.batik.bridge;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.w3c.dom.events.EventTarget;

class BatikWrapFactory extends WrapFactory {
   private RhinoInterpreter interpreter;

   public BatikWrapFactory(RhinoInterpreter interp) {
      this.interpreter = interp;
      this.setJavaPrimitiveWrap(false);
   }

   public Object wrap(Context ctx, Scriptable scope, Object obj, Class staticType) {
      return obj instanceof EventTarget ? this.interpreter.buildEventTargetWrapper((EventTarget)obj) : super.wrap(ctx, scope, obj, staticType);
   }
}
