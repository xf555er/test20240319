package net.jsign.bouncycastle.jcajce.provider.config;

import java.security.BasicPermission;
import java.security.Permission;
import java.util.StringTokenizer;
import net.jsign.bouncycastle.util.Strings;

public class ProviderConfigurationPermission extends BasicPermission {
   private final String actions;
   private final int permissionMask;

   public ProviderConfigurationPermission(String var1, String var2) {
      super(var1, var2);
      this.actions = var2;
      this.permissionMask = this.calculateMask(var2);
   }

   private int calculateMask(String var1) {
      StringTokenizer var2 = new StringTokenizer(Strings.toLowerCase(var1), " ,");
      int var3 = 0;

      while(var2.hasMoreTokens()) {
         String var4 = var2.nextToken();
         if (var4.equals("threadlocalecimplicitlyca")) {
            var3 |= 1;
         } else if (var4.equals("ecimplicitlyca")) {
            var3 |= 2;
         } else if (var4.equals("threadlocaldhdefaultparams")) {
            var3 |= 4;
         } else if (var4.equals("dhdefaultparams")) {
            var3 |= 8;
         } else if (var4.equals("acceptableeccurves")) {
            var3 |= 16;
         } else if (var4.equals("additionalecparameters")) {
            var3 |= 32;
         } else if (var4.equals("all")) {
            var3 |= 63;
         }
      }

      if (var3 == 0) {
         throw new IllegalArgumentException("unknown permissions passed to mask");
      } else {
         return var3;
      }
   }

   public String getActions() {
      return this.actions;
   }

   public boolean implies(Permission var1) {
      if (!(var1 instanceof ProviderConfigurationPermission)) {
         return false;
      } else if (!this.getName().equals(var1.getName())) {
         return false;
      } else {
         ProviderConfigurationPermission var2 = (ProviderConfigurationPermission)var1;
         return (this.permissionMask & var2.permissionMask) == var2.permissionMask;
      }
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof ProviderConfigurationPermission)) {
         return false;
      } else {
         ProviderConfigurationPermission var2 = (ProviderConfigurationPermission)var1;
         return this.permissionMask == var2.permissionMask && this.getName().equals(var2.getName());
      }
   }

   public int hashCode() {
      return this.getName().hashCode() + this.permissionMask;
   }
}
