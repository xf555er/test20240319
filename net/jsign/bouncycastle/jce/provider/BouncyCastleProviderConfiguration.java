package net.jsign.bouncycastle.jce.provider;

import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.jsign.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import net.jsign.bouncycastle.jcajce.provider.config.ProviderConfigurationPermission;

class BouncyCastleProviderConfiguration implements ProviderConfiguration {
   private static Permission BC_EC_LOCAL_PERMISSION = new ProviderConfigurationPermission("BC", "threadLocalEcImplicitlyCa");
   private static Permission BC_EC_PERMISSION = new ProviderConfigurationPermission("BC", "ecImplicitlyCa");
   private static Permission BC_DH_LOCAL_PERMISSION = new ProviderConfigurationPermission("BC", "threadLocalDhDefaultParams");
   private static Permission BC_DH_PERMISSION = new ProviderConfigurationPermission("BC", "DhDefaultParams");
   private static Permission BC_EC_CURVE_PERMISSION = new ProviderConfigurationPermission("BC", "acceptableEcCurves");
   private static Permission BC_ADDITIONAL_EC_CURVE_PERMISSION = new ProviderConfigurationPermission("BC", "additionalEcParameters");
   private ThreadLocal ecThreadSpec = new ThreadLocal();
   private ThreadLocal dhThreadSpec = new ThreadLocal();
   private volatile Set acceptableNamedCurves = new HashSet();
   private volatile Map additionalECParameters = new HashMap();
}
