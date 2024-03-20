package net.jsign.bouncycastle.crypto;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class CryptoServicesPermission extends Permission {
   private final Set actions = new HashSet();

   public CryptoServicesPermission(String var1) {
      super(var1);
      this.actions.add(var1);
   }

   public boolean implies(Permission var1) {
      if (var1 instanceof CryptoServicesPermission) {
         CryptoServicesPermission var2 = (CryptoServicesPermission)var1;
         if (this.getName().equals(var2.getName())) {
            return true;
         }

         if (this.actions.containsAll(var2.actions)) {
            return true;
         }
      }

      return false;
   }

   public boolean equals(Object var1) {
      if (var1 instanceof CryptoServicesPermission) {
         CryptoServicesPermission var2 = (CryptoServicesPermission)var1;
         if (this.actions.equals(var2.actions)) {
            return true;
         }
      }

      return false;
   }

   public int hashCode() {
      return this.actions.hashCode();
   }

   public String getActions() {
      return this.actions.toString();
   }
}
