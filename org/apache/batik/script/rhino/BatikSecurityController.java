package org.apache.batik.script.rhino;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.WrappedException;

public class BatikSecurityController extends SecurityController {
   public GeneratedClassLoader createClassLoader(ClassLoader parentLoader, Object securityDomain) {
      if (securityDomain instanceof RhinoClassLoader) {
         return (RhinoClassLoader)securityDomain;
      } else {
         throw new SecurityException("Script() objects are not supported");
      }
   }

   public Object getDynamicSecurityDomain(Object securityDomain) {
      ClassLoader loader = (RhinoClassLoader)securityDomain;
      return loader != null ? loader : AccessController.getContext();
   }

   public Object callWithDomain(Object securityDomain, final Context cx, final Callable callable, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
      AccessControlContext acc;
      if (securityDomain instanceof AccessControlContext) {
         acc = (AccessControlContext)securityDomain;
      } else {
         RhinoClassLoader loader = (RhinoClassLoader)securityDomain;
         acc = loader.rhinoAccessControlContext;
      }

      PrivilegedExceptionAction execAction = new PrivilegedExceptionAction() {
         public Object run() {
            return callable.call(cx, scope, thisObj, args);
         }
      };

      try {
         return AccessController.doPrivileged(execAction, acc);
      } catch (Exception var10) {
         throw new WrappedException(var10);
      }
   }
}
