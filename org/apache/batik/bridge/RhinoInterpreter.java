package org.apache.batik.bridge;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.batik.script.ImportInfo;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.rhino.BatikSecurityController;
import org.apache.batik.script.rhino.RhinoClassLoader;
import org.apache.batik.script.rhino.RhinoClassShutter;
import org.mozilla.javascript.ClassCache;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.w3c.dom.events.EventTarget;

public class RhinoInterpreter implements Interpreter {
   private static final int MAX_CACHED_SCRIPTS = 32;
   public static final String SOURCE_NAME_SVG = "<SVG>";
   public static final String BIND_NAME_WINDOW = "window";
   protected static List contexts = new LinkedList();
   protected Window window;
   protected ScriptableObject globalObject = null;
   protected LinkedList compiledScripts = new LinkedList();
   protected WrapFactory wrapFactory = new BatikWrapFactory(this);
   protected ClassShutter classShutter = new RhinoClassShutter();
   protected RhinoClassLoader rhinoClassLoader;
   protected SecurityController securityController = new BatikSecurityController();
   protected ContextFactory contextFactory = new Factory();
   protected Context defaultContext;

   public RhinoInterpreter(URL documentURL) {
      this.init(documentURL, (ImportInfo)null);
   }

   public RhinoInterpreter(URL documentURL, ImportInfo imports) {
      this.init(documentURL, imports);
   }

   protected void init(URL documentURL, final ImportInfo imports) {
      try {
         this.rhinoClassLoader = new RhinoClassLoader(documentURL, this.getClass().getClassLoader());
      } catch (SecurityException var4) {
         this.rhinoClassLoader = null;
      }

      ContextAction initAction = new ContextAction() {
         public Object run(Context cx) {
            Scriptable scriptable = cx.initStandardObjects((ScriptableObject)null, false);
            RhinoInterpreter.this.defineGlobalWrapperClass(scriptable);
            RhinoInterpreter.this.globalObject = RhinoInterpreter.this.createGlobalObject(cx);
            ClassCache cache = ClassCache.get(RhinoInterpreter.this.globalObject);
            cache.setCachingEnabled(RhinoInterpreter.this.rhinoClassLoader != null);
            ImportInfo ii = imports;
            if (ii == null) {
               ii = ImportInfo.getImports();
            }

            StringBuffer sb = new StringBuffer();
            Iterator iter = ii.getPackages();

            String cls;
            while(iter.hasNext()) {
               cls = (String)iter.next();
               sb.append("importPackage(Packages.");
               sb.append(cls);
               sb.append(");");
            }

            iter = ii.getClasses();

            while(iter.hasNext()) {
               cls = (String)iter.next();
               sb.append("importClass(Packages.");
               sb.append(cls);
               sb.append(");");
            }

            cx.evaluateString(RhinoInterpreter.this.globalObject, sb.toString(), (String)null, 0, RhinoInterpreter.this.rhinoClassLoader);
            return null;
         }
      };
      this.contextFactory.call(initAction);
   }

   public String[] getMimeTypes() {
      return RhinoInterpreterFactory.RHINO_MIMETYPES;
   }

   public Window getWindow() {
      return this.window;
   }

   public ContextFactory getContextFactory() {
      return this.contextFactory;
   }

   protected void defineGlobalWrapperClass(Scriptable global) {
      try {
         ScriptableObject.defineClass(global, WindowWrapper.class);
      } catch (Exception var3) {
      }

   }

   protected ScriptableObject createGlobalObject(Context ctx) {
      return new WindowWrapper(ctx);
   }

   public AccessControlContext getAccessControlContext() {
      return this.rhinoClassLoader == null ? null : this.rhinoClassLoader.getAccessControlContext();
   }

   protected ScriptableObject getGlobalObject() {
      return this.globalObject;
   }

   public Object evaluate(Reader scriptreader) throws IOException {
      return this.evaluate(scriptreader, "<SVG>");
   }

   public Object evaluate(final Reader scriptReader, final String description) throws IOException {
      ContextAction evaluateAction = new ContextAction() {
         public Object run(Context cx) {
            try {
               return cx.evaluateReader(RhinoInterpreter.this.globalObject, scriptReader, description, 1, RhinoInterpreter.this.rhinoClassLoader);
            } catch (IOException var3) {
               throw new WrappedException(var3);
            }
         }
      };

      try {
         return this.contextFactory.call(evaluateAction);
      } catch (JavaScriptException var7) {
         Object value = var7.getValue();
         Exception ex = value instanceof Exception ? (Exception)value : var7;
         throw new InterpreterException((Exception)ex, ((Exception)ex).getMessage(), -1, -1);
      } catch (WrappedException var8) {
         Throwable w = var8.getWrappedException();
         if (w instanceof Exception) {
            throw new InterpreterException((Exception)w, w.getMessage(), -1, -1);
         } else {
            throw new InterpreterException(w.getMessage(), -1, -1);
         }
      } catch (InterruptedBridgeException var9) {
         throw var9;
      } catch (RuntimeException var10) {
         throw new InterpreterException(var10, var10.getMessage(), -1, -1);
      }
   }

   public Object evaluate(final String scriptStr) {
      ContextAction evalAction = new ContextAction() {
         public Object run(final Context cx) {
            Script script = null;
            Entry entry = null;
            Iterator it = RhinoInterpreter.this.compiledScripts.iterator();

            while(it.hasNext()) {
               if ((entry = (Entry)it.next()).str.equals(scriptStr)) {
                  script = entry.script;
                  it.remove();
                  break;
               }
            }

            if (script == null) {
               PrivilegedAction compile = new PrivilegedAction() {
                  public Object run() {
                     try {
                        return cx.compileReader(new StringReader(scriptStr), "<SVG>", 1, RhinoInterpreter.this.rhinoClassLoader);
                     } catch (IOException var2) {
                        throw new RuntimeException(var2.getMessage());
                     }
                  }
               };
               script = (Script)AccessController.doPrivileged(compile);
               if (RhinoInterpreter.this.compiledScripts.size() + 1 > 32) {
                  RhinoInterpreter.this.compiledScripts.removeFirst();
               }

               RhinoInterpreter.this.compiledScripts.addLast(new Entry(scriptStr, script));
            } else {
               RhinoInterpreter.this.compiledScripts.addLast(entry);
            }

            return script.exec(cx, RhinoInterpreter.this.globalObject);
         }
      };

      try {
         return this.contextFactory.call(evalAction);
      } catch (InterpreterException var6) {
         throw var6;
      } catch (JavaScriptException var7) {
         Object value = var7.getValue();
         Exception ex = value instanceof Exception ? (Exception)value : var7;
         throw new InterpreterException((Exception)ex, ((Exception)ex).getMessage(), -1, -1);
      } catch (WrappedException var8) {
         Throwable w = var8.getWrappedException();
         if (w instanceof Exception) {
            throw new InterpreterException((Exception)w, w.getMessage(), -1, -1);
         } else {
            throw new InterpreterException(w.getMessage(), -1, -1);
         }
      } catch (RuntimeException var9) {
         throw new InterpreterException(var9, var9.getMessage(), -1, -1);
      }
   }

   public void dispose() {
      if (this.rhinoClassLoader != null) {
         ClassCache cache = ClassCache.get(this.globalObject);
         cache.setCachingEnabled(false);
      }

   }

   public void bindObject(final String name, final Object object) {
      this.contextFactory.call(new ContextAction() {
         public Object run(Context cx) {
            Object o = object;
            if (name.equals("window") && object instanceof Window) {
               ((WindowWrapper)RhinoInterpreter.this.globalObject).window = (Window)object;
               RhinoInterpreter.this.window = (Window)object;
               o = RhinoInterpreter.this.globalObject;
            }

            Scriptable jsObject = Context.toObject(o, RhinoInterpreter.this.globalObject);
            RhinoInterpreter.this.globalObject.put(name, RhinoInterpreter.this.globalObject, jsObject);
            return null;
         }
      });
   }

   void callHandler(final Function handler, final Object arg) {
      this.contextFactory.call(new ContextAction() {
         public Object run(Context cx) {
            Object a = Context.toObject(arg, RhinoInterpreter.this.globalObject);
            Object[] args = new Object[]{a};
            handler.call(cx, RhinoInterpreter.this.globalObject, RhinoInterpreter.this.globalObject, args);
            return null;
         }
      });
   }

   void callMethod(final ScriptableObject obj, final String methodName, final ArgumentsBuilder ab) {
      this.contextFactory.call(new ContextAction() {
         public Object run(Context cx) {
            ScriptableObject.callMethod(obj, methodName, ab.buildArguments());
            return null;
         }
      });
   }

   void callHandler(final Function handler, final Object[] args) {
      this.contextFactory.call(new ContextAction() {
         public Object run(Context cx) {
            handler.call(cx, RhinoInterpreter.this.globalObject, RhinoInterpreter.this.globalObject, args);
            return null;
         }
      });
   }

   void callHandler(final Function handler, final ArgumentsBuilder ab) {
      this.contextFactory.call(new ContextAction() {
         public Object run(Context cx) {
            Object[] args = ab.buildArguments();
            handler.call(cx, handler.getParentScope(), RhinoInterpreter.this.globalObject, args);
            return null;
         }
      });
   }

   Object call(ContextAction action) {
      return this.contextFactory.call(action);
   }

   Scriptable buildEventTargetWrapper(EventTarget obj) {
      return new EventTargetWrapper(this.globalObject, obj, this);
   }

   public void setOut(Writer out) {
   }

   public Locale getLocale() {
      return null;
   }

   public void setLocale(Locale locale) {
   }

   public String formatMessage(String key, Object[] args) {
      return null;
   }

   protected class Factory extends ContextFactory {
      protected Context makeContext() {
         Context cx = super.makeContext();
         cx.setWrapFactory(RhinoInterpreter.this.wrapFactory);
         cx.setSecurityController(RhinoInterpreter.this.securityController);
         cx.setClassShutter(RhinoInterpreter.this.classShutter);
         if (RhinoInterpreter.this.rhinoClassLoader == null) {
            cx.setOptimizationLevel(-1);
         }

         return cx;
      }
   }

   protected static class Entry {
      public String str;
      public Script script;

      public Entry(String str, Script script) {
         this.str = str;
         this.script = script;
      }
   }

   public interface ArgumentsBuilder {
      Object[] buildArguments();
   }
}
