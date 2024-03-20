package net.jsign.jca;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

public class SigningServicePrivateKey implements PrivateKey {
   private final String id;
   private final String algorithm;
   private final Map properties = new HashMap();

   public SigningServicePrivateKey(String id, String algorithm) {
      this.id = id;
      this.algorithm = algorithm;
   }

   public String getId() {
      return this.id;
   }

   public Map getProperties() {
      return this.properties;
   }

   public String getAlgorithm() {
      return this.algorithm;
   }

   public String getFormat() {
      throw new UnsupportedOperationException();
   }

   public byte[] getEncoded() {
      throw new UnsupportedOperationException();
   }
}
