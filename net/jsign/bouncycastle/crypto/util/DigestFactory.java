package net.jsign.bouncycastle.crypto.util;

import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.crypto.Digest;
import net.jsign.bouncycastle.crypto.digests.MD5Digest;
import net.jsign.bouncycastle.crypto.digests.SHA1Digest;
import net.jsign.bouncycastle.crypto.digests.SHA224Digest;
import net.jsign.bouncycastle.crypto.digests.SHA256Digest;
import net.jsign.bouncycastle.crypto.digests.SHA384Digest;
import net.jsign.bouncycastle.crypto.digests.SHA3Digest;
import net.jsign.bouncycastle.crypto.digests.SHA512Digest;
import net.jsign.bouncycastle.crypto.digests.SHAKEDigest;

public final class DigestFactory {
   private static final Map cloneMap = new HashMap();

   public static Digest createMD5() {
      return new MD5Digest();
   }

   public static Digest createSHA1() {
      return new SHA1Digest();
   }

   public static Digest createSHA224() {
      return new SHA224Digest();
   }

   public static Digest createSHA256() {
      return new SHA256Digest();
   }

   public static Digest createSHA384() {
      return new SHA384Digest();
   }

   public static Digest createSHA512() {
      return new SHA512Digest();
   }

   public static Digest createSHA3_224() {
      return new SHA3Digest(224);
   }

   public static Digest createSHA3_256() {
      return new SHA3Digest(256);
   }

   public static Digest createSHA3_384() {
      return new SHA3Digest(384);
   }

   public static Digest createSHA3_512() {
      return new SHA3Digest(512);
   }

   public static Digest createSHAKE128() {
      return new SHAKEDigest(128);
   }

   public static Digest createSHAKE256() {
      return new SHAKEDigest(256);
   }

   static {
      cloneMap.put(createMD5().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA1().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA224().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA256().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA384().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA512().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA3_224().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA3_256().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA3_384().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHA3_512().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHAKE128().getAlgorithmName(), new Object() {
      });
      cloneMap.put(createSHAKE256().getAlgorithmName(), new Object() {
      });
   }
}
