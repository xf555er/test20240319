package org.apache.batik.script.rhino;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import org.mozilla.javascript.GeneratedClassLoader;

public class RhinoClassLoader extends URLClassLoader implements GeneratedClassLoader {
   protected URL documentURL;
   protected CodeSource codeSource;
   protected AccessControlContext rhinoAccessControlContext;

   public RhinoClassLoader(URL documentURL, ClassLoader parent) {
      super(documentURL != null ? new URL[]{documentURL} : new URL[0], parent);
      this.documentURL = documentURL;
      if (documentURL != null) {
         this.codeSource = new CodeSource(documentURL, (Certificate[])null);
      }

      ProtectionDomain rhinoProtectionDomain = new ProtectionDomain(this.codeSource, this.getPermissions(this.codeSource));
      this.rhinoAccessControlContext = new AccessControlContext(new ProtectionDomain[]{rhinoProtectionDomain});
   }

   static URL[] getURL(ClassLoader parent) {
      if (parent instanceof RhinoClassLoader) {
         URL documentURL = ((RhinoClassLoader)parent).documentURL;
         return documentURL != null ? new URL[]{documentURL} : new URL[0];
      } else {
         return new URL[0];
      }
   }

   public Class defineClass(String name, byte[] data) {
      return super.defineClass(name, data, 0, data.length, this.codeSource);
   }

   public void linkClass(Class clazz) {
      super.resolveClass(clazz);
   }

   public AccessControlContext getAccessControlContext() {
      return this.rhinoAccessControlContext;
   }

   protected PermissionCollection getPermissions(CodeSource codesource) {
      PermissionCollection perms = null;
      if (codesource != null) {
         perms = super.getPermissions(codesource);
      }

      if (this.documentURL != null && perms != null) {
         Permission p = null;
         Permission dirPerm = null;

         try {
            p = this.documentURL.openConnection().getPermission();
         } catch (IOException var7) {
            p = null;
         }

         if (p instanceof FilePermission) {
            String path = p.getName();
            if (!path.endsWith(File.separator)) {
               int dirEnd = path.lastIndexOf(File.separator);
               if (dirEnd != -1) {
                  path = path.substring(0, dirEnd + 1);
                  path = path + "-";
                  dirPerm = new FilePermission(path, "read");
                  perms.add(dirPerm);
               }
            }
         }
      }

      return perms;
   }
}
