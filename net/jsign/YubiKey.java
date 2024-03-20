package net.jsign;

import java.io.File;
import java.security.Provider;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class YubiKey {
   static Provider getProvider() {
      return ProviderUtils.createSunPKCS11Provider(getSunPKCS11Configuration());
   }

   static String getSunPKCS11Configuration() {
      File libykcs11 = getYkcs11Library();
      if (!libykcs11.exists()) {
         throw new ProviderException("YubiKey PKCS11 module (ykcs11) is not installed (" + libykcs11 + " is missing)");
      } else {
         return "--name=yubikey\nlibrary = " + libykcs11.getAbsolutePath();
      }
   }

   static File getYkcs11Library() {
      String osname = System.getProperty("os.name");
      String arch = System.getProperty("sun.arch.data.model");
      if (!osname.contains("Windows")) {
         if (osname.contains("Mac")) {
            return new File("/usr/local/lib/libykcs11.dylib");
         } else {
            List paths = new ArrayList();
            if ("32".equals(arch)) {
               paths.add("/usr/lib/libykcs11.so");
               paths.add("/usr/lib/libykcs11.so.1");
               paths.add("/usr/lib/i386-linux-gnu/libykcs11.so");
               paths.add("/usr/lib/arm-linux-gnueabi/libykcs11.so");
               paths.add("/usr/lib/arm-linux-gnueabihf/libykcs11.so");
            } else {
               paths.add("/usr/lib64/libykcs11.so");
               paths.add("/usr/lib64/libykcs11.so.1");
               paths.add("/usr/lib/x86_64-linux-gnu/libykcs11.so");
               paths.add("/usr/lib/aarch64-linux-gnu/libykcs11.so");
               paths.add("/usr/lib/mips64el-linux-gnuabi64/libykcs11.so");
               paths.add("/usr/lib/riscv64-linux-gnu/libykcs11.so");
            }

            Iterator var7 = paths.iterator();

            File libykcs11;
            do {
               if (!var7.hasNext()) {
                  return new File("/usr/local/lib/libykcs11.so");
               }

               String path = (String)var7.next();
               libykcs11 = new File(path);
            } while(!libykcs11.exists());

            return libykcs11;
         }
      } else {
         String programfiles;
         if ("32".equals(arch) && System.getenv("ProgramFiles(x86)") != null) {
            programfiles = System.getenv("ProgramFiles(x86)");
         } else {
            programfiles = System.getenv("ProgramFiles");
         }

         File libykcs11 = new File(programfiles + "/Yubico/Yubico PIV Tool/bin/libykcs11.dll");
         if (!System.getenv("PATH").contains("Yubico PIV Tool\\bin")) {
            System.out.println("WARNING: The YubiKey library path (" + libykcs11.getParentFile().getAbsolutePath().replace('/', '\\') + ") is missing from the PATH environment variable");
         }

         return libykcs11;
      }
   }
}
