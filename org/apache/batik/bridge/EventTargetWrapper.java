package org.apache.batik.bridge;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.script.ScriptEventWrapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

class EventTargetWrapper extends NativeJavaObject {
   protected static WeakHashMap mapOfListenerMap;
   public static final String ADD_NAME = "addEventListener";
   public static final String ADDNS_NAME = "addEventListenerNS";
   public static final String REMOVE_NAME = "removeEventListener";
   public static final String REMOVENS_NAME = "removeEventListenerNS";
   protected RhinoInterpreter interpreter;

   EventTargetWrapper(Scriptable scope, EventTarget object, RhinoInterpreter interpreter) {
      super(scope, object, (Class)null);
      this.interpreter = interpreter;
   }

   public Object get(String name, Scriptable start) {
      Object method = super.get(name, start);
      if (name.equals("addEventListener")) {
         method = new FunctionAddProxy(this.interpreter, (Function)method, this.initMap());
      } else if (name.equals("removeEventListener")) {
         method = new FunctionRemoveProxy((Function)method, this.initMap());
      } else if (name.equals("addEventListenerNS")) {
         method = new FunctionAddNSProxy(this.interpreter, (Function)method, this.initMap());
      } else if (name.equals("removeEventListenerNS")) {
         method = new FunctionRemoveNSProxy((Function)method, this.initMap());
      }

      return method;
   }

   public Map initMap() {
      Map map = null;
      if (mapOfListenerMap == null) {
         mapOfListenerMap = new WeakHashMap(10);
      }

      if ((map = (Map)mapOfListenerMap.get(this.unwrap())) == null) {
         mapOfListenerMap.put(this.unwrap(), map = new WeakHashMap(2));
      }

      return (Map)map;
   }

   static class FunctionRemoveNSProxy extends FunctionProxy {
      protected Map listenerMap;

      FunctionRemoveNSProxy(Function delegate, Map listenerMap) {
         super(delegate);
         this.listenerMap = listenerMap;
      }

      public Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
         NativeJavaObject njo = (NativeJavaObject)thisObj;
         SoftReference sr;
         EventListener el;
         Class[] paramTypes;
         int i;
         AbstractNode target;
         if (args[2] instanceof Function) {
            sr = (SoftReference)this.listenerMap.get(args[2]);
            if (sr == null) {
               return Undefined.instance;
            } else {
               el = (EventListener)sr.get();
               if (el == null) {
                  return Undefined.instance;
               } else {
                  paramTypes = new Class[]{String.class, String.class, Function.class, Boolean.TYPE};

                  for(i = 0; i < args.length; ++i) {
                     args[i] = Context.jsToJava(args[i], paramTypes[i]);
                  }

                  target = (AbstractNode)njo.unwrap();
                  target.removeEventListenerNS((String)args[0], (String)args[1], el, (Boolean)args[3]);
                  return Undefined.instance;
               }
            }
         } else if (!(args[2] instanceof NativeObject)) {
            return this.delegate.call(ctx, scope, thisObj, args);
         } else {
            sr = (SoftReference)this.listenerMap.get(args[2]);
            if (sr == null) {
               return Undefined.instance;
            } else {
               el = (EventListener)sr.get();
               if (el == null) {
                  return Undefined.instance;
               } else {
                  paramTypes = new Class[]{String.class, String.class, Scriptable.class, Boolean.TYPE};

                  for(i = 0; i < args.length; ++i) {
                     args[i] = Context.jsToJava(args[i], paramTypes[i]);
                  }

                  target = (AbstractNode)njo.unwrap();
                  target.removeEventListenerNS((String)args[0], (String)args[1], el, (Boolean)args[3]);
                  return Undefined.instance;
               }
            }
         }
      }
   }

   static class FunctionAddNSProxy extends FunctionProxy {
      protected Map listenerMap;
      protected RhinoInterpreter interpreter;

      FunctionAddNSProxy(RhinoInterpreter interpreter, Function delegate, Map listenerMap) {
         super(delegate);
         this.listenerMap = listenerMap;
         this.interpreter = interpreter;
      }

      public Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
         NativeJavaObject njo = (NativeJavaObject)thisObj;
         Class[] paramTypes;
         int i;
         AbstractNode target;
         if (args[2] instanceof Function) {
            EventListener evtListener = new FunctionEventListener((Function)args[2], this.interpreter);
            this.listenerMap.put(args[2], new SoftReference(evtListener));
            paramTypes = new Class[]{String.class, String.class, Function.class, Boolean.TYPE, Object.class};

            for(i = 0; i < args.length; ++i) {
               args[i] = Context.jsToJava(args[i], paramTypes[i]);
            }

            target = (AbstractNode)njo.unwrap();
            target.addEventListenerNS((String)args[0], (String)args[1], evtListener, (Boolean)args[3], args[4]);
            return Undefined.instance;
         } else if (!(args[2] instanceof NativeObject)) {
            return this.delegate.call(ctx, scope, thisObj, args);
         } else {
            EventListener evtListener = new HandleEventListener((Scriptable)args[2], this.interpreter);
            this.listenerMap.put(args[2], new SoftReference(evtListener));
            paramTypes = new Class[]{String.class, String.class, Scriptable.class, Boolean.TYPE, Object.class};

            for(i = 0; i < args.length; ++i) {
               args[i] = Context.jsToJava(args[i], paramTypes[i]);
            }

            target = (AbstractNode)njo.unwrap();
            target.addEventListenerNS((String)args[0], (String)args[1], evtListener, (Boolean)args[3], args[4]);
            return Undefined.instance;
         }
      }
   }

   static class FunctionRemoveProxy extends FunctionProxy {
      public Map listenerMap;

      FunctionRemoveProxy(Function delegate, Map listenerMap) {
         super(delegate);
         this.listenerMap = listenerMap;
      }

      public Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
         NativeJavaObject njo = (NativeJavaObject)thisObj;
         SoftReference sr;
         EventListener el;
         Class[] paramTypes;
         int i;
         if (args[1] instanceof Function) {
            sr = (SoftReference)this.listenerMap.get(args[1]);
            if (sr == null) {
               return Undefined.instance;
            } else {
               el = (EventListener)sr.get();
               if (el == null) {
                  return Undefined.instance;
               } else {
                  paramTypes = new Class[]{String.class, Function.class, Boolean.TYPE};

                  for(i = 0; i < args.length; ++i) {
                     args[i] = Context.jsToJava(args[i], paramTypes[i]);
                  }

                  ((EventTarget)njo.unwrap()).removeEventListener((String)args[0], el, (Boolean)args[2]);
                  return Undefined.instance;
               }
            }
         } else if (!(args[1] instanceof NativeObject)) {
            return this.delegate.call(ctx, scope, thisObj, args);
         } else {
            sr = (SoftReference)this.listenerMap.get(args[1]);
            if (sr == null) {
               return Undefined.instance;
            } else {
               el = (EventListener)sr.get();
               if (el == null) {
                  return Undefined.instance;
               } else {
                  paramTypes = new Class[]{String.class, Scriptable.class, Boolean.TYPE};

                  for(i = 0; i < args.length; ++i) {
                     args[i] = Context.jsToJava(args[i], paramTypes[i]);
                  }

                  ((EventTarget)njo.unwrap()).removeEventListener((String)args[0], el, (Boolean)args[2]);
                  return Undefined.instance;
               }
            }
         }
      }
   }

   static class FunctionAddProxy extends FunctionProxy {
      protected Map listenerMap;
      protected RhinoInterpreter interpreter;

      FunctionAddProxy(RhinoInterpreter interpreter, Function delegate, Map listenerMap) {
         super(delegate);
         this.listenerMap = listenerMap;
         this.interpreter = interpreter;
      }

      public Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
         NativeJavaObject njo = (NativeJavaObject)thisObj;
         Object evtListener;
         SoftReference sr;
         Class[] paramTypes;
         int i;
         if (args[1] instanceof Function) {
            evtListener = null;
            sr = (SoftReference)this.listenerMap.get(args[1]);
            if (sr != null) {
               evtListener = (EventListener)sr.get();
            }

            if (evtListener == null) {
               evtListener = new FunctionEventListener((Function)args[1], this.interpreter);
               this.listenerMap.put(args[1], new SoftReference(evtListener));
            }

            paramTypes = new Class[]{String.class, Function.class, Boolean.TYPE};

            for(i = 0; i < args.length; ++i) {
               args[i] = Context.jsToJava(args[i], paramTypes[i]);
            }

            ((EventTarget)njo.unwrap()).addEventListener((String)args[0], (EventListener)evtListener, (Boolean)args[2]);
            return Undefined.instance;
         } else if (!(args[1] instanceof NativeObject)) {
            return this.delegate.call(ctx, scope, thisObj, args);
         } else {
            evtListener = null;
            sr = (SoftReference)this.listenerMap.get(args[1]);
            if (sr != null) {
               evtListener = (EventListener)sr.get();
            }

            if (evtListener == null) {
               evtListener = new HandleEventListener((Scriptable)args[1], this.interpreter);
               this.listenerMap.put(args[1], new SoftReference(evtListener));
            }

            paramTypes = new Class[]{String.class, Scriptable.class, Boolean.TYPE};

            for(i = 0; i < args.length; ++i) {
               args[i] = Context.jsToJava(args[i], paramTypes[i]);
            }

            ((EventTarget)njo.unwrap()).addEventListener((String)args[0], (EventListener)evtListener, (Boolean)args[2]);
            return Undefined.instance;
         }
      }
   }

   abstract static class FunctionProxy implements Function {
      protected Function delegate;

      public FunctionProxy(Function delegate) {
         this.delegate = delegate;
      }

      public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
         return this.delegate.construct(cx, scope, args);
      }

      public String getClassName() {
         return this.delegate.getClassName();
      }

      public Object get(String name, Scriptable start) {
         return this.delegate.get(name, start);
      }

      public Object get(int index, Scriptable start) {
         return this.delegate.get(index, start);
      }

      public boolean has(String name, Scriptable start) {
         return this.delegate.has(name, start);
      }

      public boolean has(int index, Scriptable start) {
         return this.delegate.has(index, start);
      }

      public void put(String name, Scriptable start, Object value) {
         this.delegate.put(name, start, value);
      }

      public void put(int index, Scriptable start, Object value) {
         this.delegate.put(index, start, value);
      }

      public void delete(String name) {
         this.delegate.delete(name);
      }

      public void delete(int index) {
         this.delegate.delete(index);
      }

      public Scriptable getPrototype() {
         return this.delegate.getPrototype();
      }

      public void setPrototype(Scriptable prototype) {
         this.delegate.setPrototype(prototype);
      }

      public Scriptable getParentScope() {
         return this.delegate.getParentScope();
      }

      public void setParentScope(Scriptable parent) {
         this.delegate.setParentScope(parent);
      }

      public Object[] getIds() {
         return this.delegate.getIds();
      }

      public Object getDefaultValue(Class hint) {
         return this.delegate.getDefaultValue(hint);
      }

      public boolean hasInstance(Scriptable instance) {
         return this.delegate.hasInstance(instance);
      }
   }

   static class HandleEventListener implements EventListener {
      public static final String HANDLE_EVENT = "handleEvent";
      public Scriptable scriptable;
      public Object[] array = new Object[1];
      public RhinoInterpreter interpreter;

      HandleEventListener(Scriptable s, RhinoInterpreter interpreter) {
         this.scriptable = s;
         this.interpreter = interpreter;
      }

      public void handleEvent(Event evt) {
         if (evt instanceof ScriptEventWrapper) {
            this.array[0] = ((ScriptEventWrapper)evt).getEventObject();
         } else {
            this.array[0] = evt;
         }

         ContextAction handleEventAction = new ContextAction() {
            public Object run(Context cx) {
               ScriptableObject.callMethod(HandleEventListener.this.scriptable, "handleEvent", HandleEventListener.this.array);
               return null;
            }
         };
         this.interpreter.call(handleEventAction);
      }
   }

   static class FunctionEventListener implements EventListener {
      protected Function function;
      protected RhinoInterpreter interpreter;

      FunctionEventListener(Function f, RhinoInterpreter i) {
         this.function = f;
         this.interpreter = i;
      }

      public void handleEvent(Event evt) {
         Object event;
         if (evt instanceof ScriptEventWrapper) {
            event = ((ScriptEventWrapper)evt).getEventObject();
         } else {
            event = evt;
         }

         this.interpreter.callHandler(this.function, event);
      }
   }
}
