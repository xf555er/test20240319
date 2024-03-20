package org.apache.batik.bridge;

import org.apache.batik.dom.svg12.SVGGlobal;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.events.EventTarget;

public class GlobalWrapper extends WindowWrapper {
   public GlobalWrapper(Context context) {
      super(context);
      String[] names = new String[]{"startMouseCapture", "stopMouseCapture"};
      this.defineFunctionProperties(names, GlobalWrapper.class, 2);
   }

   public String getClassName() {
      return "SVGGlobal";
   }

   public String toString() {
      return "[object SVGGlobal]";
   }

   public static void startMouseCapture(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
      int len = args.length;
      GlobalWrapper gw = (GlobalWrapper)thisObj;
      SVGGlobal global = (SVGGlobal)gw.window;
      if (len >= 3) {
         EventTarget et = null;
         if (args[0] instanceof NativeJavaObject) {
            Object o = ((NativeJavaObject)args[0]).unwrap();
            if (o instanceof EventTarget) {
               et = (EventTarget)o;
            }
         }

         if (et == null) {
            throw Context.reportRuntimeError("First argument to startMouseCapture must be an EventTarget");
         }

         boolean sendAll = Context.toBoolean(args[1]);
         boolean autoRelease = Context.toBoolean(args[2]);
         global.startMouseCapture(et, sendAll, autoRelease);
      }

   }

   public static void stopMouseCapture(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
      GlobalWrapper gw = (GlobalWrapper)thisObj;
      SVGGlobal global = (SVGGlobal)gw.window;
      global.stopMouseCapture();
   }
}
